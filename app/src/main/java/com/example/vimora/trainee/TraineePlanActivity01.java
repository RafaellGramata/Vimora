package com.example.vimora.trainee;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.vimora.DatabaseHelper;
import com.example.vimora.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TraineePlanActivity01 extends AppCompatActivity {
    DatabaseHelper databaseHelper;
    Calendar date;
    TextView txtDate;
    TextView txtTrainingItem;
    TextView txtExercisePlanName;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    long userID;
    Button btnMarkComplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_trainee_plan01);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tvName), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseHelper = new DatabaseHelper(this);
        Intent intent = getIntent();
        userID = intent.getLongExtra("userID", -1);
        date = Calendar.getInstance();

        ImageButton btnReminder = findViewById(R.id.btnReminder);
        Button btnTrack = findViewById(R.id.btnTrackOfPlan);
        Button btnProfile = findViewById(R.id.btnProfileOfPlan);
        txtDate = findViewById(R.id.txtDate);
        txtTrainingItem = findViewById(R.id.txtTrianingItem);
        txtExercisePlanName = findViewById(R.id.txtExercisePlanName);
        btnMarkComplete = findViewById(R.id.btnMarkComplete);

        updateDate();
        loadPlanDetails();
        updateCompletionButton();

        Button previousDay = findViewById(R.id.btnPlanYesterday);
        Button nextDay = findViewById(R.id.btnPlanNextday);

        previousDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                date.add(Calendar.DAY_OF_MONTH, -1);
                updateDate();
                loadPlanDetails();
                updateCompletionButton();
            }
        });

        nextDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                date.add(Calendar.DAY_OF_MONTH, 1);
                updateDate();
                loadPlanDetails();
                updateCompletionButton();
            }
        });

        // Mark as Complete button functionality
        btnMarkComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String currentDate = sdf.format(date.getTime());

                // Check if already completed
                if (databaseHelper.isWorkoutCompletedOnDate(userID, currentDate)) {
                    // Already completed - do nothing (button is non-clickable in appearance)
                    Toast.makeText(TraineePlanActivity01.this,
                            "Already marked as complete",
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Not completed yet, mark as complete
                    long planID = getAssignedPlanID(currentDate);

                    if (planID > 0) {
                        boolean success = databaseHelper.markWorkoutComplete(userID, planID, currentDate);

                        if (success) {
                            Toast.makeText(TraineePlanActivity01.this,
                                    "Workout marked as complete! ✓",
                                    Toast.LENGTH_SHORT).show();
                            updateCompletionButton();
                        } else {
                            Toast.makeText(TraineePlanActivity01.this,
                                    "Error marking workout",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(TraineePlanActivity01.this,
                                "No plan assigned for this date",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        btnTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newIntent = new Intent(TraineePlanActivity01.this, TraineeTrackActivity01.class);
                newIntent.putExtra("userID", userID);
                startActivity(newIntent);
            }
        });

        btnReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newIntent = new Intent(TraineePlanActivity01.this, TraineeRemindActivity01.class);
                newIntent.putExtra("userID", userID);
                startActivity(newIntent);
            }
        });

        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newIntent = new Intent(TraineePlanActivity01.this, TraineeProfileActivity.class);
                newIntent.putExtra("userID", userID);
                startActivity(newIntent);
            }
        });
    }

    private void updateDate() {
        txtDate.setText(sdf.format(date.getTime()));
    }

    /**
     * Load and display the assigned plan details for the current date
     */
    @SuppressLint("Range")
    private void loadPlanDetails() {
        try {
            String currentDate = sdf.format(date.getTime());

            // Get assigned plan details for this date with duration check
            Cursor planCursor = databaseHelper.getReadableDatabase().rawQuery(
                    "SELECT p.*, ap.assignedDate FROM AssignedPlan ap " +
                            "JOIN PlanTable p ON ap.planID = p.PlanID " +
                            "WHERE ap.traineeID = ? " +
                            "AND DATE(?) BETWEEN DATE(ap.assignedDate) AND DATE(ap.assignedDate, '+' || (CAST(p.workout_duration AS INTEGER) - 1) || ' days') " +
                            "ORDER BY ap.assignedDate DESC LIMIT 1",
                    new String[]{String.valueOf(userID), currentDate}
            );

            if (planCursor != null && planCursor.moveToFirst()) {
                // Get and display the Exercise Plan Name
                String exerciseName = planCursor.getString(planCursor.getColumnIndex("ExerciseName"));
                txtExercisePlanName.setText(exerciseName != null ? exerciseName : "Exercise Plan");

                // Build the workout plan text
                StringBuilder planText = new StringBuilder();

                // Exercise 1
                String item1 = planCursor.getString(planCursor.getColumnIndex("item1"));
                String reps1 = planCursor.getString(planCursor.getColumnIndex("reps1"));
                String sets1 = planCursor.getString(planCursor.getColumnIndex("sets1"));

                if (item1 != null && !item1.isEmpty()) {
                    planText.append(item1).append("\n");
                    planText.append("Reps: ").append(reps1 != null ? reps1 : "0");
                    planText.append("     Sets: ").append(sets1 != null ? sets1 : "0").append("\n\n");
                }

                // Exercise 2
                String item2 = planCursor.getString(planCursor.getColumnIndex("item2"));
                String reps2 = planCursor.getString(planCursor.getColumnIndex("reps2"));
                String sets2 = planCursor.getString(planCursor.getColumnIndex("sets2"));

                if (item2 != null && !item2.isEmpty()) {
                    planText.append(item2).append("\n");
                    planText.append("Reps: ").append(reps2 != null ? reps2 : "0");
                    planText.append("     Sets: ").append(sets2 != null ? sets2 : "0").append("\n\n");
                }

                // Exercise 3
                String item3 = planCursor.getString(planCursor.getColumnIndex("item3"));
                String reps3 = planCursor.getString(planCursor.getColumnIndex("reps3"));
                String sets3 = planCursor.getString(planCursor.getColumnIndex("sets3"));

                if (item3 != null && !item3.isEmpty()) {
                    planText.append(item3).append("\n");
                    planText.append("Reps: ").append(reps3 != null ? reps3 : "0");
                    planText.append("     Sets: ").append(sets3 != null ? sets3 : "0").append("\n\n");
                }

                // Rest Duration and Workout Duration (Number of Days)
                String restDuration = planCursor.getString(planCursor.getColumnIndex("rest_duration"));
                String workoutDuration = planCursor.getString(planCursor.getColumnIndex("workout_duration"));

                if (restDuration != null && !restDuration.isEmpty()) {
                    planText.append("Rest Duration: ").append(restDuration).append(" min\n\n");
                }
                if (workoutDuration != null && !workoutDuration.isEmpty()) {
                    planText.append("Exercise Plan Duration: ").append(workoutDuration).append(" days");
                }

                txtTrainingItem.setText(planText.toString());
                planCursor.close();
            } else {
                txtExercisePlanName.setText("Exercise Plan");
                txtTrainingItem.setText("No workout plan assigned for this date.");
                if (planCursor != null) planCursor.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
            txtExercisePlanName.setText("Exercise Plan");
            txtTrainingItem.setText("Error loading plan");
            android.util.Log.e("TraineePlanActivity01", "Error loading plan: " + e.getMessage());
        }
    }

    /**
     * Get the assigned plan ID for a specific date
     */
    @SuppressLint("Range")
    private long getAssignedPlanID(String date) {
        try {
            Cursor cursor = databaseHelper.getReadableDatabase().rawQuery(
                    "SELECT ap.planID FROM AssignedPlan ap " +
                            "JOIN PlanTable p ON ap.planID = p.PlanID " +
                            "WHERE ap.traineeID = ? " +
                            "AND DATE(?) BETWEEN DATE(ap.assignedDate) AND DATE(ap.assignedDate, '+' || (CAST(p.workout_duration AS INTEGER) - 1) || ' days') " +
                            "ORDER BY ap.assignedDate DESC LIMIT 1",
                    new String[]{String.valueOf(userID), date}
            );

            if (cursor != null && cursor.moveToFirst()) {
                long planID = cursor.getLong(cursor.getColumnIndex("planID"));
                cursor.close();
                return planID;
            }
            if (cursor != null) cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1; // No plan found
    }

    /**
     * Update the completion button appearance and text
     */
    private void updateCompletionButton() {
        String currentDate = sdf.format(date.getTime());
        boolean isCompleted = databaseHelper.isWorkoutCompletedOnDate(userID, currentDate);

        if (isCompleted) {
            // Change to "Completed" - orange background (#FF6501)
            btnMarkComplete.setText("Completed ✓");
            btnMarkComplete.setBackgroundColor(0xFFFF6501); // Orange background
            btnMarkComplete.setTextColor(0xFFFFFFFF); // White text
            btnMarkComplete.setAlpha(0.7f); // Slightly transparent
            btnMarkComplete.setEnabled(true); // Still clickable but does nothing (shows toast)
        } else {
            // Show "Mark as Complete" - dark blue background (#005D9E)
            btnMarkComplete.setText("Mark as Complete");
            btnMarkComplete.setBackgroundColor(0xFF005D9E); // Dark blue background
            btnMarkComplete.setTextColor(0xFFFFFFFF); // White text
            btnMarkComplete.setAlpha(1.0f); // Fully opaque
            btnMarkComplete.setEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload plan details when returning to this activity
        loadPlanDetails();
        updateCompletionButton();
    }
}