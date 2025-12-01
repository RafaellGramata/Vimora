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

// this activity shows daily meal tracking for a trainee
// trainer can see what the trainee ate for breakfast, lunch, and dinner
public class TrainerTrackActivity04 extends AppCompatActivity {
    // database helper to query nutrition data
    DatabaseHelper databaseHelper;
    // stores the logged-in trainer's id
    long trainerID;
    // stores the selected trainee's id
    long traineeID;
    // calendar object to track the currently selected date
    Calendar currentDate;
    // formats dates as yyyy-MM-dd for display and queries
    SimpleDateFormat dateFormat;

    // ui elements for displaying meal data
    TextView editTextDate;
    TextView tvBreakfastCalories, tvBreakfastProtein, tvBreakfastFat;
    TextView tvLunchCalories, tvLunchProtein, tvLunchFat;
    TextView tvDinnerCalories, tvDinnerProtein, tvDinnerFat;
    TextView tvTotalCalories, tvTotalProtein, tvTotalFat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // connect this activity to its layout file
        setContentView(R.layout.activity_trainer_track04);

        // initialize database helper
        databaseHelper = new DatabaseHelper(this);
        // get trainer and trainee ids from previous screen
        Intent intent = getIntent();
        trainerID = intent.getLongExtra("userID", -1);
        traineeID = intent.getLongExtra("traineeID", -1);

        // initialize date format
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        // set current date to today
        currentDate = Calendar.getInstance();

        // find all buttons and views
        Button btnProfile = findViewById(R.id.btnProfileOfTrack);
        Button btnPlan = findViewById(R.id.btnPlanOfTrack);
        Button btnTrack = findViewById(R.id.btnTrackOfTrack);
        ImageButton btnReminder = findViewById(R.id.btnReminder);
        ImageView btnBack = findViewById(R.id.imageView9);
        ImageView btnForward = findViewById(R.id.imageView6);
        ImageView btnPrevDate = findViewById(R.id.imageView10);
        ImageView btnNextDate = findViewById(R.id.imageView11);

        // find date display
        editTextDate = findViewById(R.id.editTextDate);

        // find breakfast fields
        tvBreakfastCalories = findViewById(R.id.tvBreakfastCalories);
        tvBreakfastProtein = findViewById(R.id.tvBreakfastProtein);
        tvBreakfastFat = findViewById(R.id.tvBreakfastFat);

        // find lunch fields
        tvLunchCalories = findViewById(R.id.tvLunchCalories);
        tvLunchProtein = findViewById(R.id.tvLunchProtein);
        tvLunchFat = findViewById(R.id.tvLunchFat);

        // find dinner fields
        tvDinnerCalories = findViewById(R.id.tvDinnerCalories);
        tvDinnerProtein = findViewById(R.id.tvDinnerProtein);
        tvDinnerFat = findViewById(R.id.tvDinnerFat);

        // find total fields
        tvTotalCalories = findViewById(R.id.tvTotalCalories);
        tvTotalProtein = findViewById(R.id.tvTotalProtein);
        tvTotalFat = findViewById(R.id.tvTotalFat);

        // display current date and load meal data
        updateDateDisplay();
        loadDailyMealData();

