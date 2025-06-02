package com.example.myunyt;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.text.DateFormat;

import com.google.android.material.navigation.NavigationView;

import android.widget.Button;
import android.widget.ImageButton;
import androidx.fragment.app.FragmentTransaction;

public class MainActivityPage extends AppCompatActivity {

    private int userId;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FrameLayout menuContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_page);

        userId = getIntent().getIntExtra("USER_ID", -1);
        if (userId == -1) {
            finish();
            return;
        }

        TextView dateTextView = findViewById(R.id.dateTextView);
        TextView welcomeTextView = findViewById(R.id.welcomeTextView);
        LinearLayout transportationButton = findViewById(R.id.transportationButton);
        FrameLayout academicCalendarButton = findViewById(R.id.academicCalendarButton);

        LinearLayout myScheduleButton = findViewById(R.id.myCalendar);
        FrameLayout container = findViewById(R.id.fragment_container);
        ScrollView scrollView = findViewById(R.id.mainScrollView);

        TextView motivationalQuote = findViewById(R.id.motivationalQuote);
        motivationalQuote.setText(getDailyMotivationalQuote());

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        menuContainer = findViewById(R.id.menuContainer);

        FrameLayout libraryFragmentContainer = findViewById(R.id.libraryFragmentContainer);
        Button libraryButton = findViewById(R.id.libraryButton);

        FrameLayout studentsPortalButton = findViewById(R.id.studentsPortalButton);

        setupNavigationView();
        setupSemesterProgress();

        String currentDate = DateFormat.getDateInstance(DateFormat.FULL, Locale.getDefault()).format(new Date());
        dateTextView.setText(currentDate);

        String uname = getIntent().getStringExtra("USERNAME");
        welcomeTextView.setText("Welcome, " + uname + "!");

        TextView motivationalMessage = findViewById(R.id.motivationalMessage);
        motivationalMessage.setText(getTimeBasedMessage());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        menuContainer.setOnClickListener(v -> {
            openMenu();
            drawerLayout.openDrawer(GravityCompat.START);
        });

        academicCalendarButton.setOnClickListener(v ->
                startActivity(new Intent(this, AcademicCalendarActivity.class)));

        transportationButton.setOnClickListener(v ->
                startActivity(new Intent(this, TransportationActivity.class)));


        myScheduleButton.setOnClickListener(v -> {
            Intent i = new Intent(MainActivityPage.this, ScheduleActivity.class);
            i.putExtra("USER_ID", userId);
            startActivity(i);
        });

        libraryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                libraryButton.setVisibility(View.GONE);

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.libraryFragmentContainer, new LibraryFragment());
                transaction.commit();
            }
        });

        studentsPortalButton.setOnClickListener(v -> {
            startActivity(new Intent(this, StudentsPortalActivity.class));
        });

    }

    private void setupNavigationView() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            DatabaseHelper dbHelper = new DatabaseHelper(this);

            if (id == R.id.nav_vote) {
                if (dbHelper.isScheduleConfirmed(userId)) {
                    Intent voteIntent = new Intent(this, ProfessorsActivity.class);
                    voteIntent.putExtra("USER_ID", userId);
                    voteIntent.putExtra("VOTE_MODE", true);
                    startActivity(voteIntent);
                } else {
                    Toast.makeText(this,
                            "Please confirm your schedule first before voting",
                            Toast.LENGTH_LONG).show();
                }
            } else if (id == R.id.nav_information) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.unyt.edu.al/"));
                startActivity(browserIntent);
            } else if (id == R.id.nav_location) {

                View actionView = item.getActionView();
                RadioGroup radioGroup = actionView.findViewById(R.id.location_radio_group);

                radioGroup.setVisibility(
                        radioGroup.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);

                radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                    if (checkedId == R.id.main_campus_radio) {
                        openMapLocation("https://maps.app.goo.gl/hEj3dAUACVwHfs3r6");
                    } else if (checkedId == R.id.east_campus_radio) {
                        openMapLocation("https://maps.app.goo.gl/B7sxikCXUBJCqzMr5");
                    }
                    radioGroup.setVisibility(View.GONE);
                    drawerLayout.closeDrawer(GravityCompat.START);
                });

                return true;
            } else if (id == R.id.nav_instagram) {
                openInstagram();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }


    private void openInstagram() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/unyt_official/"));
            intent.setPackage("com.instagram.android");
            startActivity(intent);
        } catch (Exception e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/unyt_official/")));
        }
    }

    private void openMapLocation(String mapUrl) {
        try {
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mapUrl));
            mapIntent.setPackage("com.google.android.apps.maps");

            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mapUrl));
                startActivity(browserIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error opening maps", Toast.LENGTH_SHORT).show();
        }
    }

    private void openMenu() {
    }

    @Override
    public void onBackPressed() {
        FrameLayout container = findViewById(R.id.fragment_container);
        ScrollView scrollView = findViewById(R.id.mainScrollView);

        if (container.getVisibility() == View.VISIBLE) {
            container.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
    private String getTimeBasedMessage() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= 7 && hour < 12) {
            return "Good morning. Have a nice and productive day — your future depends on it.";
        } else if (hour >= 12 && hour < 20) {
            return "Keep going — progress is built one decision at a time.";
        } else {
            return "Rest well. Growth happens even in the quiet moments.";
        }
    }

    private void setupSemesterProgress() {
        Calendar startDate = Calendar.getInstance();
        startDate.set(2024, Calendar.OCTOBER, 7);

        Calendar endDate = Calendar.getInstance();
        endDate.set(2025, Calendar.JUNE, 20);

        Calendar today = Calendar.getInstance();

        float progress = calculateProgressPercentage(startDate, endDate, today);

        updateProgressUI(progress);
    }

    private float calculateProgressPercentage(Calendar startDate, Calendar endDate, Calendar today) {
        long totalMillis = endDate.getTimeInMillis() - startDate.getTimeInMillis();
        long elapsedMillis = today.getTimeInMillis() - startDate.getTimeInMillis();

        return Math.max(0, Math.min(100, (elapsedMillis * 100f / totalMillis)));
    }

    private void updateProgressUI(float progress) {
        ProgressBar progressBar = findViewById(R.id.semesterProgress);
        TextView progressText = findViewById(R.id.progressText);

        progressBar.setProgress((int) progress);
        progressText.setText(String.format(Locale.getDefault(),
                "%.0f%% of semester completed", progress));
    }

    private String getDailyMotivationalQuote() {
        String[] quotes = {
                "Believe you can and you're halfway there. — Theodore Roosevelt",
                "The only way to do great work is to love what you do. — Steve Jobs",
                "You are never too old to set another goal or to dream a new dream. — C.S. Lewis",
                "Success is not final, failure is not fatal: It is the courage to continue that counts. — Winston Churchill",
                "Don't watch the clock; do what it does. Keep going. — Sam Levenson",
                "The future depends on what you do today. — Mahatma Gandhi",
                "Start where you are. Use what you have. Do what you can. — Arthur Ashe",
                "Dream big and dare to fail. — Norman Vaughan",
                "What we achieve inwardly will change outer reality. — Plutarch",
                "The harder you work for something, the greater you'll feel when you achieve it.",
                "Don't stop when you're tired. Stop when you're done.",
                "Little by little, a little becomes a lot. — Tanzanian Proverb",
                "It always seems impossible until it's done. — Nelson Mandela",
                "Success is walking from failure to failure with no loss of enthusiasm. — Winston Churchill",
                "You don’t have to be great to start, but you have to start to be great. — Zig Ziglar"
        };

        Calendar calendar = Calendar.getInstance();
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);

        int index = dayOfYear % quotes.length;
        return quotes[index];
    }
}
