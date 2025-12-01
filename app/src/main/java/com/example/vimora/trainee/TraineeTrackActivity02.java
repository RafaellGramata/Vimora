package com.example.vimora.trainee;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vimora.DatabaseHelper;
import com.example.vimora.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

// this activity lets trainees track their daily workouts
// they can see their assigned plan and enter calories burned and workout duration
public class TraineeTrackActivity02 extends AppCompatActivity {
    // database helper to query and save workout data
    DatabaseHelper databaseHelper;
    // stores the logged-in trainee's id
    long userID;
    // calendar object to track the selected date
    Calendar selectedDate;
    // formats dates as yyyy-MM-dd
    SimpleDateFormat dateFormat;

    // ui elements for date and workout tracking
    TextView editTextDateWorkout;
    TextView txtAssignedPlan;
    EditText editCalories;
    EditText editDuration;
    TextView btnPreviousDate;
    TextView btnNextDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // connect this activity to its layout file
        setContentView(R.layout.activity_trainee_track02);

        // initialize database helper
        databaseHelper = new DatabaseHelper(this);
        // get user id from previous screen
        Intent intent = getIntent();
        userID = intent.getLongExtra("userID", -1);

        // initialize date format
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        // set selected date to today
        selectedDate = Calendar.getInstance();

        // find all buttons and views
        Button btnPlan = findViewById(R.id.btnPlanOfTrack00);
        Button btnProfile = findViewById(R.id.btnProfileOfTrack00);
        Button btnTrack = findViewById(R.id.btnTrackOfTrack00);
        ImageButton btnReminder = findViewById(R.id.btnReminder);
        ImageButton btnBack = findViewById(R.id.btnInfo);
        ImageButton btnMeal = findViewById(R.id.btnMeal);
        ImageButton btnSave = findViewById(R.id.btnSaveMealTrack);
        ImageButton btnViewMore = findViewById(R.id.btnMoreMealTrack);

        // find workout tracking views
        editTextDateWorkout = findViewById(R.id.editTextDateWorkout);
        btnPreviousDate = findViewById(R.id.btnPreviousDate);
        btnNextDate = findViewById(R.id.btnNextDate);
        txtAssignedPlan = findViewById(R.id.txtAssignedPlan);
        editCalories = findViewById(R.id.editCalories);
        editDuration = findViewById(R.id.editDuration);

        // display today's date and load workout data
        updateDateDisplay();
        loadWorkoutData();

