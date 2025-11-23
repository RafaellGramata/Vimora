package com.example.vimora.trainee;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vimora.DatabaseHelper;
import com.example.vimora.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TraineeTrackActivity03 extends AppCompatActivity {
    DatabaseHelper databaseHelper;
    long userID;
    Calendar selectedMonth;
    SimpleDateFormat monthFormat;

    TextView txtMonthYear;
    TextView btnPreviousMonth;
    TextView btnNextMonth;
    TextView trainerDateMonthly;
    TextView trainerCaloriesMonthly;
    TextView trainerDurationMonthly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainee_track03);

        databaseHelper = new DatabaseHelper(this);
        Intent intent = getIntent();
        userID = intent.getLongExtra("userID", -1);

        monthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        selectedMonth = Calendar.getInstance();

        // Find views
        Button btnPlan = findViewById(R.id.btnPlanOfTrack2);
        Button btnProfile = findViewById(R.id.btnProfileOfTrack2);
        Button btnTrack = findViewById(R.id.btnTrackOfTrack2);
        ImageButton btnReminder = findViewById(R.id.btnReminder);
        ImageButton btnBack = findViewById(R.id.btnInfo);
        ImageButton btnMeal = findViewById(R.id.btnMeal);

        txtMonthYear = findViewById(R.id.txtMonthYear);
        btnPreviousMonth = findViewById(R.id.btnPreviousMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        trainerDateMonthly = findViewById(R.id.trainerDateMonthly);
        trainerCaloriesMonthly = findViewById(R.id.trainerCaloriesMonthly);
        trainerDurationMonthly = findViewById(R.id.trainerDurationMonthly);

        // Load initial data
        updateMonthDisplay();
        loadMonthlyStats();

        // Month navigation
        btnPreviousMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedMonth.add(Calendar.MONTH, -1);
                updateMonthDisplay();
                loadMonthlyStats();
            }
        });

        btnNextMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedMonth.add(Calendar.MONTH, 1);
                updateMonthDisplay();
                loadMonthlyStats();
            }
        });

        // Back button
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Navigation buttons
        btnPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TraineeTrackActivity03.this, TraineePlanActivity01.class);
                i.putExtra("userID", userID);
                startActivity(i);
            }
        });

        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TraineeTrackActivity03.this, TraineeProfileActivity.class);
                i.putExtra("userID", userID);
                startActivity(i);
            }
        });

        btnTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TraineeTrackActivity03.this, TraineeTrackActivity01.class);
                i.putExtra("userID", userID);
                startActivity(i);
            }
        });

        btnReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TraineeTrackActivity03.this, TraineeRemindActivity01.class);
                i.putExtra("userID", userID);
                startActivity(i);
            }
        });

        btnMeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TraineeTrackActivity03.this, TraineeTrackActivity04.class);
                i.putExtra("userID", userID);
                startActivity(i);
            }
        });
    }

    private void updateMonthDisplay() {
        String monthString = monthFormat.format(selectedMonth.getTime());
        txtMonthYear.setText(monthString);
    }

    @SuppressLint("Range")
    private void loadMonthlyStats() {
        try {
            String currentMonthString = monthFormat.format(selectedMonth.getTime());

            // Get first assigned plan date
            Cursor startDateCursor = databaseHelper.getReadableDatabase().rawQuery(
                    "SELECT assignedDate FROM AssignedPlan " +
                            "WHERE traineeID = ? " +
                            "ORDER BY assignedDate ASC LIMIT 1",
                    new String[]{String.valueOf(userID)}
            );

            if (startDateCursor != null && startDateCursor.moveToFirst()) {
                String startDate = startDateCursor.getString(startDateCursor.getColumnIndex("assignedDate"));
                if (startDate != null && startDate.length() >= 10) {
                    trainerDateMonthly.setText(startDate.substring(0, 10));
                } else {
                    trainerDateMonthly.setText("No workout started.");
                }
                startDateCursor.close();
            } else {
                trainerDateMonthly.setText("No workout started.");
                if (startDateCursor != null) startDateCursor.close();
            }

            // Calculate average calories burned per day for the month
            Cursor caloriesCursor = databaseHelper.getReadableDatabase().rawQuery(
                    "SELECT AVG(caloriesBurned) as avgCalories FROM WorkoutCompletion " +
                            "WHERE traineeID = ? AND completionDate LIKE ?",
                    new String[]{String.valueOf(userID), currentMonthString + "%"}
            );

            if (caloriesCursor != null && caloriesCursor.moveToFirst()) {
                double avgCalories = caloriesCursor.getDouble(caloriesCursor.getColumnIndex("avgCalories"));
                if (avgCalories > 0) {
                    trainerCaloriesMonthly.setText(String.format(Locale.getDefault(), "%.0f kcal", avgCalories));
                } else {
                    trainerCaloriesMonthly.setText("No data");
                }
                caloriesCursor.close();
            } else {
                trainerCaloriesMonthly.setText("No data");
            }

            // Calculate average duration per day for the month
            Cursor durationCursor = databaseHelper.getReadableDatabase().rawQuery(
                    "SELECT AVG(duration) as avgDuration FROM WorkoutCompletion " +
                            "WHERE traineeID = ? AND completionDate LIKE ?",
                    new String[]{String.valueOf(userID), currentMonthString + "%"}
            );

            if (durationCursor != null && durationCursor.moveToFirst()) {
                double avgDuration = durationCursor.getDouble(durationCursor.getColumnIndex("avgDuration"));
                if (avgDuration > 0) {
                    trainerDurationMonthly.setText(String.format(Locale.getDefault(), "%.0f minutes", avgDuration));
                } else {
                    trainerDurationMonthly.setText("No data");
                }
                durationCursor.close();
            } else {
                trainerDurationMonthly.setText("No data");
            }

        } catch (Exception e) {
            e.printStackTrace();
            trainerDateMonthly.setText("Error loading data");
            trainerCaloriesMonthly.setText("Error");
            trainerDurationMonthly.setText("Error");
        }
    }
}