package com.example.myunyt;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.ArrayList;
import java.util.List;

public class ProfessorsActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private ProfessorAdapter professorAdapter;
    private List<Professor> professorList;
    private boolean isVoteMode;
    private boolean hasVotedThisSemester = false;
    private SharedPreferences sharedPreferences;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professors);

        isVoteMode = getIntent().getBooleanExtra("VOTE_MODE", false);
        userId = getIntent().getIntExtra("USER_ID", -1);

        sharedPreferences = getSharedPreferences("VotingPrefs", MODE_PRIVATE);
        hasVotedThisSemester = sharedPreferences.getBoolean("hasVoted_" + userId, false);

        dbHelper = new DatabaseHelper(this);
        initializeViews();
        setupRecyclerView();

        if (userId != -1) {
            loadProfessors(userId);
        } else {
            Log.e("ProfessorsActivity", "No user ID specified");
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

        professorAdapter.setOnRatingChangedListener((profId, rating) -> {
            if (isVoteMode) {
                new Thread(() -> {
                    try {
                        dbHelper.saveOrUpdateVote(userId, profId, rating);
                    } catch (Exception e) {
                        Log.e("Votes", "saveOrUpdate failed", e);
                    }
                }).start();
            }
        });


        professorAdapter.setSubmitClickListener(() -> {
            if (hasVotedThisSemester) {
                Toast.makeText(this,
                        "You've already voted this semester. Please wait until next semester.",
                        Toast.LENGTH_LONG).show();
            } else {
                submitVotes();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(professorAdapter);
    }


    private void loadProfessors(int userId) {
        new Thread(() -> {
            try {
                hasVotedThisSemester = dbHelper.hasUserVotedThisSemester(userId);

                Cursor cursor;
                if (isVoteMode) {
                    cursor = dbHelper.getProfessorsFromConfirmedSchedule(userId);
                } else {
                    cursor = dbHelper.getProfessorsByFaculty("");
                }
                List<Professor> tempList = new ArrayList<>();

                if (cursor != null) {
                    Log.d("ProfessorsActivity", "Cursor count: " + cursor.getCount());
                    while (cursor.moveToNext()) {
                        try {
                            int    id       = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                            String name     = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME));
                            String courses  = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSES));
                            String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMAGE));

                            float avgRating = 0f;
                            try {
                                avgRating = cursor.getFloat(cursor.getColumnIndexOrThrow("average_rating"));
                            } catch (Exception ignore) {}


                            int  myColIndex = cursor.getColumnIndexOrThrow("my_rating");
                            boolean hasMyRating = !cursor.isNull(myColIndex);
                            float   myRating    = hasMyRating
                                    ? cursor.getFloat(myColIndex)
                                    : avgRating;

                            Professor prof = new Professor(id, name, courses, imageUrl, "", myRating);
                            prof.setHasVoted(hasMyRating);

                            tempList.add(prof);

                            Log.d("ProfessorsActivity", "Added professor " + name +
                                    (hasMyRating ? " (user rated " + myRating + ")" : ""));
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

                    professorAdapter.setVoteMode(isVoteMode && !hasVotedThisSemester);

                    if (professorList.isEmpty()) {
                        if (isVoteMode) {
                            Toast.makeText(ProfessorsActivity.this,
                                    "No professors in your schedule to vote for",
                                    Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            Log.w("ProfessorsActivity", "No professors found");
                        }
                    }
                });
            } catch (Exception e) {
                Log.e("ProfessorsActivity", "Error loading professors", e);
                runOnUiThread(() -> finish());
            }
        }).start();
    }

    private void submitVotes() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("hasVoted_" + userId, true);
        editor.apply();

        hasVotedThisSemester = true;
        professorAdapter.setVoteMode(false);

        new Thread(() -> {
            for (Professor p : professorList) {
                dbHelper.saveOrUpdateVote(userId, p.getId(), p.getRating());
            }
            runOnUiThread(() -> {
                Toast.makeText(this,
                        "Thank you for voting! Your ratings have been submitted.",
                        Toast.LENGTH_LONG).show();
            });
        }).start();
    }

    public void resetVotingForNewSemester() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("hasVoted_" + userId, false);
        editor.apply();
        hasVotedThisSemester = false;
        if (professorAdapter != null) {
            professorAdapter.setVoteMode(isVoteMode);
        }
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}