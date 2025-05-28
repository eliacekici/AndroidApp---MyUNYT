package com.example.myunyt;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button   signupButton;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DatabaseHelper(this);

        usernameEditText = findViewById(R.id.regUsernameEditText);
        emailEditText    = findViewById(R.id.regEmailEditText);
        passwordEditText = findViewById(R.id.regPasswordEditText);
        signupButton     = findViewById(R.id.regSignupButton);

        signupButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String email    = emailEditText.getText().toString().trim().toLowerCase();
            String password = passwordEditText.getText().toString().trim();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Fill all three fields", Toast.LENGTH_SHORT).show();
            }

            long rowId = dbHelper.addUser(email, password,username);
            if (rowId == -1) {
                Toast.makeText(this, "Account already exists.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Account created! You can now log in.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}