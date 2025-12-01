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

// this activity shows monthly workout statistics for the trainee
// displays workout start date, average calories, and average duration
public class TraineeTrackActivity03 extends AppCompatActivity {
    // database helper to query workout data
    DatabaseHelper databaseHelper;
    // stores the logged-in trainee's id
    long userID;
    // calendar object to track the selected month
    Calendar selectedMonth;
    // formats month as yyyy-MM
    SimpleDateFormat monthFormat;

    // ui elements for displaying statistics
    TextView txtMonthYear;
    TextView btnPreviousMonth;
    TextView btnNextMonth;
    TextView trainerDateMonthly;
    TextView trainerCaloriesMonthly;
    TextView trainerDurationMonthly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // connect this activity to its layout file
        setContentView(R.layout.activity_trainee_track03);

        // initialize database helper
        databaseHelper = new DatabaseHelper(this);
        // get user id from previous screen
        Intent intent = getIntent();
        userID = intent.getLongExtra("userID", -1);

        // initialize month format
        monthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        // set selected month to current month
        selectedMonth = Calendar.getInstance();

        // find all buttons and views
        Button btnPlan = findViewById(R.id.btnPlanOfTrack2);
        Button btnProfile = findViewById(R.id.btnProfileOfTrack2);
        Button btnTrack = findViewById(R.id.btnTrackOfTrack2);
        ImageButton btnReminder = findViewById(R.id.btnReminder);
        ImageButton btnBack = findViewById(R.id.btnInfo);
        ImageButton btnMeal = findViewById(R.id.btnMeal);

        // find statistics display views
        txtMonthYear = findViewById(R.id.txtMonthYear);
        btnPreviousMonth = findViewById(R.id.btnPreviousMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        trainerDateMonthly = findViewById(R.id.trainerDateMonthly);
        trainerCaloriesMonthly = findViewById(R.id.trainerCaloriesMonthly);
        trainerDurationMonthly = findViewById(R.id.trainerDurationMonthly);

        // display current month and load statistics
        updateMonthDisplay();
        loadMonthlyStats();

        // previous month button - go back one month
        btnPreviousMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // subtract one month
                selectedMonth.add(Calendar.MONTH, -1);
                updateMonthDisplay();
                loadMonthlyStats();
            }
        });

        // next month button - go forward one month
        btnNextMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // add one month
                selectedMonth.add(Calendar.MONTH, 1);
                updateMonthDisplay();
                loadMonthlyStats();
            }
        });

        // back button - return to previous screen
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // navigation buttons - same as before
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

    // updates the month display textview
    private void updateMonthDisplay() {
        String monthString = monthFormat.format(selectedMonth.getTime());
        txtMonthYear.setText(monthString);
    }

    // loads monthly workout statistics
    @SuppressLint("Range")
    private void loadMonthlyStats() {
        try {
            // get current month as string
            String currentMonthString = monthFormat.format(selectedMonth.getTime());

            // get the first date when a plan was assigned to this trainee
            // this shows when they started their workout journey
            Cursor startDateCursor = databaseHelper.getReadableDatabase().rawQuery(
                    "SELECT assignedDate FROM AssignedPlan " +
                            "WHERE traineeID = ? " +
                            "ORDER BY assignedDate ASC LIMIT 1",
                    new String[]{String.valueOf(userID)}
            );

            if (startDateCursor != null && startDateCursor.moveToFirst()) {
                // get the start date
                String startDate = startDateCursor.getString(startDateCursor.getColumnIndex("assignedDate"));
                // extract just the date part (yyyy-MM-dd)
                if (startDate != null && startDate.length() >= 10) {
                    trainerDateMonthly.setText(startDate.substring(0, 10));
                } else {
                    trainerDateMonthly.setText("No workout started.");
                }
                startDateCursor.close();
            } else {
                // no plan was ever assigned
                trainerDateMonthly.setText("No workout started.");
                if (startDateCursor != null) startDateCursor.close();
            }

            // calculate average calories burned per workout day in this month
            Cursor caloriesCursor = databaseHelper.getReadableDatabase().rawQuery(
                    "SELECT AVG(caloriesBurned) as avgCalories FROM WorkoutCompletion " +
                            "WHERE traineeID = ? AND completionDate LIKE ?",
                    new String[]{String.valueOf(userID), currentMonthString + "%"}
            );

            if (caloriesCursor != null && caloriesCursor.moveToFirst()) {
                // get average calories
                double avgCalories = caloriesCursor.getDouble(caloriesCursor.getColumnIndex("avgCalories"));
                if (avgCalories > 0) {
                    // display with no decimal places
                    trainerCaloriesMonthly.setText(String.format(Locale.getDefault(), "%.0f kcal", avgCalories));
                } else {
                    trainerCaloriesMonthly.setText("No data");
                }
                caloriesCursor.close();
            } else {
                trainerCaloriesMonthly.setText("No data");
            }

            // calculate average workout duration per workout day in this month
            Cursor durationCursor = databaseHelper.getReadableDatabase().rawQuery(
                    "SELECT AVG(duration) as avgDuration FROM WorkoutCompletion " +
                            "WHERE traineeID = ? AND completionDate LIKE ?",
                    new String[]{String.valueOf(userID), currentMonthString + "%"}
            );

            if (durationCursor != null && durationCursor.moveToFirst()) {
                // get average duration
                double avgDuration = durationCursor.getDouble(durationCursor.getColumnIndex("avgDuration"));
                if (avgDuration > 0) {
                    // display with no decimal places
                    trainerDurationMonthly.setText(String.format(Locale.getDefault(), "%.0f minutes", avgDuration));
                } else {
                    trainerDurationMonthly.setText("No data");
                }
                durationCursor.close();
            } else {
                trainerDurationMonthly.setText("No data");
            }

        } catch (Exception e) {
            // if error occurs, display error message
            e.printStackTrace();
            trainerDateMonthly.setText("Error loading data");
            trainerCaloriesMonthly.setText("Error");
            trainerDurationMonthly.setText("Error");
        }
    }
}