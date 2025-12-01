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

// this activity lets trainees track their daily meals
// they can enter calories, protein, and fat for breakfast, lunch, and dinner
public class TraineeTrackActivity04 extends AppCompatActivity {

    // ui elements for date and meal tracking
    private TextView editTextDate, txtTotalCal, txtTotalProtein, txtTotalFat;
    private EditText editBreakCal, editBreakProtein, editBreakFat;
    private EditText editLunchCal, editLunchProtein, editLunchFat;
    private EditText editDinnerCal, editDinnerProtein, editDinnerFat;
    private ImageButton btnSave, btnViewMore, btnInfo, btnReminder;
    private TextView btnPreviousDate, btnNextDate;
    private Button btnPlan, btnProfile, btnTrack;

    // database helpers for workout and nutrition data
    private DatabaseHelper dbHelper;
    private NutritionDatabaseHelper nutritionHelper;
    // stores the logged-in trainee's id
    private long traineeID;
    // calendar object for date navigation
    private Calendar calendar;
    // stores the currently selected date
    private String selectedDate;
    // formats dates as yyyy-MM-dd
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // connect this activity to its layout file
        setContentView(R.layout.activity_trainee_track04);

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

        // find all views in the layout
        initializeViews();

        // set up calendar for date navigation
        calendar = Calendar.getInstance();
        // set selected date to today
        selectedDate = getCurrentDate();
        editTextDate.setText(selectedDate);

        // load existing nutrition data for today
        loadNutritionData(selectedDate);

