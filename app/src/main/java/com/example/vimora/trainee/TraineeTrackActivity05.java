package com.example.vimora.trainee;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vimora.DatabaseHelper;
import com.example.vimora.NutritionDatabaseHelper;
import com.example.vimora.R;
import com.example.vimora.WelcomeActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

public class TraineeTrackActivity05 extends AppCompatActivity {

    private EditText traineeCaloriesAverage, traineeProteinAverage, traineeFatAverage;
    private ImageButton btnInfo, btnReminder;
    private Button btnPlan, btnProfile, btnTrack;

    private DatabaseHelper dbHelper;
    private NutritionDatabaseHelper nutritionHelper;
    private long traineeID;
    private String currentYearMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainee_track05);

        // Initialize database helpers
        dbHelper = new DatabaseHelper(this);
        nutritionHelper = new NutritionDatabaseHelper(dbHelper);

        // Get trainee ID from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("VimoraPrefs", MODE_PRIVATE);
        traineeID = prefs.getLong("userID", -1);

        if (traineeID == -1) {
            Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        initializeViews();

        // Get current month
        currentYearMonth = getCurrentYearMonth();

        // Load monthly statistics
        loadMonthlyStatistics();

        // Set up listeners
        setupListeners();
    }

    private void initializeViews() {
        traineeCaloriesAverage = findViewById(R.id.traineeCaloriesAverage);
        traineeProteinAverage = findViewById(R.id.traineeProteinAverage);
        traineeFatAverage = findViewById(R.id.traineeFatAverage);

        btnInfo = findViewById(R.id.btnInfo4);
        btnReminder = findViewById(R.id.btnReminder);

        btnPlan = findViewById(R.id.btnPlanOfTrack4);
        btnProfile = findViewById(R.id.btnProfileOfTrack4);
        btnTrack = findViewById(R.id.btnTrackOfTrack4);

        // Make fields read-only (display only)
        traineeCaloriesAverage.setFocusable(false);
        traineeCaloriesAverage.setClickable(false);

        traineeProteinAverage.setFocusable(false);
        traineeProteinAverage.setClickable(false);

        traineeFatAverage.setFocusable(false);
        traineeFatAverage.setClickable(false);
    }

    private void setupListeners() {
        // Back button - go back to daily tracking
        btnInfo.setOnClickListener(v -> {
            Intent intent = new Intent(TraineeTrackActivity05.this, TraineeTrackActivity04.class);
            startActivity(intent);
            finish();
        });

        // Navigation buttons
        btnPlan.setOnClickListener(v -> {
            Intent intent = new Intent(TraineeTrackActivity05.this, TraineePlanActivity01.class);
            intent.putExtra("userID", traineeID);
            startActivity(intent);
        });

        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(TraineeTrackActivity05.this, TraineeProfileActivity.class);
            intent.putExtra("userID", traineeID);
            startActivity(intent);
        });

        btnTrack.setOnClickListener(v -> {
            // Go back to daily meal tracking view
            Intent intent = new Intent(TraineeTrackActivity05.this, TraineeTrackActivity04.class);
            startActivity(intent);
            finish();
        });


        // Reminder button
        btnReminder.setOnClickListener(v -> {
            Intent intent = new Intent(TraineeTrackActivity05.this, TraineeRemindActivity.class);
            intent.putExtra("userID", traineeID);
            startActivity(intent);
        });
    }

    private void loadMonthlyStatistics() {
        // Get monthly averages from database
        Map<String, Double> averages = nutritionHelper.getMonthlyAverage(traineeID, currentYearMonth);

        // Display the averages
        double avgCalories = averages.get("avgCalories");
        double avgProtein = averages.get("avgProtein");
        double avgFat = averages.get("avgTotalFat");

        if (avgCalories > 0) {
            traineeCaloriesAverage.setText(String.format(Locale.getDefault(),
                    "%.0f kcal/day", avgCalories));
            traineeProteinAverage.setText(String.format(Locale.getDefault(),
                    "%.1f g/day", avgProtein));
            traineeFatAverage.setText(String.format(Locale.getDefault(),
                    "%.1f g/day", avgFat));
        } else {
            traineeCaloriesAverage.setText("No data for this month");
            traineeProteinAverage.setText("No data for this month");
            traineeFatAverage.setText("No data for this month");
        }
    }

    private String getCurrentYearMonth() {
        // Returns current month in format "yyyy-MM" (e.g., "2025-10")
        return new SimpleDateFormat("yyyy-MM", Locale.getDefault())
                .format(Calendar.getInstance().getTime());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload statistics when returning to this screen
        loadMonthlyStatistics();
    }
}