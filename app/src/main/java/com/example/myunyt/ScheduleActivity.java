package com.example.myunyt;

import android.app.AlertDialog;
import android.graphics.Color;
import android.text.TextUtils;
import android.app.TimePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;

import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

public class ScheduleActivity extends AppCompatActivity {

    private int userId;

    private EditText subjectET;
    private AutoCompleteTextView profAC;
    private Spinner  daySpinner;
    private Button   startBtn, endBtn, addBtn;
    private CheckBox confirmCheck;
    private Button clearBtn;
    private Button   confirmBtn;
    private TextView summaryText;
    private TableLayout table;
    private int  startMin = -1, endMin = -1;
    private int  selectedProfId = -1;
    private DatabaseHelper db;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        db = new DatabaseHelper(this);

        userId = getIntent().getIntExtra("USER_ID", -1);

        subjectET  = findViewById(R.id.subjEditText);
        profAC     = findViewById(R.id.profAutoComplete);
        daySpinner = findViewById(R.id.daySpinner);
        startBtn   = findViewById(R.id.startButton);
        endBtn     = findViewById(R.id.endButton);
        addBtn     = findViewById(R.id.addSchedButton);
        table      = findViewById(R.id.timetable);
        confirmCheck = findViewById(R.id.confirmCheck);
        clearBtn = findViewById(R.id.clearButton);
        confirmBtn   = findViewById(R.id.confirmButton);
        summaryText  = findViewById(R.id.summaryText);

        boolean windowOpen   = SemesterUtils.isEditWindowOpen();
        boolean alreadyConf  = db.isScheduleConfirmed(userId);
        boolean editable     = windowOpen && !alreadyConf;

        setInputsEnabled(editable);

        if (alreadyConf) {
            drawSummary(userId);
        }


        ArrayAdapter<String> days = new ArrayAdapter<>(this,
                R.layout.spinner_item,
                new String[]{"Mon","Tue","Wed","Thu","Fri"});
        days.setDropDownViewResource(R.layout.spinner_dropdown_item);
        daySpinner.setAdapter(days);


