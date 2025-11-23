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

public class TrainerTrackActivity05 extends AppCompatActivity {
    DatabaseHelper databaseHelper;
    long trainerID;
    long traineeID;
    Calendar currentMonth;
    SimpleDateFormat monthFormat;

    TextView tvAvgCalories;
    TextView tvAvgProtein;
    TextView tvAvgFat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer_track05);

        databaseHelper = new DatabaseHelper(this);
        Intent intent = getIntent();
        trainerID = intent.getLongExtra("userID", -1);
        traineeID = intent.getLongExtra("traineeID", -1);

        // Initialize date format for month
        monthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        currentMonth = Calendar.getInstance();

        // Find views
        Button btnProfile = findViewById(R.id.btnProfileOfTrack);
        Button btnPlan = findViewById(R.id.btnPlanOfTrack);
        Button btnTrack = findViewById(R.id.btnTrackOfTrack);
        ImageButton btnReminder = findViewById(R.id.btnReminder);
        ImageView btnBack = findViewById(R.id.imageView9);
        ImageView btnForward = findViewById(R.id.imageView6);

        tvAvgCalories = findViewById(R.id.tvAvgCalories);
        tvAvgProtein = findViewById(R.id.tvAvgProtein);
        tvAvgFat = findViewById(R.id.tvAvgFat);

        // Load monthly data
        loadMonthlyNutritionData();

        // Back button - return to daily meal track (Activity04)
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TrainerTrackActivity05.this, TrainerTrackActivity04.class);
                i.putExtra("userID", trainerID);
                i.putExtra("traineeID", traineeID);
                startActivity(i);
                finish();
            }
        });

        // Forward button - also return to trainee list (Activity01)
        btnForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TrainerTrackActivity05.this, TrainerTrackActivity01.class);
                i.putExtra("userID", trainerID);
                startActivity(i);
                finish();
            }
        });

        // Navigation buttons
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TrainerTrackActivity05.this, TrainerProfileActivity1.class);
                i.putExtra("userID", trainerID);
                startActivity(i);
            }
        });

        btnPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TrainerTrackActivity05.this, TrainerPlanActivity1.class);
                i.putExtra("userID", trainerID);
                startActivity(i);
            }
        });

        btnTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TrainerTrackActivity05.this, TrainerTrackActivity01.class);
                i.putExtra("userID", trainerID);
                startActivity(i);
            }
        });

        btnReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TrainerTrackActivity05.this, TrainerRemindActivity01.class);
                i.putExtra("userID", trainerID);
                startActivity(i);
            }
        });
    }

    @SuppressLint("Range")
    private void loadMonthlyNutritionData() {
        try {
            String currentMonthString = monthFormat.format(currentMonth.getTime());

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
                    tvAvgCalories.setText("0 kcal/day");
                }
                caloriesCursor.close();
            } else {
                tvAvgCalories.setText("0 kcal/day");
            }

            // Calculate average daily protein for the month
            Cursor proteinCursor = databaseHelper.getReadableDatabase().rawQuery(
                    "SELECT AVG(dailyTotal) as avgProtein FROM (" +
                            "  SELECT date, SUM(protein) as dailyTotal " +
                            "  FROM NutritionLog " +
                            "  WHERE traineeID = ? AND date LIKE ? " +
                            "  GROUP BY date" +
                            ")",
                    new String[]{String.valueOf(traineeID), currentMonthString + "%"}
            );

            if (proteinCursor != null && proteinCursor.moveToFirst()) {
                double avgProtein = proteinCursor.getDouble(proteinCursor.getColumnIndex("avgProtein"));
                if (avgProtein > 0) {
                    tvAvgProtein.setText(String.format(Locale.getDefault(), "%.1f g/day", avgProtein));
                } else {
                    tvAvgProtein.setText("0.0 g/day");
                }
                proteinCursor.close();
            } else {
                tvAvgProtein.setText("0.0 g/day");
            }

            // Calculate average daily fat for the month
            Cursor fatCursor = databaseHelper.getReadableDatabase().rawQuery(
                    "SELECT AVG(dailyTotal) as avgFat FROM (" +
                            "  SELECT date, SUM(totalFat) as dailyTotal " +
                            "  FROM NutritionLog " +
                            "  WHERE traineeID = ? AND date LIKE ? " +
                            "  GROUP BY date" +
                            ")",
                    new String[]{String.valueOf(traineeID), currentMonthString + "%"}
            );

            if (fatCursor != null && fatCursor.moveToFirst()) {
                double avgFat = fatCursor.getDouble(fatCursor.getColumnIndex("avgFat"));
                if (avgFat > 0) {
                    tvAvgFat.setText(String.format(Locale.getDefault(), "%.1f g/day", avgFat));
                } else {
                    tvAvgFat.setText("0.0 g/day");
                }
                fatCursor.close();
            } else {
                tvAvgFat.setText("0.0 g/day");
            }

        } catch (Exception e) {
            e.printStackTrace();
            tvAvgCalories.setText("Error loading data");
            tvAvgProtein.setText("Error loading data");
            tvAvgFat.setText("Error loading data");
        }
    }
}