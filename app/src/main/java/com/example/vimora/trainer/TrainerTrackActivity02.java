package com.example.vimora.trainer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
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

// this activity shows daily workout tracking details for a specific trainee
// the trainer can see what plan is assigned, calories burned, and workout duration
public class TrainerTrackActivity02 extends AppCompatActivity {
    // tag for logging debug messages
    private static final String TAG = "TrainerTrackActivity02";

    // database helper to query trainee data
    DatabaseHelper databaseHelper;
    // stores the logged-in trainer's id
    long trainerID;
    // stores the selected trainee's id
    long traineeID;
    // calendar object to track the currently selected date
    Calendar currentDate;
    // formats dates as yyyy-MM-dd for display and database queries
    SimpleDateFormat dateFormat;

    // ui elements to display trainee information
    TextView editTextDate;
    TextView tvTraineeName;
    TextView tvAssignedPlan;
    TextView tvCaloriesBurned;
    TextView tvDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // connect this activity to its layout file
        setContentView(R.layout.activity_trainer_track02);

        // initialize database helper
        databaseHelper = new DatabaseHelper(this);
        // get the trainer and trainee ids passed from previous screen
        Intent intent = getIntent();
        trainerID = intent.getLongExtra("userID", -1);
        traineeID = intent.getLongExtra("traineeID", -1);

        // initialize date format for displaying dates
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        // set current date to today
        currentDate = Calendar.getInstance();

        // find all the buttons and views in the layout
        Button btnProfile = findViewById(R.id.btnProfileOfTrack);
        Button btnPlan = findViewById(R.id.btnPlanOfTrack);
        Button btnTrack = findViewById(R.id.btnTrackOfTrack);
        ImageButton btnReminder = findViewById(R.id.btnReminder);
        ImageView btnBack = findViewById(R.id.imageView9);
        ImageView btnForward = findViewById(R.id.imageView6);
        ImageView btnPrevDate = findViewById(R.id.imageView10);
        ImageView btnNextDate = findViewById(R.id.imageView11);

        // find textviews that display trainee data
        editTextDate = findViewById(R.id.editTextDate);
        tvTraineeName = findViewById(R.id.textView14);
        tvAssignedPlan = findViewById(R.id.textView23);
        tvCaloriesBurned = findViewById(R.id.textView25);
        tvDuration = findViewById(R.id.textView27);

        // load the trainee's name from database
        loadTraineeName();

        // display today's date and load workout data for today
        updateDateDisplay();
        loadDailyData();

