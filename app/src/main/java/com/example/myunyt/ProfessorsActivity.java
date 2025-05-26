package com.example.myunyt;

import android.database.Cursor;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageButton;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.ArrayList;
import java.util.List;

public class ProfessorsActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private ProfessorAdapter professorAdapter;
    private List<Professor> professorList;
    private String faculty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professors);

        dbHelper = new DatabaseHelper(this);
        initializeViews();
        setupRecyclerView();

        faculty = getIntent().getStringExtra("FACULTY");
        if (faculty != null) {
            loadProfessors();
        } else {
            Log.e("ProfessorsActivity", "No faculty specified");
            finish();
        }
    }
    private void initializeViews() {
        recyclerView = findViewById(R.id.recycler_view);

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            finish();

            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    private void setupRecyclerView() {
        professorList = new ArrayList<>();
        professorAdapter = new ProfessorAdapter(professorList);

        professorAdapter.setOnItemClickListener(professor -> {
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(professorAdapter);
    }


    private void loadProfessors() {
        new Thread(() -> {
            try {
                Cursor cursor = dbHelper.getProfessorsByFaculty(faculty);
                List<Professor> tempList = new ArrayList<>();

                if (cursor != null) {
                    Log.d("ProfessorsActivity", "Cursor count: " + cursor.getCount());
                    while (cursor.moveToNext()) {
                        try {
                            int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                            String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME));
                            String courses = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSES));
                            String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMAGE));

                            float rating = 0;
                            try {
                                rating = cursor.getFloat(cursor.getColumnIndexOrThrow("average_rating"));
                            } catch (Exception e) {
                                Log.w("ProfessorsActivity", "No rating found for professor", e);
                            }

                            Log.d("ProfessorsActivity", "Adding professor: " + name);
                            tempList.add(new Professor(id, name, courses, imageUrl, faculty, rating));
                        } catch (IllegalArgumentException e) {
                            Log.e("ProfessorsActivity", "Error reading professor data", e);
                        }
                    }
                    cursor.close();
                } else {
                    Log.e("ProfessorsActivity", "Cursor is null");
                }
                runOnUiThread(() -> {
                    professorList.clear();
                    professorList.addAll(tempList);
                    professorAdapter.updateProfessorsList(tempList);

                    if (professorList.isEmpty()) {
                        Log.w("ProfessorsActivity", "No professors found for faculty: " + faculty);
                    }
                });
            } catch (Exception e) {
                Log.e("ProfessorsActivity", "Error loading professors", e);
                runOnUiThread(() -> {
                    finish();
                });
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}