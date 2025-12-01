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

// this activity shows monthly nutrition averages for a trainee
// displays average calories, protein, and fat per day for the month
public class TrainerTrackActivity05 extends AppCompatActivity {
    // database helper to query nutrition data
    DatabaseHelper databaseHelper;
    // stores the logged-in trainer's id
    long trainerID;
    // stores the selected trainee's id
    long traineeID;
    // calendar object to track the currently selected month
    Calendar currentMonth;
    // formats month as yyyy-MM for database queries
    SimpleDateFormat monthFormat;

    // ui elements to display monthly averages
    TextView tvAvgCalories;
    TextView tvAvgProtein;
    TextView tvAvgFat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // connect this activity to its layout file
        setContentView(R.layout.activity_trainer_track05);

        // initialize database helper
        databaseHelper = new DatabaseHelper(this);
        // get trainer and trainee ids from previous screen
        Intent intent = getIntent();
        trainerID = intent.getLongExtra("userID", -1);
        traineeID = intent.getLongExtra("traineeID", -1);

        // initialize month format
        monthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        // set current month to this month
        currentMonth = Calendar.getInstance();

        // find all buttons and views
        Button btnProfile = findViewById(R.id.btnProfileOfTrack);
        Button btnPlan = findViewById(R.id.btnPlanOfTrack);
        Button btnTrack = findViewById(R.id.btnTrackOfTrack);
        ImageButton btnReminder = findViewById(R.id.btnReminder);
        ImageView btnBack = findViewById(R.id.imageView9);
        ImageView btnForward = findViewById(R.id.imageView6);

        // find textviews for displaying averages
        tvAvgCalories = findViewById(R.id.tvAvgCalories);
        tvAvgProtein = findViewById(R.id.tvAvgProtein);
        tvAvgFat = findViewById(R.id.tvAvgFat);

        // load monthly nutrition data
        loadMonthlyNutritionData();

        // back button - return to daily meal tracking
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

        // forward button - go back to trainee list
        btnForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TrainerTrackActivity05.this, TrainerTrackActivity01.class);
                i.putExtra("userID", trainerID);
                startActivity(i);
                finish();
            }
        });

        // navigation buttons - same as before
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

    // loads monthly nutrition statistics for the selected month
    // calculates average daily calories, protein, and fat
    @SuppressLint("Range")
    private void loadMonthlyNutritionData() {
        try {
            // get current month as string for queries
            String currentMonthString = monthFormat.format(currentMonth.getTime());

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

            // check if got results
            if (caloriesCursor != null && caloriesCursor.moveToFirst()) {
                // get average calories
                double avgCalories = caloriesCursor.getDouble(caloriesCursor.getColumnIndex("avgCalories"));
                if (avgCalories > 0) {
                    // display with no decimal places
                    tvAvgCalories.setText(String.format(Locale.getDefault(), "%.0f kcal/day", avgCalories));
                } else {
                    tvAvgCalories.setText("0 kcal/day");
                }
                caloriesCursor.close();
            } else {
                tvAvgCalories.setText("0 kcal/day");
            }

            // calculate average daily protein for the month
            // same approach as calories
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
                    // display with 1 decimal place
                    tvAvgProtein.setText(String.format(Locale.getDefault(), "%.1f g/day", avgProtein));
                } else {
                    tvAvgProtein.setText("0.0 g/day");
                }
                proteinCursor.close();
            } else {
                tvAvgProtein.setText("0.0 g/day");
            }

            // calculate average daily fat for the month
            // same approach as calories and protein
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
                    // display with 1 decimal place
                    tvAvgFat.setText(String.format(Locale.getDefault(), "%.1f g/day", avgFat));
                } else {
                    tvAvgFat.setText("0.0 g/day");
                }
                fatCursor.close();
            } else {
                tvAvgFat.setText("0.0 g/day");
            }

        } catch (Exception e) {
            // if any error occurs, display error message
            e.printStackTrace();
            tvAvgCalories.setText("Error loading data");
            tvAvgProtein.setText("Error loading data");
            tvAvgFat.setText("Error loading data");
        }
    }
}