        // previous date button - go back one day
        btnPrevDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // subtract one day from current date
                currentDate.add(Calendar.DAY_OF_MONTH, -1);
                // update the date display
                updateDateDisplay();
                // reload data for the new date
                loadDailyData();
            }
        });

        // next date button - go forward one day
        btnNextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // add one day to current date
                currentDate.add(Calendar.DAY_OF_MONTH, 1);
                // update the date display
                updateDateDisplay();
                // reload data for the new date
                loadDailyData();
            }
        });

        // back button - return to trainee list
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TrainerTrackActivity02.this, TrainerTrackActivity01.class);
                i.putExtra("userID", trainerID);
                startActivity(i);
                finish();
            }
        });

        // forward button - go to monthly statistics view
        btnForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TrainerTrackActivity02.this, TrainerTrackActivity03.class);
                i.putExtra("userID", trainerID);
                i.putExtra("traineeID", traineeID);
                startActivity(i);
            }
        });

        // profile button - go to trainer profile
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TrainerTrackActivity02.this, TrainerProfileActivity1.class);
                i.putExtra("userID", trainerID);
                startActivity(i);
            }
        });

        // plan button - go to plan management
        btnPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TrainerTrackActivity02.this, TrainerPlanActivity1.class);
                i.putExtra("userID", trainerID);
                startActivity(i);
            }
        });

        // track button - go back to trainee list
        btnTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TrainerTrackActivity02.this, TrainerTrackActivity01.class);
                i.putExtra("userID", trainerID);
                startActivity(i);
            }
        });

        // reminder button - go to reminders
        btnReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TrainerTrackActivity02.this, TrainerRemindActivity01.class);
                i.putExtra("userID", trainerID);
                startActivity(i);
            }
        });
    }

    // loads the trainee's name from the database
    private void loadTraineeName() {
        try {
            // query database for the trainee's name
            String name = databaseHelper.getName(traineeID);
            // display the name, or "Unknown" if not found
            tvTraineeName.setText(name != null ? name : "Unknown");
        } catch (Exception e) {
            // if there's an error, display error message
            tvTraineeName.setText("Error loading name");
            e.printStackTrace();
        }
    }

    // updates the date display textview with the current selected date
    private void updateDateDisplay() {
        // format the date as yyyy-MM-dd string
        String dateString = dateFormat.format(currentDate.getTime());
        // display the formatted date
        editTextDate.setText(dateString);
    }

    // loads all workout data for the selected date
    // this includes assigned plan, calories burned, and workout duration
    @SuppressLint("Range")
    private void loadDailyData() {
        try {
            // get the current date as a string for database queries
            String currentDateString = dateFormat.format(currentDate.getTime());
            // log the date we're loading data for (helpful for debugging)
            Log.d(TAG, "Loading data for date: " + currentDateString);

            // first, run a debug query to see what's in the database
            // this helps us understand if there's a plan assigned at all
            Cursor debugCursor = databaseHelper.getReadableDatabase().rawQuery(
                    "SELECT ap.assignedDate, p.ExerciseName, p.workout_duration " +
                            "FROM AssignedPlan ap " +
                            "JOIN PlanTable p ON ap.planID = p.PlanID " +
                            "WHERE ap.traineeID = ? " +
                            "ORDER BY ap.assignedDate DESC LIMIT 1",
                    new String[]{String.valueOf(traineeID)}
            );

            // check if we found any plan assignment
            if (debugCursor != null && debugCursor.moveToFirst()) {
                // extract the plan details
                String assignedDate = debugCursor.getString(debugCursor.getColumnIndex("assignedDate"));
                String exerciseName = debugCursor.getString(debugCursor.getColumnIndex("ExerciseName"));
                String workoutDuration = debugCursor.getString(debugCursor.getColumnIndex("workout_duration"));

                // log the plan details for debugging
                Log.d(TAG, "Found plan: " + exerciseName);
                Log.d(TAG, "Assigned date: " + assignedDate);
                Log.d(TAG, "Duration: " + workoutDuration);

                debugCursor.close();
            }

            // now get the assigned plan, but only if it's valid for the current date
            // the plan is only valid if the current date falls within the duration period
            Cursor assignedPlanCursor = databaseHelper.getReadableDatabase().rawQuery(
                    "SELECT p.ExerciseName, ap.assignedDate, p.workout_duration FROM AssignedPlan ap " +
                            "JOIN PlanTable p ON ap.planID = p.PlanID " +
                            "WHERE ap.traineeID = ? " +
                            // make sure workout_duration is not null or empty
                            "AND p.workout_duration IS NOT NULL " +
                            "AND p.workout_duration != '' " +
                            // make sure it's a positive number
                            "AND CAST(p.workout_duration AS INTEGER) > 0 " +
                            // check if current date is within the plan's duration
                            // for example, if assigned on 2025-12-01 with duration 7,
                            // plan is valid from 2025-12-01 to 2025-12-07
                            "AND DATE(?) BETWEEN DATE(ap.assignedDate) AND DATE(ap.assignedDate, '+' || (CAST(p.workout_duration AS INTEGER) - 1) || ' days') " +
                            "ORDER BY ap.assignedDate DESC LIMIT 1",
                    new String[]{String.valueOf(traineeID), currentDateString}
            );

            // check if we found a valid plan for this date
            if (assignedPlanCursor != null && assignedPlanCursor.moveToFirst()) {
                // extract plan details
                String planName = assignedPlanCursor.getString(assignedPlanCursor.getColumnIndex("ExerciseName"));
                String assignedDate = assignedPlanCursor.getString(assignedPlanCursor.getColumnIndex("assignedDate"));
                String duration = assignedPlanCursor.getString(assignedPlanCursor.getColumnIndex("workout_duration"));

                // log that we found a valid plan
                Log.d(TAG, "Plan is valid for current date");
                Log.d(TAG, "Plan: " + planName + ", Assigned: " + assignedDate + ", Duration: " + duration);

                // display the plan name
                tvAssignedPlan.setText(planName);
                assignedPlanCursor.close();
            } else {
                // no valid plan found for this date
                Log.d(TAG, "No valid plan found for current date");
                tvAssignedPlan.setText("No plan assigned");
                if (assignedPlanCursor != null) assignedPlanCursor.close();
            }

            // get workout completion data for this date
            // this shows if the trainee actually did their workout
            Cursor workoutCursor = databaseHelper.getReadableDatabase().rawQuery(
                    "SELECT caloriesBurned, duration FROM WorkoutCompletion " +
                            "WHERE traineeID = ? AND completionDate = ?",
                    new String[]{String.valueOf(traineeID), currentDateString}
            );

            // check if trainee completed their workout on this date
            if (workoutCursor != null && workoutCursor.moveToFirst()) {
                // workout was completed, get the details
                int caloriesBurned = workoutCursor.getInt(workoutCursor.getColumnIndex("caloriesBurned"));
                int duration = workoutCursor.getInt(workoutCursor.getColumnIndex("duration"));

                // log the completion data
                Log.d(TAG, "Workout completed - Calories: " + caloriesBurned + ", Duration: " + duration);

                // display calories burned
                if (caloriesBurned > 0) {
                    // show actual calories if greater than 0
                    tvCaloriesBurned.setText(caloriesBurned + " kcal");
                } else {
                    // trainee marked complete but didn't enter calories
                    tvCaloriesBurned.setText("Completed (no calories)");
                }

                // display workout duration
                if (duration > 0) {
                    // show actual duration if greater than 0
                    tvDuration.setText(duration + " min");
                } else {
                    // trainee marked complete but didn't enter duration
                    tvDuration.setText("Completed (no duration)");
                }

                workoutCursor.close();
            } else {
                // workout was not completed on this date
                Log.d(TAG, "Workout not completed");
                tvCaloriesBurned.setText("Not completed");
                tvDuration.setText("Not completed");
                if (workoutCursor != null) workoutCursor.close();
            }

        } catch (Exception e) {
            // if any error occurs, log it and display error message
            Log.e(TAG, "Error loading daily data", e);
            tvAssignedPlan.setText("Error loading data");
            tvCaloriesBurned.setText("Error");
            tvDuration.setText("Error");
            e.printStackTrace();
        }
    }
}