        Cursor c = db.getAllProfessorNames();
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                c,
                new String[]{"name"},
                new int[]{android.R.id.text1},
                0);

        profAC.setAdapter(new SimpleCursorAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                db.getAllProfessorNames(),
                new String[]{"name"},
                new int[]{android.R.id.text1},
                0) {
            @Override
            public CharSequence convertToString(Cursor cursor) {
                return cursor.getString(cursor.getColumnIndexOrThrow("name"));
            }
        });

        profAC.setOnItemClickListener((parent, view, position, id) -> {
            Cursor cursor = (Cursor) parent.getItemAtPosition(position);
            selectedProfId = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
            profAC.setText(cursor.getString(cursor.getColumnIndexOrThrow("name")));
        });


        startBtn.setOnClickListener(v -> pickTime(true));
        endBtn  .setOnClickListener(v -> pickTime(false));

        addBtn.setOnClickListener(v -> {
            String subj = subjectET.getText().toString().trim();
            if (TextUtils.isEmpty(subj) || selectedProfId == -1 ||
                    startMin == -1 || endMin == -1 || endMin <= startMin) {
                Toast.makeText(this, "Fill all fields correctly", Toast.LENGTH_SHORT).show();
                return;
            }
            db.saveDraftSchedule(userId, subj,
                    daySpinner.getSelectedItem().toString(),
                    startMin, endMin, selectedProfId);

            subjectET.setText("");
            profAC.setText("");
            selectedProfId = -1;
            startMin = endMin = -1;
            startBtn.setText("Start"); endBtn.setText("End");

            drawTable();
        });

        clearBtn.setOnClickListener(v -> {
            if (db.isScheduleConfirmed(userId)) {
                new AlertDialog.Builder(this)
                        .setTitle("Schedule Confirmed")
                        .setMessage("Schedule confirmed. Talk with your advisor if you want to make any changes.")
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
                return;
            }
            db.clearDraftSchedule(userId);
            drawTable();
            Toast.makeText(this,"Draft cleared",Toast.LENGTH_SHORT).show();
        });

        confirmBtn.setOnClickListener(v -> {
            if (!confirmCheck.isChecked()) {
                Toast.makeText(this, "Tick the checkbox first", Toast.LENGTH_SHORT).show();
                return;
            }
            db.confirmSchedule(userId);
            setInputsEnabled(false);
            drawSummary(userId);
            Toast.makeText(this,
                    "Schedule confirmed – locked until next window",
                    Toast.LENGTH_LONG).show();
        });
        drawTable();
    }

    private void pickTime(boolean isStart) {
        Calendar c = Calendar.getInstance();
        int initialHour = c.get(Calendar.HOUR_OF_DAY);
        int initialMinute = c.get(Calendar.MINUTE);

        if (initialHour < 9) {
            initialHour = 9;
            initialMinute = 0;
        }
        else if (initialHour >= 18) {
            initialHour = 18;
            initialMinute = 0;
        }

        TimePickerDialog timePicker = new TimePickerDialog(this,
                (view, h, m) -> {
                    int minutes = h * 60 + m;
                    if (h < 9 || h >= 18) {
                        Toast.makeText(this, "Time must be between 9:00 AM and 6:00 PM", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (isStart) {
                        startMin = minutes;
                        startBtn.setText(String.format(Locale.getDefault(), "%02d:%02d", h, m));
                    } else {
                        endMin = minutes;
                        endBtn.setText(String.format(Locale.getDefault(), "%02d:%02d", h, m));
                    }
                },
                initialHour,
                initialMinute,
                true);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            timePicker.setTitle("Select Time (9:00 AM - 6:00 PM)");
        }

        timePicker.show();
    }

    private void drawTable() {
        table.removeAllViews();

        TableRow header = new TableRow(this);
        header.addView(makeCell(""));
        for (String d : new String[]{"Mon","Tue","Wed","Thu","Fri"})
            header.addView(makeCell(d));
        table.addView(header);

        for (int hour=8; hour<=19; hour++) {
            TableRow row = new TableRow(this);
            row.addView(makeCell(String.format("%02d:00", hour)));

            for (String d : new String[]{"Mon","Tue","Wed","Thu","Fri"}) {
                TextView cell = makeCell("");

                try (Cursor c = db.getReadableDatabase().rawQuery(
                        "SELECT s.subject, p.name " +
                                "FROM schedules s JOIN professors p ON p._id=s.professor_id " +
                                "WHERE s.user_id=? AND s.day=? " +
                                "  AND s.start_min<=? AND s.end_min>? " +
                                "LIMIT 1",
                        new String[]{ String.valueOf(userId),
                                d,
                                String.valueOf(hour*60),
                                String.valueOf(hour*60)})) {
                    if (c.moveToFirst())
                        cell.setText(c.getString(0) + "\n" + c.getString(1));
                }
                row.addView(cell);
            }
            table.addView(row);
        }
    }

    private TextView makeCell(String txt) {
        TextView tv = new TextView(this);
        tv.setText(txt);
        tv.setTextColor(Color.WHITE);
        tv.setBackgroundColor(Color.TRANSPARENT);
        tv.setPadding(8, 4, 8, 4);
        tv.setBackgroundResource(R.drawable.cell_border);

        tv.setWidth(220);
        tv.setHeight(140);

        tv.setSingleLine(false);
        tv.setEllipsize(null);
        tv.setMaxLines(3);

        return tv;
    }

    private void setInputsEnabled(boolean on) {
        subjectET.setEnabled(on);
        profAC.setEnabled(on);
        daySpinner.setEnabled(on);
        startBtn.setEnabled(on);
        endBtn.setEnabled(on);
        addBtn.setEnabled(on);
        confirmCheck.setEnabled(on);
        confirmBtn.setEnabled(on);
    }

    private void drawSummary(int userId) {
        StringBuilder sb = new StringBuilder("• Your confirmed schedule:\n");
        try (Cursor c = db.getConfirmedBlocks(userId)) {
            while (c.moveToNext()) {
                String day      = c.getString(0);
                int    startMin = c.getInt(1);
                int    endMin   = c.getInt(2);
                String subject  = c.getString(3);
                String profName = c.getString(4);
                sb.append(String.format(
                        Locale.getDefault(),
                        "%s %02d:%02d-%02d:%02d  %s  (%s)\n",
                        day,
                        startMin/60, startMin%60,
                        endMin/60,   endMin%60,
                        subject, profName));
            }
        }
        summaryText.setText(sb.toString());
    }
}
