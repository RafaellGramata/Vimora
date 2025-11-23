package com.example.vimora.trainee;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vimora.DatabaseHelper;
import com.example.vimora.NutritionDatabaseHelper;
import com.example.vimora.NutritionEntry;
import com.example.vimora.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TraineeTrackActivity04 extends AppCompatActivity {

    private TextView editTextDate, txtTotalCal, txtTotalProtein, txtTotalFat;
    private EditText editBreakCal, editBreakProtein, editBreakFat;
    private EditText editLunchCal, editLunchProtein, editLunchFat;
    private EditText editDinnerCal, editDinnerProtein, editDinnerFat;
    private ImageButton btnSave, btnViewMore, btnInfo, btnReminder;
    private TextView btnPreviousDate, btnNextDate;
    private Button btnPlan, btnProfile, btnTrack;

    private DatabaseHelper dbHelper;
    private NutritionDatabaseHelper nutritionHelper;
    private long traineeID;
    private Calendar calendar;
    private String selectedDate;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainee_track04);

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

        // Set up calendar for date navigation
        calendar = Calendar.getInstance();
        selectedDate = getCurrentDate();
        editTextDate.setText(selectedDate);

        // Load existing data for today
        loadNutritionData(selectedDate);

        // Set up listeners
        setupListeners();
    }

    private void initializeViews() {
        editTextDate = findViewById(R.id.editTextDateMeal);
        btnPreviousDate = findViewById(R.id.btnPreviousDate);
        btnNextDate = findViewById(R.id.btnNextDate);

        // Breakfast fields
        editBreakCal = findViewById(R.id.editBreakCal);
        editBreakProtein = findViewById(R.id.editBreakProtein);
        editBreakFat = findViewById(R.id.editBreakFat);

        // Lunch fields
        editLunchCal = findViewById(R.id.editLunchCal);
        editLunchProtein = findViewById(R.id.editLunchProtein);
        editLunchFat = findViewById(R.id.editLunchFat);

        // Dinner fields
        editDinnerCal = findViewById(R.id.editDinnerCal);
        editDinnerProtein = findViewById(R.id.editDinnerProtein);
        editDinnerFat = findViewById(R.id.editDinnerFat);

        // Total displays (read-only)
        txtTotalCal = findViewById(R.id.editTotalCal);
        txtTotalProtein = findViewById(R.id.txtTotalProtein);
        txtTotalFat = findViewById(R.id.txtTotalFat);

        btnSave = findViewById(R.id.btnSaveMealTrack);
        btnViewMore = findViewById(R.id.btnMoreMealTrack);
        btnInfo = findViewById(R.id.btnInfo3);
        btnReminder = findViewById(R.id.btnReminder);

        btnPlan = findViewById(R.id.btnPlanOfTrack3);
        btnProfile = findViewById(R.id.btnProfileOfTrack3);
        btnTrack = findViewById(R.id.btnTrackOfTrack3);
    }

    private void setupListeners() {
        // Date navigation with < > buttons
        btnPreviousDate.setOnClickListener(v -> {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            selectedDate = sdf.format(calendar.getTime());
            editTextDate.setText(selectedDate);
            loadNutritionData(selectedDate);
        });

        btnNextDate.setOnClickListener(v -> {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            selectedDate = sdf.format(calendar.getTime());
            editTextDate.setText(selectedDate);
            loadNutritionData(selectedDate);
        });

        // Date picker on click
        editTextDate.setOnClickListener(v -> showDatePicker());

        // Auto-calculate totals when any field changes
        setupAutoCalculate();

        // Save button - saves data according to the selected date
        btnSave.setOnClickListener(v -> saveNutritionData());

        // View more button - go to monthly summary (Activity 05)
        btnViewMore.setOnClickListener(v -> {
            Intent intent = new Intent(TraineeTrackActivity04.this, TraineeTrackActivity05.class);
            startActivity(intent);
        });

        // Info/Back button - go back to workout tracking
        btnInfo.setOnClickListener(v -> {
            Intent intent = new Intent(TraineeTrackActivity04.this, TraineeTrackActivity01.class);
            intent.putExtra("userID", traineeID);
            startActivity(intent);
            finish();
        });

        // Navigation buttons
        btnPlan.setOnClickListener(v -> {
            Intent intent = new Intent(TraineeTrackActivity04.this, TraineePlanActivity01.class);
            intent.putExtra("userID", traineeID);
            startActivity(intent);
        });

        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(TraineeTrackActivity04.this, TraineeProfileActivity.class);
            intent.putExtra("userID", traineeID);
            startActivity(intent);
        });


        // Reminder button
        btnReminder.setOnClickListener(v -> {
            Intent intent = new Intent(TraineeTrackActivity04.this, TraineeRemindActivity01.class);
            intent.putExtra("userID", traineeID);
            startActivity(intent);
        });
    }

    private void setupAutoCalculate() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                calculateTotals();
            }
        };

        // Add listeners to all input fields
        editBreakCal.addTextChangedListener(textWatcher);
        editBreakProtein.addTextChangedListener(textWatcher);
        editBreakFat.addTextChangedListener(textWatcher);

        editLunchCal.addTextChangedListener(textWatcher);
        editLunchProtein.addTextChangedListener(textWatcher);
        editLunchFat.addTextChangedListener(textWatcher);

        editDinnerCal.addTextChangedListener(textWatcher);
        editDinnerProtein.addTextChangedListener(textWatcher);
        editDinnerFat.addTextChangedListener(textWatcher);
    }

    private void calculateTotals() {
        int totalCalories = getIntValue(editBreakCal) + getIntValue(editLunchCal) + getIntValue(editDinnerCal);
        int totalProtein = getIntValue(editBreakProtein) + getIntValue(editLunchProtein) + getIntValue(editDinnerProtein);
        int totalFat = getIntValue(editBreakFat) + getIntValue(editLunchFat) + getIntValue(editDinnerFat);

        txtTotalCal.setText(String.valueOf(totalCalories));
        txtTotalProtein.setText(String.valueOf(totalProtein));
        txtTotalFat.setText(String.valueOf(totalFat));
    }

    private int getIntValue(EditText editText) {
        String text = editText.getText().toString().trim();
        if (text.isEmpty()) return 0;
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    selectedDate = formatDate(calendar);
                    editTextDate.setText(selectedDate);
                    loadNutritionData(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void loadNutritionData(String date) {
        // Load breakfast
        NutritionEntry breakfast = nutritionHelper.getNutritionEntry(traineeID, date, "breakfast");
        if (breakfast != null) {
            editBreakCal.setText(String.valueOf(breakfast.getCalories()));
            editBreakProtein.setText(String.valueOf(breakfast.getProtein()));
            editBreakFat.setText(String.valueOf(breakfast.getTotalFat()));
        } else {
            editBreakCal.setText("");
            editBreakProtein.setText("");
            editBreakFat.setText("");
        }

        // Load lunch
        NutritionEntry lunch = nutritionHelper.getNutritionEntry(traineeID, date, "lunch");
        if (lunch != null) {
            editLunchCal.setText(String.valueOf(lunch.getCalories()));
            editLunchProtein.setText(String.valueOf(lunch.getProtein()));
            editLunchFat.setText(String.valueOf(lunch.getTotalFat()));
        } else {
            editLunchCal.setText("");
            editLunchProtein.setText("");
            editLunchFat.setText("");
        }

        // Load dinner
        NutritionEntry dinner = nutritionHelper.getNutritionEntry(traineeID, date, "dinner");
        if (dinner != null) {
            editDinnerCal.setText(String.valueOf(dinner.getCalories()));
            editDinnerProtein.setText(String.valueOf(dinner.getProtein()));
            editDinnerFat.setText(String.valueOf(dinner.getTotalFat()));
        } else {
            editDinnerCal.setText("");
            editDinnerProtein.setText("");
            editDinnerFat.setText("");
        }

        // Totals will auto-calculate via TextWatcher
        calculateTotals();
    }

    private void saveNutritionData() {
        try {
            // Save breakfast
            int breakCal = getIntValue(editBreakCal);
            int breakProt = getIntValue(editBreakProtein);
            int breakFat = getIntValue(editBreakFat);
            if (breakCal > 0 || breakProt > 0 || breakFat > 0) {
                nutritionHelper.saveNutritionEntry(traineeID, selectedDate, "breakfast",
                        breakCal, breakProt, breakFat);
            }

            // Save lunch
            int lunchCal = getIntValue(editLunchCal);
            int lunchProt = getIntValue(editLunchProtein);
            int lunchFat = getIntValue(editLunchFat);
            if (lunchCal > 0 || lunchProt > 0 || lunchFat > 0) {
                nutritionHelper.saveNutritionEntry(traineeID, selectedDate, "lunch",
                        lunchCal, lunchProt, lunchFat);
            }

            // Save dinner
            int dinnerCal = getIntValue(editDinnerCal);
            int dinnerProt = getIntValue(editDinnerProtein);
            int dinnerFat = getIntValue(editDinnerFat);
            if (dinnerCal > 0 || dinnerProt > 0 || dinnerFat > 0) {
                nutritionHelper.saveNutritionEntry(traineeID, selectedDate, "dinner",
                        dinnerCal, dinnerProt, dinnerFat);
            }

            Toast.makeText(this, "Nutrition data saved for " + selectedDate, Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "Error saving data: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String getCurrentDate() {
        return sdf.format(Calendar.getInstance().getTime());
    }

    private String formatDate(Calendar cal) {
        return sdf.format(cal.getTime());
    }
}