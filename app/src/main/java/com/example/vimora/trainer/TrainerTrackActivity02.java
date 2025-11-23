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

public class TrainerTrackActivity02 extends AppCompatActivity {
    DatabaseHelper databaseHelper;
    long trainerID;
    long traineeID;
    Calendar currentDate;
    SimpleDateFormat dateFormat;

    TextView editTextDate;  // Changed from EditText to TextView
    TextView tvTraineeName;
    TextView tvAssignedPlan;
    TextView tvCaloriesBurned;
    TextView tvDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer_track02);

        databaseHelper = new DatabaseHelper(this);
        Intent intent = getIntent();
        trainerID = intent.getLongExtra("userID", -1);
        traineeID = intent.getLongExtra("traineeID", -1);

        // Initialize date format
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        currentDate = Calendar.getInstance();

        // Find views
        Button btnProfile = findViewById(R.id.btnProfileOfTrack);
        Button btnPlan = findViewById(R.id.btnPlanOfTrack);
        Button btnTrack = findViewById(R.id.btnTrackOfTrack);
        ImageButton btnReminder = findViewById(R.id.btnReminder);
        ImageView btnBack = findViewById(R.id.imageView9);
        ImageView btnForward = findViewById(R.id.imageView6);
        ImageView btnPrevDate = findViewById(R.id.imageView10);
        ImageView btnNextDate = findViewById(R.id.imageView11);

        editTextDate = findViewById(R.id.editTextDate);
        tvTraineeName = findViewById(R.id.textView14);
        tvAssignedPlan = findViewById(R.id.textView23);
        tvCaloriesBurned = findViewById(R.id.textView25);
        tvDuration = findViewById(R.id.textView27);

        // Load trainee name
        loadTraineeName();

        // Load data for current date
        updateDateDisplay();
        loadDailyData();

        // Date navigation
        btnPrevDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentDate.add(Calendar.DAY_OF_MONTH, -1);
                updateDateDisplay();
                loadDailyData();
            }
        });

        btnNextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentDate.add(Calendar.DAY_OF_MONTH, 1);
                updateDateDisplay();
                loadDailyData();
            }
        });

        // Back button - return to trainee list
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TrainerTrackActivity02.this, TrainerTrackActivity01.class);
                i.putExtra("userID", trainerID);
                startActivity(i);
                finish();
            }
        });

        // Forward button - go to monthly view
        btnForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TrainerTrackActivity02.this, TrainerTrackActivity03.class);
                i.putExtra("userID", trainerID);
                i.putExtra("traineeID", traineeID);
                startActivity(i);
            }
        });

        // Navigation buttons
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TrainerTrackActivity02.this, TrainerProfileActivity1.class);
                i.putExtra("userID", trainerID);
                startActivity(i);
            }
        });

        btnPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TrainerTrackActivity02.this, TrainerPlanActivity1.class);
                i.putExtra("userID", trainerID);
                startActivity(i);
            }
        });

        btnTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TrainerTrackActivity02.this, TrainerTrackActivity01.class);
                i.putExtra("userID", trainerID);
                startActivity(i);
            }
        });

        btnReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TrainerTrackActivity02.this, TrainerRemindActivity01.class);
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

    private void updateDateDisplay() {
        String dateString = dateFormat.format(currentDate.getTime());
        editTextDate.setText(dateString);
    }

    @SuppressLint("Range")
    private void loadDailyData() {
        try {
            String currentDateString = dateFormat.format(currentDate.getTime());

            // Get assigned plan for this trainee
            Cursor assignedPlanCursor = databaseHelper.getReadableDatabase().rawQuery(
                    "SELECT p.ExerciseName FROM AssignedPlan ap " +
                            "JOIN PlanTable p ON ap.planID = p.PlanID " +
                            "WHERE ap.traineeID = ? " +
                            "ORDER BY ap.assignedDate DESC LIMIT 1",
                    new String[]{String.valueOf(traineeID)}
            );

            if (assignedPlanCursor != null && assignedPlanCursor.moveToFirst()) {
                String planName = assignedPlanCursor.getString(assignedPlanCursor.getColumnIndex("ExerciseName"));
                tvAssignedPlan.setText(planName);
                assignedPlanCursor.close();
            } else {
                tvAssignedPlan.setText("No plan assigned");
                if (assignedPlanCursor != null) assignedPlanCursor.close();
            }

            // Get nutrition data for the selected date
            Cursor nutritionCursor = databaseHelper.getReadableDatabase().rawQuery(
                    "SELECT SUM(calories) as totalCalories FROM NutritionLog " +
                            "WHERE traineeID = ? AND date = ?",
                    new String[]{String.valueOf(traineeID), currentDateString}
            );

            int totalCalories = 0;
            if (nutritionCursor != null && nutritionCursor.moveToFirst()) {
                totalCalories = nutritionCursor.getInt(nutritionCursor.getColumnIndex("totalCalories"));
                nutritionCursor.close();
            }

            tvCaloriesBurned.setText(totalCalories > 0 ? totalCalories + " kcal" : "No data");

            // Check if workout was completed on this date
            boolean workoutCompleted = databaseHelper.isWorkoutCompletedOnDate(traineeID, currentDateString);

            if (workoutCompleted) {
                tvDuration.setText("Completed");
            } else {
                tvDuration.setText("Not completed");
            }
        } catch (Exception e) {
            tvAssignedPlan.setText("Error loading data");
            tvCaloriesBurned.setText("Error");
            tvDuration.setText("Error");
            e.printStackTrace();
        }
    }
}