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

public class TraineeTrackActivity01 extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private TextView txtMonthYear;
    private TextView btnPreviousMonth;
    private TextView btnNextMonth;
    private GridLayout calendarGrid;
    private Calendar selectedDate;
    private SimpleDateFormat monthYearFormat;
    private long userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_trainee_track01);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tvName), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize database and date
        databaseHelper = new DatabaseHelper(this);
        selectedDate = Calendar.getInstance();
        monthYearFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());

        // Get userID
        Intent intent = getIntent();
        userID = intent.getLongExtra("userID", -1);

        SharedPreferences prefs = getSharedPreferences("VimoraPrefs", MODE_PRIVATE);
        prefs.edit().putLong("userID", userID).apply();

        // Initialize views
        initializeViews();

        // Setup calendar
        setupCalendar();

        // Setup navigation buttons
        setupNavigation();
    }

    private void initializeViews() {
        txtMonthYear = findViewById(R.id.txtMonthYear);
        btnPreviousMonth = findViewById(R.id.btnPreviousMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        calendarGrid = findViewById(R.id.calendarGrid);
    }

    private void setupNavigation() {
        Button btnPlan = findViewById(R.id.btnPlanOfTrack00);
        Button btnProfile = findViewById(R.id.btnProfileOfTrack00);
        ImageButton btnMeal = findViewById(R.id.btnMeal);
        ImageButton btnReminder = findViewById(R.id.btnReminder);
        Button btnNext1 = findViewById(R.id.btnNext1);
        Button btnNext2 = findViewById(R.id.btnNext2);

        btnPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newIntent = new Intent(TraineeTrackActivity01.this, TraineePlanActivity01.class);
                newIntent.putExtra("userID", userID);
                startActivity(newIntent);
            }
        });

        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newIntent = new Intent(TraineeTrackActivity01.this, TraineeProfileActivity.class);
                newIntent.putExtra("userID", userID);
                startActivity(newIntent);
            }
        });

        btnMeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newIntent = new Intent(TraineeTrackActivity01.this, TraineeTrackActivity04.class);
                newIntent.putExtra("userID", userID);
                startActivity(newIntent);
            }
        });

        btnReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newIntent = new Intent(TraineeTrackActivity01.this, TraineeRemindActivity01.class);
                newIntent.putExtra("userID", userID);
                startActivity(newIntent);
            }
        });

        // Month navigation
        btnPreviousMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedDate.add(Calendar.MONTH, -1);
                setupCalendar();
            }
        });

        btnNextMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedDate.add(Calendar.MONTH, 1);
                setupCalendar();
            }
        });

        // Bottom Next buttons - Navigate to Track02 and Track03
        btnNext1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Track Activity 2 (Activity details)
                Intent newIntent = new Intent(TraineeTrackActivity01.this, TraineeTrackActivity02.class);
                newIntent.putExtra("userID", userID);
                startActivity(newIntent);
            }
        });

        btnNext2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Track Activity 3 (Monthly statistics)
                Intent newIntent = new Intent(TraineeTrackActivity01.this, TraineeTrackActivity03.class);
                newIntent.putExtra("userID", userID);
                startActivity(newIntent);
            }
        });
    }

    private void setupCalendar() {
        // Update month/year display
        txtMonthYear.setText(monthYearFormat.format(selectedDate.getTime()));

        // Clear existing calendar cells
        calendarGrid.removeAllViews();

        // Get completed days for this month
        Set<Integer> completedDays = getCompletedDaysForMonth();

        // Calculate calendar data
        Calendar calendar = (Calendar) selectedDate.clone();
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1; // 0 = Sunday

        // Create 42 cells (6 rows x 7 days)
        int dayCounter = 1;
        for (int i = 0; i < 42; i++) {
            TextView dayCell = createDayCell();

            if (i < firstDayOfWeek || dayCounter > daysInMonth) {
                // Empty cell (before month starts or after month ends)
                dayCell.setText("");
                dayCell.setVisibility(View.INVISIBLE);
            } else {
                // Day cell
                dayCell.setText(String.valueOf(dayCounter));
                dayCell.setVisibility(View.VISIBLE);

                // Check if this day is completed
                if (completedDays.contains(dayCounter)) {
                    // Completed day - green background with checkmark
                    dayCell.setBackgroundResource(R.drawable.calendar_cell_completed);
                    dayCell.setText("✓");
                    dayCell.setTextColor(Color.WHITE);
                    dayCell.setTextSize(20);
                } else {
                    // Regular day - white background
                    dayCell.setBackgroundResource(R.drawable.calendar_cell_default);
                    dayCell.setTextColor(Color.BLACK);
                    dayCell.setTextSize(14);
                }

                dayCounter++;
            }

            calendarGrid.addView(dayCell);
        }
    }

    /**
     * Creates a single calendar day cell (TextView)
     */
    private TextView createDayCell() {
        TextView dayCell = new TextView(this);

        // Set layout parameters for GridLayout
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = 0;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(2, 2, 2, 2);

        dayCell.setLayoutParams(params);
        dayCell.setGravity(Gravity.CENTER);
        dayCell.setPadding(4, 8, 4, 8);

        return dayCell;
    }

    /**
     * Gets all completed days for the selected month from database
     */
    @SuppressLint("Range")
    private Set<Integer> getCompletedDaysForMonth() {
        Set<Integer> completedDays = new HashSet<>();

        String yearMonth = monthYearFormat.format(selectedDate.getTime());
        Cursor cursor = databaseHelper.getCompletedDatesForMonth(userID, yearMonth);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String date = cursor.getString(cursor.getColumnIndex("completionDate"));
                // Extract day from date string (format: yyyy-MM-dd)
                String[] parts = date.split("-");
                if (parts.length == 3) {
                    try {
                        int day = Integer.parseInt(parts[2]);
                        completedDays.add(day);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
            cursor.close();
        }

        return completedDays;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh calendar when returning to this activity
        setupCalendar();
    }
}