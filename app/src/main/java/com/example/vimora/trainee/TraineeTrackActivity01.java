package com.example.vimora.trainee;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.vimora.DatabaseHelper;
import com.example.vimora.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

// this activity shows a monthly calendar view for the trainee
// displays completed workout days with checkmarks
public class TraineeTrackActivity01 extends AppCompatActivity {

    // database helper to query workout completion data
    private DatabaseHelper databaseHelper;
    // ui elements for month navigation and calendar display
    private TextView txtMonthYear;
    private TextView btnPreviousMonth;
    private TextView btnNextMonth;
    private GridLayout calendarGrid;
    // calendar object to track selected month
    private Calendar selectedDate;
    // formats month as yyyy-MM for database queries
    private SimpleDateFormat monthYearFormat;
    // stores the logged-in trainee's id
    private long userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // enable edge-to-edge display
        EdgeToEdge.enable(this);
        // connect this activity to its layout file
        setContentView(R.layout.activity_trainee_track01);

        // handle system bars padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tvName), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // initialize database helper
        databaseHelper = new DatabaseHelper(this);
        // set selected date to current month
        selectedDate = Calendar.getInstance();
        // initialize month format for queries
        monthYearFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());

        // get user id from previous screen
        Intent intent = getIntent();
        userID = intent.getLongExtra("userID", -1);

        // save user id to shared preferences for later use
        SharedPreferences prefs = getSharedPreferences("VimoraPrefs", MODE_PRIVATE);
        prefs.edit().putLong("userID", userID).apply();

        // find all views in the layout
        initializeViews();

        // create the calendar display
        setupCalendar();

        // setup button click listeners
        setupNavigation();
    }

    // finds all ui elements in the layout
    private void initializeViews() {
        txtMonthYear = findViewById(R.id.txtMonthYear);
        btnPreviousMonth = findViewById(R.id.btnPreviousMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        calendarGrid = findViewById(R.id.calendarGrid);
    }

    // sets up all button click listeners for navigation
    private void setupNavigation() {
        Button btnPlan = findViewById(R.id.btnPlanOfTrack00);
        Button btnProfile = findViewById(R.id.btnProfileOfTrack00);
        ImageButton btnMeal = findViewById(R.id.btnMeal);
        ImageButton btnReminder = findViewById(R.id.btnReminder);
        ImageButton btnNext = findViewById(R.id.btnNext);

        // plan button - go to workout plans
        btnPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newIntent = new Intent(TraineeTrackActivity01.this, TraineePlanActivity01.class);
                newIntent.putExtra("userID", userID);
                startActivity(newIntent);
            }
        });

        // profile button - go to profile
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newIntent = new Intent(TraineeTrackActivity01.this, TraineeProfileActivity.class);
                newIntent.putExtra("userID", userID);
                startActivity(newIntent);
            }
        });

        // meal button - go to meal tracking
        btnMeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newIntent = new Intent(TraineeTrackActivity01.this, TraineeTrackActivity04.class);
                newIntent.putExtra("userID", userID);
                startActivity(newIntent);
            }
        });

        // reminder button - go to reminders
        btnReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newIntent = new Intent(TraineeTrackActivity01.this, TraineeRemindActivity01.class);
                newIntent.putExtra("userID", userID);
                startActivity(newIntent);
            }
        });

        // previous month button - go back one month
        btnPreviousMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // subtract one month
                selectedDate.add(Calendar.MONTH, -1);
                // rebuild calendar for new month
                setupCalendar();
            }
        });

        // next month button - go forward one month
        btnNextMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // add one month
                selectedDate.add(Calendar.MONTH, 1);
                // rebuild calendar for new month
                setupCalendar();
            }
        });

        // next button - go to daily workout tracking
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newIntent = new Intent(TraineeTrackActivity01.this, TraineeTrackActivity02.class);
                newIntent.putExtra("userID", userID);
                startActivity(newIntent);
            }
        });
    }

    // builds the calendar display for the selected month
    private void setupCalendar() {
        // update month/year display (ex: "2025-11")
        txtMonthYear.setText(monthYearFormat.format(selectedDate.getTime()));

        // remove any existing calendar cells from previous month
        calendarGrid.removeAllViews();

        // get which days are completed for this month from database
        Set<Integer> completedDays = getCompletedDaysForMonth();

        // create a calendar object for calculations
        Calendar calendar = (Calendar) selectedDate.clone();
        // set to first day of month
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        // get total days in this month
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        // get which day of week the month starts on (0=Sunday, 1=Monday, etc.)
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        // -1 because java's system uses 1 as Sunday while grid uses 0 for Sunday


        // create 42 cells (6 rows x 7 days) to display the full calendar
        int dayCounter = 1;
        for (int i = 0; i < 42; i++) {
            // create a textview for each calendar cell
            TextView dayCell = createDayCell();

            if (i < firstDayOfWeek || dayCounter > daysInMonth) {
                // empty cell - before month starts or after month ends
                dayCell.setText("");
                dayCell.setVisibility(View.INVISIBLE);
            } else {
                // actual day cell
                dayCell.setText(String.valueOf(dayCounter));
                dayCell.setVisibility(View.VISIBLE);

                // check if this day has a completed workout
                if (completedDays.contains(dayCounter)) {
                    // workout completed - show green background with checkmark
                    dayCell.setBackgroundResource(R.drawable.calendar_cell_completed);
                    dayCell.setText("✓");
                    dayCell.setTextColor(Color.WHITE);
                    dayCell.setTextSize(20);
                } else {
                    // no workout completed - show white background with day number
                    dayCell.setBackgroundResource(R.drawable.calendar_cell_default);
                    dayCell.setTextColor(Color.BLACK);
                    dayCell.setTextSize(14);
                }

                dayCounter++;
            }

            // add the cell to the calendar grid
            calendarGrid.addView(dayCell);
        }
    }

    // method that creates and returns a single calendar day cell (textview)
    // sets the size and styling for each cell
    private TextView createDayCell() {
        TextView dayCell = new TextView(this);

        // set layout parameters for grid layout
        // layoutparams tells gridlayout how this view should be sized and positioned
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        // width and height set to 0 with weight 1f means equal distribution
        params.width = 0;
        params.height = 0;

        // tells grid how the view behaves horizontally
        // gridlayout.undefined tells the cell to go in whatever column the grid decides next
        // 1f weight is every cell has the same weight, getting equal row width
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);

        //same as column for got vertical
        params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        // add small margins between cells (px)
        params.setMargins(2, 2, 2, 2);

        //apply the params to the dayCell
        dayCell.setLayoutParams(params);
        // center the text in the cell
        dayCell.setGravity(Gravity.CENTER);
        // add padding inside the cell
        dayCell.setPadding(4, 8, 4, 8);

        return dayCell;
    }

    // gets all completed workout days for the selected month from database
    // returns a set of day numbers (ex: {1, 5, 12, 20})
    @SuppressLint("Range") // don't show warnings about a lint rule for this method
    // lint rule is a warning system that checks for possible mistakes
    private Set<Integer> getCompletedDaysForMonth() {
        // create a set to store completed day numbers
        Set<Integer> completedDays = new HashSet<>();

        // get month as string (ex: "2025-11")
        String yearMonth = monthYearFormat.format(selectedDate.getTime());
        // query database for all completed dates in this month
        Cursor cursor = databaseHelper.getCompletedDatesForMonth(userID, yearMonth);

        if (cursor != null) {
            // loop through all completed dates
            while (cursor.moveToNext()) {
                // get the date string (format: yyyy-MM-dd)
                String date = cursor.getString(cursor.getColumnIndex("completionDate"));
                // split the date string by '-' to extract day
                String[] parts = date.split("-");
                if (parts.length == 3) {
                    try {
                        // parse the day number (third part of the date)
                        int day = Integer.parseInt(parts[2]);
                        // add this day to our set
                        completedDays.add(day);
                        // in case string cant be converted to number
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
            // close cursor to free memory
            cursor.close();
        }

        return completedDays;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // refresh calendar when returning to this screen
        // this ensures newly completed workouts show up
        setupCalendar();
    }
}