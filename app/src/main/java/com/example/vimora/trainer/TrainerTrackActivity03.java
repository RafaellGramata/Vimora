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

// this activity shows monthly workout statistics for a trainee
// trainer can see workout start date, average calories, and workout frequency
public class TrainerTrackActivity03 extends AppCompatActivity {
    // database helper to query workout data
    DatabaseHelper databaseHelper;
    // stores the logged-in trainer's id
    long trainerID;
    // stores the selected trainee's id
    long traineeID;
    // calendar object to track the currently selected month
    Calendar currentMonth;
    // formats month as yyyy-MM for database queries
    SimpleDateFormat monthFormat;

    // ui elements to display monthly statistics
    TextView editTextDate;
    TextView tvTraineeName;
    TextView tvWorkoutStartDate;
    TextView tvAvgCalories;
    TextView tvAvgDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // connect this activity to its layout file
        setContentView(R.layout.activity_trainer_track03);

        // initialize database helper
        databaseHelper = new DatabaseHelper(this);
        // get trainer and trainee ids from previous screen
        Intent intent = getIntent();
        trainerID = intent.getLongExtra("userID", -1);
        traineeID = intent.getLongExtra("traineeID", -1);

        // initialize month format for display and queries
        monthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        // set current month to this month
        currentMonth = Calendar.getInstance();

        // find all buttons and views in the layout
        Button btnProfile = findViewById(R.id.btnProfileOfTrack);
        Button btnPlan = findViewById(R.id.btnPlanOfTrack);
        Button btnTrack = findViewById(R.id.btnTrackOfTrack);
        ImageButton btnReminder = findViewById(R.id.btnReminder);
        ImageView btnBack = findViewById(R.id.imageView9);
        ImageView btnForward = findViewById(R.id.imageView5);
        ImageView btnPrevMonth = findViewById(R.id.imageView10);
        ImageView btnNextMonth = findViewById(R.id.imageView11);

        // find textviews for displaying statistics
        editTextDate = findViewById(R.id.editTextDate);
        tvTraineeName = findViewById(R.id.textView14);
        tvWorkoutStartDate = findViewById(R.id.textView23);
        tvAvgCalories = findViewById(R.id.textView25);
        tvAvgDuration = findViewById(R.id.textView27);

        // load trainee's name
        loadTraineeName();

        // display current month and load statistics
        updateMonthDisplay();
        loadMonthlyData();

        // previous month button - go back one month
        btnPrevMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // subtract one month
                currentMonth.add(Calendar.MONTH, -1);
                // update month display
                updateMonthDisplay();
                // reload statistics for new month
                loadMonthlyData();
            }
        });

        // next month button - go forward one month
        btnNextMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // add one month
                currentMonth.add(Calendar.MONTH, 1);
                // update month display
                updateMonthDisplay();
                // reload statistics for new month
                loadMonthlyData();
            }
        });

        // back button - return to daily view
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

        // forward button - go to daily meal tracking
        btnForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TrainerTrackActivity03.this, TrainerTrackActivity04.class);
                i.putExtra("userID", trainerID);
                i.putExtra("traineeID", traineeID);
                startActivity(i);
            }
        });

        // navigation buttons - same as before
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

    // loads trainee's name from database
    private void loadTraineeName() {
        try {
            String name = databaseHelper.getName(traineeID);
            tvTraineeName.setText(name != null ? name : "Unknown");
        } catch (Exception e) {
            tvTraineeName.setText("Error loading name");
            e.printStackTrace();
        }
    }

    // updates the month display textview
    private void updateMonthDisplay() {
        // format month as yyyy-MM (ex: "2025-11")
        String monthString = monthFormat.format(currentMonth.getTime());
        editTextDate.setText(monthString);
    }

    // loads all monthly statistics for the selected month
    @SuppressLint("Range")
    private void loadMonthlyData() {
        try {
            // get current month as string for database queries
            String currentMonthString = monthFormat.format(currentMonth.getTime());

            // get the first date when a workout plan was assigned to this trainee
            // this shows when the trainee started their workout journey
            Cursor firstAssignmentCursor = databaseHelper.getReadableDatabase().rawQuery(
                    "SELECT assignedDate FROM AssignedPlan " +
                            "WHERE traineeID = ? " +
                            "ORDER BY assignedDate ASC LIMIT 1",
                    new String[]{String.valueOf(traineeID)}
            );

            // check if found an assignment
            if (firstAssignmentCursor != null && firstAssignmentCursor.moveToFirst()) {
                // get the assigned date
                String startDate = firstAssignmentCursor.getString(firstAssignmentCursor.getColumnIndex("assignedDate"));
                // extract just the date part (yyyy-MM-dd) and display it
                if (startDate != null && startDate.length() >= 10) {
                    tvWorkoutStartDate.setText(startDate.substring(0, 10));
                } else {
                    tvWorkoutStartDate.setText("N/A");
                }
                firstAssignmentCursor.close();
            } else {
                // no workout plan was ever assigned
                tvWorkoutStartDate.setText("No workout started.");
                if (firstAssignmentCursor != null) firstAssignmentCursor.close();
            }

            // calculate average daily calories for the month
            // use a subquery to first get total calories per day,
            // then calculate the average of those daily totals
            Cursor caloriesCursor = databaseHelper.getReadableDatabase().rawQuery(
                    "SELECT AVG(dailyTotal) as avgCalories FROM (" +
                            "  SELECT date, SUM(calories) as dailyTotal " +
                            "  FROM NutritionLog " +
                            "  WHERE traineeID = ? AND date LIKE ? " +
                            "  GROUP BY date" +
                            ")",
                    new String[]{String.valueOf(traineeID), currentMonthString + "%"}
            );

            // check if got any results
            if (caloriesCursor != null && caloriesCursor.moveToFirst()) {
                // get the average calories
                double avgCalories = caloriesCursor.getDouble(caloriesCursor.getColumnIndex("avgCalories"));
                if (avgCalories > 0) {
                    // display average with no decimal places
                    tvAvgCalories.setText(String.format(Locale.getDefault(), "%.0f kcal/day", avgCalories));
                } else {
                    tvAvgCalories.setText("No data");
                }
                caloriesCursor.close();
            } else {
                tvAvgCalories.setText("No data");
            }

            // calculate workout completion rate for the month
            // count how many unique days the trainee completed workouts
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

            // get total days in the selected month
            int daysInMonth = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH);

            // calculate workout frequency as a percentage
            double workoutFrequency = (completedDays * 100.0) / daysInMonth;
            // display as "completed/total days (percentage%)"
            tvAvgDuration.setText(String.format(Locale.getDefault(),
                    "%d/%d days (%.0f%%)", completedDays, daysInMonth, workoutFrequency));
        } catch (Exception e) {
            // if any error occurs, display error message
            tvWorkoutStartDate.setText("Error loading data");
            tvAvgCalories.setText("Error");
            tvAvgDuration.setText("Error");
            e.printStackTrace();
        }
    }
}