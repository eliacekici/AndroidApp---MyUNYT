package com.example.myunyt;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class FacultySelectionActivity extends AppCompatActivity {

    public static final String FACULTY_ECONOMY = "Economy and Business";
    public static final String FACULTY_LAW = "Law and Social Sciences";
    public static final String FACULTY_ENGINEERING = "Engineering and Architecture";
    private DatabaseHelper dbHelper;
    private RadioGroup facultyRadioGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.faculties);

        dbHelper = new DatabaseHelper(this);

        facultyRadioGroup = findViewById(R.id.facultyRadioGroup);
        Button continueButton = findViewById(R.id.continueButton);
        ImageButton backButton = findViewById(R.id.backButton);

        continueButton.setOnClickListener(v -> {
            int selectedId = facultyRadioGroup.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, "Please select a faculty", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton selectedRadio = findViewById(selectedId);
            String selectedText = selectedRadio.getText().toString();

            String selectedFaculty;
            if (selectedText.equals("Faculty of Economy and Business")) {
                selectedFaculty = FACULTY_ECONOMY;
            } else if (selectedText.equals("Faculty of Law and Social Sciences")) {
                selectedFaculty = FACULTY_LAW;
            } else if (selectedText.equals("Faculty of Engineering and Architecture")) {
                selectedFaculty = FACULTY_ENGINEERING;
            } else {
                Toast.makeText(this, "Invalid faculty selection", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, ProfessorsActivity.class);
            intent.putExtra("FACULTY", selectedFaculty);
            startActivity(intent);
        });

        backButton.setOnClickListener(v -> finish());

    }
}