        // previous date button - go back one day
        btnPrevDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // subtract one day
                currentDate.add(Calendar.DAY_OF_MONTH, -1);
                updateDateDisplay();
                loadDailyMealData();
            }
        });

        // next date button - go forward one day
        btnNextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // add one day
                currentDate.add(Calendar.DAY_OF_MONTH, 1);
                updateDateDisplay();
                loadDailyMealData();
            }
        });

        // back button - return to monthly workout stats
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TrainerTrackActivity04.this, TrainerTrackActivity03.class);
                i.putExtra("userID", trainerID);
                i.putExtra("traineeID", traineeID);
                startActivity(i);
                finish();
            }
        });

        // forward button - go to monthly nutrition summary
        btnForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TrainerTrackActivity04.this, TrainerTrackActivity05.class);
                i.putExtra("userID", trainerID);
                i.putExtra("traineeID", traineeID);
                startActivity(i);
            }
        });

        // navigation buttons - same as before
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TrainerTrackActivity04.this, TrainerProfileActivity1.class);
                i.putExtra("userID", trainerID);
                startActivity(i);
            }
        });

        btnPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TrainerTrackActivity04.this, TrainerPlanActivity1.class);
                i.putExtra("userID", trainerID);
                startActivity(i);
            }
        });

        btnTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TrainerTrackActivity04.this, TrainerTrackActivity01.class);
                i.putExtra("userID", trainerID);
                startActivity(i);
            }
        });

        btnReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TrainerTrackActivity04.this, TrainerRemindActivity01.class);
                i.putExtra("userID", trainerID);
                startActivity(i);
            }
        });
    }

    // updates the date display
    private void updateDateDisplay() {
        String dateString = dateFormat.format(currentDate.getTime());
        editTextDate.setText(dateString);
    }

    // loads all meal data for the selected date
    // gets nutrition info for breakfast, lunch, and dinner separately
    @SuppressLint("Range")
    private void loadDailyMealData() {
        try {
            // get current date as string for queries
            String currentDateString = dateFormat.format(currentDate.getTime());

            // log what we're loading (helpful for debugging)
            android.util.Log.d("TrainerTrack04", "Loading data for traineeID: " + traineeID + ", date: " + currentDateString);

            // query breakfast data
            // we sum up all nutrition entries for breakfast on this date
            Cursor breakfastCursor = databaseHelper.getReadableDatabase().rawQuery(
                    "SELECT SUM(calories) as totalCalories, SUM(protein) as totalProtein, SUM(totalFat) as totalFat " +
                            "FROM NutritionLog " +
                            "WHERE traineeID = ? AND date = ? AND LOWER(mealType) = 'breakfast'",
                    new String[]{String.valueOf(traineeID), currentDateString}
            );

            // check if we found breakfast data
            if (breakfastCursor != null && breakfastCursor.moveToFirst()) {
                // extract the totals
                int calories = breakfastCursor.getInt(breakfastCursor.getColumnIndex("totalCalories"));
                int protein = breakfastCursor.getInt(breakfastCursor.getColumnIndex("totalProtein"));
                int fat = breakfastCursor.getInt(breakfastCursor.getColumnIndex("totalFat"));

                // log the data
                android.util.Log.d("TrainerTrack04", "Breakfast: " + calories + " cal, " + protein + " protein, " + fat + " fat");

                // display the values
                tvBreakfastCalories.setText(String.valueOf(calories));
                tvBreakfastProtein.setText(String.valueOf(protein));
                tvBreakfastFat.setText(String.valueOf(fat));
                breakfastCursor.close();
            } else {
                // no breakfast data found, show 0
                android.util.Log.d("TrainerTrack04", "No breakfast data found");
                tvBreakfastCalories.setText("0");
                tvBreakfastProtein.setText("0");
                tvBreakfastFat.setText("0");
            }

            // query lunch data - same process as breakfast
            Cursor lunchCursor = databaseHelper.getReadableDatabase().rawQuery(
                    "SELECT SUM(calories) as totalCalories, SUM(protein) as totalProtein, SUM(totalFat) as totalFat " +
                            "FROM NutritionLog " +
                            "WHERE traineeID = ? AND date = ? AND LOWER(mealType) = 'lunch'",
                    new String[]{String.valueOf(traineeID), currentDateString}
            );

            if (lunchCursor != null && lunchCursor.moveToFirst()) {
                int calories = lunchCursor.getInt(lunchCursor.getColumnIndex("totalCalories"));
                int protein = lunchCursor.getInt(lunchCursor.getColumnIndex("totalProtein"));
                int fat = lunchCursor.getInt(lunchCursor.getColumnIndex("totalFat"));

                tvLunchCalories.setText(String.valueOf(calories));
                tvLunchProtein.setText(String.valueOf(protein));
                tvLunchFat.setText(String.valueOf(fat));
                lunchCursor.close();
            } else {
                tvLunchCalories.setText("0");
                tvLunchProtein.setText("0");
                tvLunchFat.setText("0");
            }

            // query dinner data - same process as breakfast and lunch
            Cursor dinnerCursor = databaseHelper.getReadableDatabase().rawQuery(
                    "SELECT SUM(calories) as totalCalories, SUM(protein) as totalProtein, SUM(totalFat) as totalFat " +
                            "FROM NutritionLog " +
                            "WHERE traineeID = ? AND date = ? AND LOWER(mealType) = 'dinner'",
                    new String[]{String.valueOf(traineeID), currentDateString}
            );

            if (dinnerCursor != null && dinnerCursor.moveToFirst()) {
                int calories = dinnerCursor.getInt(dinnerCursor.getColumnIndex("totalCalories"));
                int protein = dinnerCursor.getInt(dinnerCursor.getColumnIndex("totalProtein"));
                int fat = dinnerCursor.getInt(dinnerCursor.getColumnIndex("totalFat"));

                tvDinnerCalories.setText(String.valueOf(calories));
                tvDinnerProtein.setText(String.valueOf(protein));
                tvDinnerFat.setText(String.valueOf(fat));
                dinnerCursor.close();
            } else {
                tvDinnerCalories.setText("0");
                tvDinnerProtein.setText("0");
                tvDinnerFat.setText("0");
            }

            // calculate totals by adding breakfast + lunch + dinner
            int totalCal = Integer.parseInt(tvBreakfastCalories.getText().toString()) +
                    Integer.parseInt(tvLunchCalories.getText().toString()) +
                    Integer.parseInt(tvDinnerCalories.getText().toString());

            int totalProt = Integer.parseInt(tvBreakfastProtein.getText().toString()) +
                    Integer.parseInt(tvLunchProtein.getText().toString()) +
                    Integer.parseInt(tvDinnerProtein.getText().toString());

            int totalFt = Integer.parseInt(tvBreakfastFat.getText().toString()) +
                    Integer.parseInt(tvLunchFat.getText().toString()) +
                    Integer.parseInt(tvDinnerFat.getText().toString());

            // display the totals
            tvTotalCalories.setText(String.valueOf(totalCal));
            tvTotalProtein.setText(String.valueOf(totalProt));
            tvTotalFat.setText(String.valueOf(totalFt));

        } catch (Exception e) {
            // if any error occurs, log it and set all values to 0
            e.printStackTrace();
            android.util.Log.e("TrainerTrack04", "Error loading meal data: " + e.getMessage());

            // set all fields to 0
            tvBreakfastCalories.setText("0");
            tvBreakfastProtein.setText("0");
            tvBreakfastFat.setText("0");
            tvLunchCalories.setText("0");
            tvLunchProtein.setText("0");
            tvLunchFat.setText("0");
            tvDinnerCalories.setText("0");
            tvDinnerProtein.setText("0");
            tvDinnerFat.setText("0");
            tvTotalCalories.setText("0");
            tvTotalProtein.setText("0");
            tvTotalFat.setText("0");
        }
    }
}