        // previous date button - go back one day
        btnPreviousDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // subtract one day
                selectedDate.add(Calendar.DAY_OF_MONTH, -1);
                updateDateDisplay();
                loadWorkoutData();
            }
        });

        // next date button - go forward one day
        btnNextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // add one day
                selectedDate.add(Calendar.DAY_OF_MONTH, 1);
                updateDateDisplay();
                loadWorkoutData();
            }
        });

        // back button - go back to calendar view
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // save button - save the workout completion data
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveWorkoutCompletion();
            }
        });

        // view more button - go to monthly statistics
        btnViewMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TraineeTrackActivity02.this, TraineeTrackActivity03.class);
                i.putExtra("userID", userID);
                startActivity(i);
            }
        });

        // navigation buttons - same as before
        btnPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TraineeTrackActivity02.this, TraineePlanActivity01.class);
                i.putExtra("userID", userID);
                startActivity(i);
            }
        });

        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TraineeTrackActivity02.this, TraineeProfileActivity.class);
                i.putExtra("userID", userID);
                startActivity(i);
            }
        });

        btnTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TraineeTrackActivity02.this, TraineeTrackActivity01.class);
                i.putExtra("userID", userID);
                startActivity(i);
            }
        });

        btnReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TraineeTrackActivity02.this, TraineeRemindActivity01.class);
                i.putExtra("userID", userID);
                startActivity(i);
            }
        });

        btnMeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TraineeTrackActivity02.this, TraineeTrackActivity04.class);
                i.putExtra("userID", userID);
                startActivity(i);
            }
        });
    }

    // updates the date display textview
    private void updateDateDisplay() {
        String dateString = dateFormat.format(selectedDate.getTime());
        editTextDateWorkout.setText(dateString);
    }

    // loads workout data for the selected date
    // shows assigned plan name and any existing workout completion data
    @SuppressLint("Range")
    private void loadWorkoutData() {
        try {
            // get current date as string
            String currentDateString = dateFormat.format(selectedDate.getTime());

            // get the assigned plan for this date
            // we use DATE() function to properly compare dates
            Cursor planCursor = databaseHelper.getReadableDatabase().rawQuery(
                    "SELECT p.ExerciseName FROM AssignedPlan ap " +
                            "JOIN PlanTable p ON ap.planID = p.PlanID " +
                            "WHERE ap.traineeID = ? AND DATE(ap.assignedDate) <= DATE(?) " +
                            "ORDER BY ap.assignedDate DESC LIMIT 1",
                    new String[]{String.valueOf(userID), currentDateString}
            );

            // check if we found a plan
            if (planCursor != null && planCursor.moveToFirst()) {
                // get the plan name
                String planName = planCursor.getString(planCursor.getColumnIndex("ExerciseName"));
                // display the plan name
                txtAssignedPlan.setText(planName != null ? planName : "No plan assigned");
                planCursor.close();
            } else {
                // no plan assigned yet
                txtAssignedPlan.setText("No plan assigned");
                if (planCursor != null) planCursor.close();
            }

            // check if trainee already completed workout for this date
            Cursor completionCursor = databaseHelper.getReadableDatabase().rawQuery(
                    "SELECT caloriesBurned, duration FROM WorkoutCompletion " +
                            "WHERE traineeID = ? AND completionDate = ?",
                    new String[]{String.valueOf(userID), currentDateString}
            );

            // if workout was already completed, load the saved data
            if (completionCursor != null && completionCursor.moveToFirst()) {
                // get saved calories and duration
                int calories = completionCursor.getInt(completionCursor.getColumnIndex("caloriesBurned"));
                int duration = completionCursor.getInt(completionCursor.getColumnIndex("duration"));

                // only show values if they're greater than 0
                if (calories > 0) {
                    editCalories.setText(String.valueOf(calories));
                } else {
                    editCalories.setText("");
                }

                if (duration > 0) {
                    editDuration.setText(String.valueOf(duration));
                } else {
                    editDuration.setText("");
                }

                completionCursor.close();
            } else {
                // no data saved yet, clear the fields
                editCalories.setText("");
                editDuration.setText("");
                if (completionCursor != null) completionCursor.close();
            }

        } catch (Exception e) {
            // if error occurs, log it and show error message
            e.printStackTrace();
            android.util.Log.e("TraineeTrackActivity02", "Error loading data: " + e.getMessage());
            Toast.makeText(this, "Error loading data: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // saves workout completion data to the database
    private void saveWorkoutCompletion() {
        try {
            // get current date as string
            String currentDateString = dateFormat.format(selectedDate.getTime());
            // get values from input fields
            String caloriesStr = editCalories.getText().toString().trim();
            String durationStr = editDuration.getText().toString().trim();

            // check if both fields are filled in
            if (caloriesStr.isEmpty() || durationStr.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // convert strings to integers
            int calories = Integer.parseInt(caloriesStr);
            int duration = Integer.parseInt(durationStr);

            // check if record already exists for this date
            Cursor cursor = databaseHelper.getReadableDatabase().rawQuery(
                    "SELECT completionID FROM WorkoutCompletion WHERE traineeID = ? AND completionDate = ?",
                    new String[]{String.valueOf(userID), currentDateString}
            );

            if (cursor != null && cursor.moveToFirst()) {
                // record exists - update it
                databaseHelper.getWritableDatabase().execSQL(
                        "UPDATE WorkoutCompletion SET caloriesBurned = ?, duration = ? " +
                                "WHERE traineeID = ? AND completionDate = ?",
                        new Object[]{calories, duration, userID, currentDateString}
                );
                cursor.close();
                Toast.makeText(this, "Workout updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                // record doesn't exist - insert new one
                // planID is set to NULL since we don't need it
                databaseHelper.getWritableDatabase().execSQL(
                        "INSERT INTO WorkoutCompletion (traineeID, planID, completionDate, caloriesBurned, duration) " +
                                "VALUES (?, NULL, ?, ?, ?)",
                        new Object[]{userID, currentDateString, calories, duration}
                );
                if (cursor != null) cursor.close();
                Toast.makeText(this, "Workout saved successfully", Toast.LENGTH_SHORT).show();
            }

        } catch (NumberFormatException e) {
            // user entered invalid numbers
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            // other error occurred
            e.printStackTrace();
            android.util.Log.e("TraineeTrackActivity02", "Error saving data: " + e.getMessage());
            Toast.makeText(this, "Error saving data: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}