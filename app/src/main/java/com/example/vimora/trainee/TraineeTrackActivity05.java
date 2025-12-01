package com.example.vimora.trainee;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vimora.DatabaseHelper;
import com.example.vimora.NutritionDatabaseHelper;
import com.example.vimora.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

// this activity shows monthly nutrition averages for the trainee
// displays average calories, protein, and fat per day for the selected month
public class TraineeTrackActivity05 extends AppCompatActivity {

    // ui elements for displaying monthly averages
    private EditText traineeCaloriesAverage, traineeProteinAverage, traineeFatAverage;
    private ImageButton btnInfo, btnReminder;
    private Button btnPlan, btnProfile, btnTrack;
    private TextView txtMonthYear, btnPreviousMonth, btnNextMonth;

    // database helpers
    private DatabaseHelper dbHelper;
    private NutritionDatabaseHelper nutritionHelper;
    // stores the logged-in trainee's id
    private long traineeID;
    // calendar object to track selected month
    private Calendar calendar;
    // formats month for display (e.g., "November 2025")
    private SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    // formats month for database queries (e.g., "2025-11")
    private SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // connect this activity to its layout file
        setContentView(R.layout.activity_trainee_track05);

        // initialize database helpers
        dbHelper = new DatabaseHelper(this);
        nutritionHelper = new NutritionDatabaseHelper(dbHelper);

        // get trainee id from shared preferences
        SharedPreferences prefs = getSharedPreferences("VimoraPrefs", MODE_PRIVATE);
        traineeID = prefs.getLong("userID", -1);

        // check if user is logged in
        if (traineeID == -1) {
            Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // initialize calendar to current month
        calendar = Calendar.getInstance();

        // find all views
        initializeViews();

        // display current month and load statistics
        updateMonthDisplay();
        loadMonthlyStatistics();

        // set up button click listeners
        setupListeners();
    }

    // finds all ui elements in the layout
    private void initializeViews() {
        traineeCaloriesAverage = findViewById(R.id.traineeCaloriesAverage);
        traineeProteinAverage = findViewById(R.id.traineeProteinAverage);
        traineeFatAverage = findViewById(R.id.traineeFatAverage);

        txtMonthYear = findViewById(R.id.txtMonthYear);
        btnPreviousMonth = findViewById(R.id.btnPreviousMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);

        btnInfo = findViewById(R.id.btnInfo4);
        btnReminder = findViewById(R.id.btnReminder);

        btnPlan = findViewById(R.id.btnPlanOfTrack4);
        btnProfile = findViewById(R.id.btnProfileOfTrack4);
        btnTrack = findViewById(R.id.btnTrackOfTrack4);

        // make fields read-only (display only)
        // these fields just show data, user cannot edit them
        traineeCaloriesAverage.setFocusable(false);
        traineeCaloriesAverage.setClickable(false);

        traineeProteinAverage.setFocusable(false);
        traineeProteinAverage.setClickable(false);

        traineeFatAverage.setFocusable(false);
        traineeFatAverage.setClickable(false);
    }

    // sets up all button click listeners
    private void setupListeners() {
        // previous month button - go back one month
        btnPreviousMonth.setOnClickListener(v -> {
            // subtract one month
            calendar.add(Calendar.MONTH, -1);
            // update month display
            updateMonthDisplay();
            // reload statistics for new month
            loadMonthlyStatistics();
        });

        // next month button - go forward one month
        btnNextMonth.setOnClickListener(v -> {
            // add one month
            calendar.add(Calendar.MONTH, 1);
            // update month display
            updateMonthDisplay();
            // reload statistics for new month
            loadMonthlyStatistics();
        });

        // back button - return to daily meal tracking
        btnInfo.setOnClickListener(v -> {
            Intent intent = new Intent(TraineeTrackActivity05.this, TraineeTrackActivity04.class);
            startActivity(intent);
            finish();
        });

        // navigation buttons
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

        // track button - go back to daily meal tracking
        btnTrack.setOnClickListener(v -> {
            Intent intent = new Intent(TraineeTrackActivity05.this, TraineeTrackActivity04.class);
            startActivity(intent);
            finish();
        });

        // reminder button
        btnReminder.setOnClickListener(v -> {
            Intent intent = new Intent(TraineeTrackActivity05.this, TraineeRemindActivity01.class);
            intent.putExtra("userID", traineeID);
            startActivity(intent);
        });
    }

    // updates the month display textview
    private void updateMonthDisplay() {
        // display month and year in readable format (e.g., "November 2025")
        txtMonthYear.setText(monthYearFormat.format(calendar.getTime()));
    }

    // loads and displays monthly nutrition statistics
    private void loadMonthlyStatistics() {
        // get current month in database format (yyyy-MM)
        String currentYearMonth = dbFormat.format(calendar.getTime());

        // get monthly averages from database
        // this returns a map with avgCalories, avgProtein, and avgTotalFat
        Map<String, Double> averages = nutritionHelper.getMonthlyAverage(traineeID, currentYearMonth);

        // extract the average values from the map
        double avgCalories = averages.get("avgCalories");
        double avgProtein = averages.get("avgProtein");
        double avgFat = averages.get("avgTotalFat");

        // check if there's any data for this month
        if (avgCalories > 0) {
            // display average calories with no decimal places
            traineeCaloriesAverage.setText(String.format(Locale.getDefault(),
                    "%.0f kcal / day", avgCalories));
            // display average protein with 1 decimal place
            traineeProteinAverage.setText(String.format(Locale.getDefault(),
                    "%.1f g / day", avgProtein));
            // display average fat with 1 decimal place
            traineeFatAverage.setText(String.format(Locale.getDefault(),
                    "%.1f g / day", avgFat));
        } else {
            // no data for this month
            traineeCaloriesAverage.setText("No data for this month");
            traineeProteinAverage.setText("No data for this month");
            traineeFatAverage.setText("No data for this month");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // reload statistics when returning to this screen
        // this ensures any newly added data is displayed
        loadMonthlyStatistics();
    }
}