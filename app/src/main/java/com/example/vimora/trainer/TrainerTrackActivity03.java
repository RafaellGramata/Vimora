package com.example.vimora.trainer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vimora.DatabaseHelper;
import com.example.vimora.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TrainerTrackActivity03 extends AppCompatActivity {
    DatabaseHelper databaseHelper;
    long trainerID;
    long traineeID;
    Calendar currentMonth;
    SimpleDateFormat monthFormat;

    TextView editTextDate;  // Changed from EditText to TextView
    TextView tvTraineeName;
    TextView tvWorkoutStartDate;
    TextView tvAvgCalories;
    TextView tvAvgDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer_track03);

        databaseHelper = new DatabaseHelper(this);
        Intent intent = getIntent();
        trainerID = intent.getLongExtra("userID", -1);
        traineeID = intent.getLongExtra("traineeID", -1);

        // Initialize date format for month display
        monthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        currentMonth = Calendar.getInstance();

        // Find views
        Button btnProfile = findViewById(R.id.btnProfileOfTrack);
        Button btnPlan = findViewById(R.id.btnPlanOfTrack);
        Button btnTrack = findViewById(R.id.btnTrackOfTrack);
        ImageButton btnReminder = findViewById(R.id.btnReminder);
        ImageView btnBack = findViewById(R.id.imageView9);
        ImageView btnForward = findViewById(R.id.imageView5);
        ImageView btnPrevMonth = findViewById(R.id.imageView10);
        ImageView btnNextMonth = findViewById(R.id.imageView11);

        editTextDate = findViewById(R.id.editTextDate);
        tvTraineeName = findViewById(R.id.textView14);
        tvWorkoutStartDate = findViewById(R.id.textView23);
        tvAvgCalories = findViewById(R.id.textView25);
        tvAvgDuration = findViewById(R.id.textView27);

        // Load trainee name
        loadTraineeName();

        // Load data for current month
        updateMonthDisplay();
        loadMonthlyData();

        // Month navigation
        btnPrevMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth.add(Calendar.MONTH, -1);
                updateMonthDisplay();
                loadMonthlyData();
            }
        });

        btnNextMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth.add(Calendar.MONTH, 1);
                updateMonthDisplay();
                loadMonthlyData();
            }
        });

        // Back button - return to daily view
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TrainerTrackActivity03.this, TrainerTrackActivity02.class);
                i.putExtra("userID", trainerID);
                i.putExtra("traineeID", traineeID);
                startActivity(i);
                finish();
            }
        });

        // Forward button - go to daily meal tracking (Activity04)
        btnForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TrainerTrackActivity03.this, TrainerTrackActivity04.class);
                i.putExtra("userID", trainerID);
                i.putExtra("traineeID", traineeID);
                startActivity(i);
            }
        });

        // Navigation buttons
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TrainerTrackActivity03.this, TrainerProfileActivity1.class);
                i.putExtra("userID", trainerID);
                startActivity(i);
            }
        });

        btnPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TrainerTrackActivity03.this, TrainerPlanActivity1.class);
                i.putExtra("userID", trainerID);
                startActivity(i);
            }
        });

        btnTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TrainerTrackActivity03.this, TrainerTrackActivity01.class);
                i.putExtra("userID", trainerID);
                startActivity(i);
            }
        });

        btnReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TrainerTrackActivity03.this, TrainerRemindActivity01.class);
                i.putExtra("userID", trainerID);
                startActivity(i);
            }
        });
    }

    private void loadTraineeName() {
        try {
            String name = databaseHelper.getName(traineeID);
            tvTraineeName.setText(name != null ? name : "Unknown");
        } catch (Exception e) {
            tvTraineeName.setText("Error loading name");
            e.printStackTrace();
        }
    }

    private void updateMonthDisplay() {
        String monthString = monthFormat.format(currentMonth.getTime());
        editTextDate.setText(monthString);
    }

    @SuppressLint("Range")
    private void loadMonthlyData() {
        try {
            String currentMonthString = monthFormat.format(currentMonth.getTime());

            // Get the first workout/plan assignment date for this trainee
            Cursor firstAssignmentCursor = databaseHelper.getReadableDatabase().rawQuery(
                    "SELECT assignedDate FROM AssignedPlan " +
                            "WHERE traineeID = ? " +
                            "ORDER BY assignedDate ASC LIMIT 1",
                    new String[]{String.valueOf(traineeID)}
            );

            if (firstAssignmentCursor != null && firstAssignmentCursor.moveToFirst()) {
                String startDate = firstAssignmentCursor.getString(firstAssignmentCursor.getColumnIndex("assignedDate"));
                // Extract just the date part (yyyy-MM-dd)
                if (startDate != null && startDate.length() >= 10) {
                    tvWorkoutStartDate.setText(startDate.substring(0, 10));
                } else {
                    tvWorkoutStartDate.setText("N/A");
                }
                firstAssignmentCursor.close();
            } else {
                tvWorkoutStartDate.setText("No workout started");
                if (firstAssignmentCursor != null) firstAssignmentCursor.close();
            }

            // Calculate average daily calories for the month
            Cursor caloriesCursor = databaseHelper.getReadableDatabase().rawQuery(
                    "SELECT AVG(dailyTotal) as avgCalories FROM (" +
                            "  SELECT date, SUM(calories) as dailyTotal " +
                            "  FROM NutritionLog " +
                            "  WHERE traineeID = ? AND date LIKE ? " +
                            "  GROUP BY date" +
                            ")",
                    new String[]{String.valueOf(traineeID), currentMonthString + "%"}
            );

            if (caloriesCursor != null && caloriesCursor.moveToFirst()) {
                double avgCalories = caloriesCursor.getDouble(caloriesCursor.getColumnIndex("avgCalories"));
                if (avgCalories > 0) {
                    tvAvgCalories.setText(String.format(Locale.getDefault(), "%.0f kcal/day", avgCalories));
                } else {
                    tvAvgCalories.setText("No data");
                }
                caloriesCursor.close();
            } else {
                tvAvgCalories.setText("No data");
            }

            // Calculate workout completion rate for the month
            Cursor completionCursor = databaseHelper.getReadableDatabase().rawQuery(
                    "SELECT COUNT(DISTINCT completionDate) as completedDays " +
                            "FROM WorkoutCompletion " +
                            "WHERE traineeID = ? AND completionDate LIKE ?",
                    new String[]{String.valueOf(traineeID), currentMonthString + "%"}
            );

            int completedDays = 0;
            if (completionCursor != null && completionCursor.moveToFirst()) {
                completedDays = completionCursor.getInt(completionCursor.getColumnIndex("completedDays"));
                completionCursor.close();
            }

            // Get total days in the month
            int daysInMonth = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH);

            // Calculate average "duration" as workout frequency
            double workoutFrequency = (completedDays * 100.0) / daysInMonth;
            tvAvgDuration.setText(String.format(Locale.getDefault(),
                    "%d/%d days (%.0f%%)", completedDays, daysInMonth, workoutFrequency));
        } catch (Exception e) {
            tvWorkoutStartDate.setText("Error loading data");
            tvAvgCalories.setText("Error");
            tvAvgDuration.setText("Error");
            e.printStackTrace();
        }
    }
}