package com.example.vimora.trainee;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
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

public class TraineeTrackActivity02 extends AppCompatActivity {
    DatabaseHelper databaseHelper;
    long userID;
    Calendar selectedDate;
    SimpleDateFormat dateFormat;

    TextView editTextDateWorkout;
    TextView txtAssignedPlan;
    EditText editCalories;
    EditText editDuration;
    TextView btnPreviousDate;
    TextView btnNextDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainee_track02);

        databaseHelper = new DatabaseHelper(this);
        Intent intent = getIntent();
        userID = intent.getLongExtra("userID", -1);

        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        selectedDate = Calendar.getInstance();

        // Find views
        Button btnPlan = findViewById(R.id.btnPlanOfTrack00);
        Button btnProfile = findViewById(R.id.btnProfileOfTrack00);
        Button btnTrack = findViewById(R.id.btnTrackOfTrack00);
        ImageButton btnReminder = findViewById(R.id.btnReminder);
        ImageButton btnBack = findViewById(R.id.btnInfo);
        ImageButton btnMeal = findViewById(R.id.btnMeal);
        ImageButton btnSave = findViewById(R.id.btnSaveMealTrack);
        ImageButton btnViewMore = findViewById(R.id.btnMoreMealTrack);

        editTextDateWorkout = findViewById(R.id.editTextDateWorkout);
        btnPreviousDate = findViewById(R.id.btnPreviousDate);
        btnNextDate = findViewById(R.id.btnNextDate);
        txtAssignedPlan = findViewById(R.id.txtAssignedPlan);
        editCalories = findViewById(R.id.editCalories);
        editDuration = findViewById(R.id.editDuration);

        // Load initial data
        updateDateDisplay();
        loadWorkoutData();

        // Date navigation
        btnPreviousDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedDate.add(Calendar.DAY_OF_MONTH, -1);
                updateDateDisplay();
                loadWorkoutData();
            }
        });

        btnNextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedDate.add(Calendar.DAY_OF_MONTH, 1);
                updateDateDisplay();
                loadWorkoutData();
            }
        });

        // Back button
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Save button
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveWorkoutCompletion();
            }
        });

        // View More button - go to Track03
        btnViewMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TraineeTrackActivity02.this, TraineeTrackActivity03.class);
                i.putExtra("userID", userID);
                startActivity(i);
            }
        });

        // Navigation buttons
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

    private void updateDateDisplay() {
        String dateString = dateFormat.format(selectedDate.getTime());
        editTextDateWorkout.setText(dateString);
    }

    @SuppressLint("Range")
    private void loadWorkoutData() {
        try {
            String currentDateString = dateFormat.format(selectedDate.getTime());

            // Get assigned plan for this date
            Cursor planCursor = databaseHelper.getReadableDatabase().rawQuery(
                    "SELECT p.planName FROM AssignedPlan ap " +
                            "JOIN PlanTable p ON ap.planID = p.planID " +
                            "WHERE ap.traineeID = ? AND ap.assignedDate <= ? " +
                            "ORDER BY ap.assignedDate DESC LIMIT 1",
                    new String[]{String.valueOf(userID), currentDateString}
            );

            if (planCursor != null && planCursor.moveToFirst()) {
                String planName = planCursor.getString(planCursor.getColumnIndex("planName"));
                txtAssignedPlan.setText(planName != null ? planName : "No plan assigned");
                planCursor.close();
            } else {
                txtAssignedPlan.setText("No plan assigned");
                if (planCursor != null) planCursor.close();
            }

            // Get existing workout completion data for this date
            Cursor completionCursor = databaseHelper.getReadableDatabase().rawQuery(
                    "SELECT caloriesBurned, duration FROM WorkoutCompletion " +
                            "WHERE traineeID = ? AND completionDate = ?",
                    new String[]{String.valueOf(userID), currentDateString}
            );

            if (completionCursor != null && completionCursor.moveToFirst()) {
                int calories = completionCursor.getInt(completionCursor.getColumnIndex("caloriesBurned"));
                int duration = completionCursor.getInt(completionCursor.getColumnIndex("duration"));

                editCalories.setText(String.valueOf(calories));
                editDuration.setText(String.valueOf(duration));
                completionCursor.close();
            } else {
                editCalories.setText("");
                editDuration.setText("");
                if (completionCursor != null) completionCursor.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveWorkoutCompletion() {
        try {
            String currentDateString = dateFormat.format(selectedDate.getTime());
            String caloriesStr = editCalories.getText().toString().trim();
            String durationStr = editDuration.getText().toString().trim();

            if (caloriesStr.isEmpty() || durationStr.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            int calories = Integer.parseInt(caloriesStr);
            int duration = Integer.parseInt(durationStr);

            // Check if record exists
            Cursor cursor = databaseHelper.getReadableDatabase().rawQuery(
                    "SELECT completionID FROM WorkoutCompletion WHERE traineeID = ? AND completionDate = ?",
                    new String[]{String.valueOf(userID), currentDateString}
            );

            if (cursor != null && cursor.moveToFirst()) {
                // Update existing record
                databaseHelper.getWritableDatabase().execSQL(
                        "UPDATE WorkoutCompletion SET caloriesBurned = ?, duration = ? " +
                                "WHERE traineeID = ? AND completionDate = ?",
                        new Object[]{calories, duration, userID, currentDateString}
                );
                cursor.close();
                Toast.makeText(this, "Workout updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                // Insert new record
                databaseHelper.getWritableDatabase().execSQL(
                        "INSERT INTO WorkoutCompletion (traineeID, completionDate, caloriesBurned, duration) " +
                                "VALUES (?, ?, ?, ?)",
                        new Object[]{userID, currentDateString, calories, duration}
                );
                if (cursor != null) cursor.close();
                Toast.makeText(this, "Workout saved successfully", Toast.LENGTH_SHORT).show();
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving data", Toast.LENGTH_SHORT).show();
        }
    }
}