package com.example.myunyt;

import android.os.Bundle;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AcademicCalendarActivity extends AppCompatActivity {

    private HorizontalScrollView fallTermCalendar;
    private HorizontalScrollView springTermCalendar;
    private TableLayout fallCalendarTable;
    private TableLayout springCalendarTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_academic_calendar);

        fallTermCalendar = findViewById(R.id.fallTermCalendar);
        springTermCalendar = findViewById(R.id.springTermCalendar);
        fallCalendarTable = findViewById(R.id.fallCalendarTable);
        springCalendarTable = findViewById(R.id.springCalendarTable);

        TextView fallTermText = findViewById(R.id.fallTerm);
        TextView springTermText = findViewById(R.id.springTerm);

        fallTermCalendar.setVisibility(View.VISIBLE);
        springTermCalendar.setVisibility(View.GONE);

        fallTermText.setOnClickListener(v -> {
            fallTermCalendar.setVisibility(View.VISIBLE);
            springTermCalendar.setVisibility(View.GONE);
        });

        springTermText.setOnClickListener(v -> {
            springTermCalendar.setVisibility(View.VISIBLE);
            fallTermCalendar.setVisibility(View.GONE);
        });

        String[][] fallCalendarData = {
                {"September 30", "Registration Begins"},
                {"October 04", "Registration End"},
                {"October 07", "Classes Begin; Add & Drop Period Begins"},
                {"October 11", "Add & Drop Period Ends"},
                {"November 22", "Alphabet Day, No Classes"},
                {"November 28", "Independence Day, No Classes"},
                {"November 29", "Liberation Day, No Classes"},
                {"December 02", "Mid-Term Exam Period Begins"},
                {"December 09", "National Youth Day, No Classes"},
                {"December 10" , "Mid-Term Exam Period Ends"},
                {"December 19", "Last Day to Submit Mid-Term Grades"},
                {"December 20", "Last Day to Withdraw from Classes"},
                {"December 24", "Holiday Season Recess Begins"},
                {"January 06", "Classes Resume"},
                {"January 31", "Last Day of Classes"},
                {"February 10", "Final Exams Period Begins"},
                {"February 14", "Final Exams Period Ends"},
                {"February 21", "Last Day to Submit Final Grades"},
        };

        String[][] springCalendarData = {
                {"February 24" , "Registration Begins"},
                {"February 28" , "Registration End"},
                {"March 03" , "Classes Begin; Add & Drop Period Begins"},
                {"March 07" , "Add & Drop Period Ends"},
                {"March 14" , "Registration Begins"},
                {"March 24" , "Summer Day, No Classes"},
                {"March 31" , "Religious Holiday Nowruz Day,No Classes"},
                {"April 21" , "Catholic and Orthodox Easter, No classes"},
                {"April 22" , "Mid-Term Exam Period Begins"},
                {"April 28" , "Mid-Term Exam Period Ends"},
                {"May 01" , "International Workers Day, No Classes"},
                {"May 07" , "Last Day to Submit Mid-Term Grades"},
                {"May 09" , "Last Day to Withdraw from Classes"},
                {"June 06" , "Religious Holiday Eid al-Adha, No Classes"},
                {"June 09" , "Last Day of Classes"},
                {"June 16" , "Final Exams Period Begins"},
                {"June 20" , "Final Exams Period Ends"},
                {"June 27" , "Last Day to Submit Final Grades"},
        };

        for (String[] entry : fallCalendarData) {
            String date = entry[0];
            String description = entry[1];
            String day = getDayOfWeek(date);

            TableRow row = new TableRow(this);

            TextView dateView = new TextView(this);
            dateView.setText(date);
            dateView.setTextColor(getResources().getColor(android.R.color.white));
            dateView.setBackgroundResource(R.drawable.cell_border);
            dateView.setPadding(16, 16, 16, 16);
            dateView.setGravity(android.view.Gravity.CENTER);

            TextView dayView = new TextView(this);
            dayView.setText(day);
            dayView.setTextColor(getResources().getColor(android.R.color.white));
            dayView.setBackgroundResource(R.drawable.cell_border);
            dayView.setPadding(16, 16, 16, 16);
            dayView.setGravity(android.view.Gravity.CENTER);

            TextView descriptionView = new TextView(this);
            descriptionView.setText(description);
            descriptionView.setTextColor(getResources().getColor(android.R.color.white));
            descriptionView.setBackgroundResource(R.drawable.cell_border);
            descriptionView.setPadding(16, 16, 16, 16);
            descriptionView.setGravity(android.view.Gravity.CENTER);

            row.addView(dateView);
            row.addView(dayView);
            row.addView(descriptionView);

            fallCalendarTable.addView(row);
        }

        for (String[] entry : springCalendarData) {
            String date = entry[0];
            String description = entry[1];
            String day = getDayOfWeek(date);

            TableRow row = new TableRow(this);

            TextView dateView = new TextView(this);
            dateView.setText(date);
            dateView.setTextColor(getResources().getColor(android.R.color.white));
            dateView.setBackgroundResource(R.drawable.cell_border);
            dateView.setPadding(16, 16, 16, 16);
            dateView.setGravity(android.view.Gravity.CENTER);

            TextView dayView = new TextView(this);
            dayView.setText(day);
            dayView.setTextColor(getResources().getColor(android.R.color.white));
            dayView.setBackgroundResource(R.drawable.cell_border);
            dayView.setPadding(16, 16, 16, 16);
            dayView.setGravity(android.view.Gravity.CENTER);

            TextView descriptionView = new TextView(this);
            descriptionView.setText(description);
            descriptionView.setTextColor(getResources().getColor(android.R.color.white));
            descriptionView.setBackgroundResource(R.drawable.cell_border);
            descriptionView.setPadding(16, 16, 16, 16);
            descriptionView.setGravity(android.view.Gravity.CENTER);

            row.addView(dateView);
            row.addView(dayView);
            row.addView(descriptionView);

            springCalendarTable.addView(row);
        }

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

    }

    private TextView createTableCell(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextColor(getResources().getColor(android.R.color.white));
        textView.setBackgroundResource(R.drawable.cell_border);
        textView.setPadding(16, 16, 16, 16);
        textView.setGravity(android.view.Gravity.CENTER);
        return textView;
    }

    public String getDayOfWeek(String dateString) {
        try {
            String fullDate = dateString + " 2025";
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d yyyy", Locale.ENGLISH);
            Date date = sdf.parse(fullDate);
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.ENGLISH);
            return dayFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return "Invalid";
        }
    }
}