        // set up button click listeners
        setupListeners();
    }

    // finds all ui elements in the layout
    private void initializeViews() {
        editTextDate = findViewById(R.id.editTextDateMeal);
        btnPreviousDate = findViewById(R.id.btnPreviousDate);
        btnNextDate = findViewById(R.id.btnNextDate);

        // breakfast input fields
        editBreakCal = findViewById(R.id.editBreakCal);
        editBreakProtein = findViewById(R.id.editBreakProtein);
        editBreakFat = findViewById(R.id.editBreakFat);

        // lunch input fields
        editLunchCal = findViewById(R.id.editLunchCal);
        editLunchProtein = findViewById(R.id.editLunchProtein);
        editLunchFat = findViewById(R.id.editLunchFat);

        // dinner input fields
        editDinnerCal = findViewById(R.id.editDinnerCal);
        editDinnerProtein = findViewById(R.id.editDinnerProtein);
        editDinnerFat = findViewById(R.id.editDinnerFat);

        // total displays (these are read-only, calculated automatically)
        txtTotalCal = findViewById(R.id.editTotalCal);
        txtTotalProtein = findViewById(R.id.txtTotalProtein);
        txtTotalFat = findViewById(R.id.txtTotalFat);

        // action buttons
        btnSave = findViewById(R.id.btnSaveMealTrack);
        btnViewMore = findViewById(R.id.btnMoreMealTrack);
        btnInfo = findViewById(R.id.btnInfo3);
        btnReminder = findViewById(R.id.btnReminder);

        // navigation buttons
        btnPlan = findViewById(R.id.btnPlanOfTrack3);
        btnProfile = findViewById(R.id.btnProfileOfTrack3);
        btnTrack = findViewById(R.id.btnTrackOfTrack3);
    }

    // sets up all button click listeners
    private void setupListeners() {
        // previous date button - go back one day
        btnPreviousDate.setOnClickListener(v -> {
            // subtract one day from calendar
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            // format new date as string
            selectedDate = sdf.format(calendar.getTime());
            // update display
            editTextDate.setText(selectedDate);
            // load nutrition data for new date
            loadNutritionData(selectedDate);
        });

        // next date button - go forward one day
        btnNextDate.setOnClickListener(v -> {
            // add one day to calendar
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            // format new date as string
            selectedDate = sdf.format(calendar.getTime());
            // update display
            editTextDate.setText(selectedDate);
            // load nutrition data for new date
            loadNutritionData(selectedDate);
        });

        // date picker - opens when user clicks on the date
        editTextDate.setOnClickListener(v -> showDatePicker());

        // auto-calculate totals when any field changes
        setupAutoCalculate();

        // save button - saves all meal data for the selected date
        btnSave.setOnClickListener(v -> saveNutritionData());

        // view more button - go to monthly nutrition summary
        btnViewMore.setOnClickListener(v -> {
            Intent intent = new Intent(TraineeTrackActivity04.this, TraineeTrackActivity05.class);
            startActivity(intent);
        });

        // info/back button - go back to workout tracking
        btnInfo.setOnClickListener(v -> {
            Intent intent = new Intent(TraineeTrackActivity04.this, TraineeTrackActivity01.class);
            intent.putExtra("userID", traineeID);
            startActivity(intent);
            finish();
        });

        // navigation buttons
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

        // reminder button
        btnReminder.setOnClickListener(v -> {
            Intent intent = new Intent(TraineeTrackActivity04.this, TraineeRemindActivity01.class);
            intent.putExtra("userID", traineeID);
            startActivity(intent);
        });
    }

    // sets up auto-calculation of totals
    // whenever user types in any field, totals are recalculated
    private void setupAutoCalculate() {
        // create a text watcher that recalculates totals on any change
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            // this is called after text changes
            public void afterTextChanged(Editable s) {
                // recalculate and display totals
                calculateTotals();
            }
        };

        // add the watcher to all input fields
        // so totals update whenever any field changes
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

    // calculates and displays total calories, protein, and fat
    // by adding breakfast + lunch + dinner
    private void calculateTotals() {
        // add up calories from all three meals
        int totalCalories = getIntValue(editBreakCal) + getIntValue(editLunchCal) + getIntValue(editDinnerCal);
        // add up protein from all three meals
        int totalProtein = getIntValue(editBreakProtein) + getIntValue(editLunchProtein) + getIntValue(editDinnerProtein);
        // add up fat from all three meals
        int totalFat = getIntValue(editBreakFat) + getIntValue(editLunchFat) + getIntValue(editDinnerFat);

        // display the totals
        txtTotalCal.setText(String.valueOf(totalCalories));
        txtTotalProtein.setText(String.valueOf(totalProtein));
        txtTotalFat.setText(String.valueOf(totalFat));
    }

    // helper method to safely get integer value from an edittext
    // returns 0 if field is empty or contains invalid number
    private int getIntValue(EditText editText) {
        String text = editText.getText().toString().trim();
        if (text.isEmpty()) return 0;
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // shows a date picker dialog for selecting a date
    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                // when user picks a date, update calendar and load data
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    selectedDate = formatDate(calendar);
                    editTextDate.setText(selectedDate);
                    loadNutritionData(selectedDate);
                },
                // set initial date in picker to current selected date
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    // loads nutrition data from database for a specific date
    private void loadNutritionData(String date) {
        // load breakfast data
        NutritionEntry breakfast = nutritionHelper.getNutritionEntry(traineeID, date, "breakfast");
        if (breakfast != null) {
            // if data exists, fill in the fields
            editBreakCal.setText(String.valueOf(breakfast.getCalories()));
            editBreakProtein.setText(String.valueOf(breakfast.getProtein()));
            editBreakFat.setText(String.valueOf(breakfast.getTotalFat()));
        } else {
            // no data found, clear the fields
            editBreakCal.setText("");
            editBreakProtein.setText("");
            editBreakFat.setText("");
        }

        // load lunch data
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

        // load dinner data
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

        // totals will auto-calculate via text watcher
        calculateTotals();
    }

    // saves all nutrition data to the database
    private void saveNutritionData() {
        try {
            // save breakfast if any values were entered
            int breakCal = getIntValue(editBreakCal);
            int breakProt = getIntValue(editBreakProtein);
            int breakFat = getIntValue(editBreakFat);
            if (breakCal > 0 || breakProt > 0 || breakFat > 0) {
                nutritionHelper.saveNutritionEntry(traineeID, selectedDate, "breakfast",
                        breakCal, breakProt, breakFat);
            }

            // save lunch if any values were entered
            int lunchCal = getIntValue(editLunchCal);
            int lunchProt = getIntValue(editLunchProtein);
            int lunchFat = getIntValue(editLunchFat);
            if (lunchCal > 0 || lunchProt > 0 || lunchFat > 0) {
                nutritionHelper.saveNutritionEntry(traineeID, selectedDate, "lunch",
                        lunchCal, lunchProt, lunchFat);
            }

            // save dinner if any values were entered
            int dinnerCal = getIntValue(editDinnerCal);
            int dinnerProt = getIntValue(editDinnerProtein);
            int dinnerFat = getIntValue(editDinnerFat);
            if (dinnerCal > 0 || dinnerProt > 0 || dinnerFat > 0) {
                nutritionHelper.saveNutritionEntry(traineeID, selectedDate, "dinner",
                        dinnerCal, dinnerProt, dinnerFat);
            }

            // show success message with the date
            Toast.makeText(this, "Nutrition data saved for " + selectedDate, Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            // show error message if something went wrong
            Toast.makeText(this, "Error saving data: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // gets current date as a formatted string
    private String getCurrentDate() {
        return sdf.format(Calendar.getInstance().getTime());
    }

    // formats a calendar object as a date string
    private String formatDate(Calendar cal) {
        return sdf.format(cal.getTime());
    }
}