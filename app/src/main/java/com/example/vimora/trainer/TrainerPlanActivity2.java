package com.example.vimora.trainer;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.vimora.DatabaseHelper;
import com.example.vimora.R;

public class TrainerPlanActivity2 extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private long planId;
    private long currentTrainerId;

    private EditText etPlanName;

    private EditText etItem1Name, etItem1Reps, etItem1Sets;
    private EditText etItem2Name, etItem2Reps, etItem2Sets;
    private EditText etItem3Name, etItem3Reps, etItem3Sets;

    private EditText etRestMinutes;
    private EditText etWorkoutMinutes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        dbHelper = new DatabaseHelper(this);
        setContentView(R.layout.activity_trainer_plan2);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tvName), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        planId = getIntent().getLongExtra("planId", -1);
        currentTrainerId = getIntent().getLongExtra("userID", -1);

        if (planId == -1 || currentTrainerId == -1) {
            Toast.makeText(this, "Loading fail", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        etPlanName = findViewById(R.id.etExerciseName);

        etItem1Name = findViewById(R.id.etExerciseItem1);
        etItem1Reps = findViewById(R.id.item1Reps);
        etItem1Sets = findViewById(R.id.Item1Sets);

        etItem2Name = findViewById(R.id.etExerciseIItem2);
        etItem2Reps = findViewById(R.id.item2Reps);
        etItem2Sets = findViewById(R.id.item2Sets);

        etItem3Name = findViewById(R.id.etExerciseIItem3);
        etItem3Reps = findViewById(R.id.item3Reps);
        etItem3Sets = findViewById(R.id.item3Sets);

        etRestMinutes = findViewById(R.id.restDurationMin);
        etWorkoutMinutes = findViewById(R.id.workoutDurationMin);

        loadAllData();


        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnReminder).setOnClickListener(v -> {
            Intent i = new Intent(this, TrainerReminder1.class);
            i.putExtra("userID", currentTrainerId);
            startActivity(i);
        });

        findViewById(R.id.btnAssign).setOnClickListener(v -> {
            saveAllData();
            Intent i = new Intent(this, TrainerPlanActivity3.class);
            i.putExtra("planId", planId);
            i.putExtra("userID", currentTrainerId);
            startActivity(i);
        });

        findViewById(R.id.btnProfileOfProfile1).setOnClickListener(v -> {
            Intent i = new Intent(this, TrainerProfileActivity1.class);
            i.putExtra("userID", currentTrainerId);
            startActivity(i);
        });

        findViewById(R.id.btnTrackOfProfile1).setOnClickListener(v -> {
            Intent i = new Intent(this, TrainerTrackActivity.class);
            i.putExtra("userID", currentTrainerId);
            startActivity(i);
        });
    }

    private void loadAllData() {

        Cursor planCursor = dbHelper.getPlanById(planId);
        if (planCursor.moveToFirst()) {
            String planName = planCursor.getString(planCursor.getColumnIndexOrThrow("ExerciseName"));
            etPlanName.setText(planName);
        }
        planCursor.close();


        Cursor cursor = dbHelper.getExerciseItemsByPlanId(planId);
        while (cursor.moveToNext()) {
            int order = cursor.getInt(cursor.getColumnIndexOrThrow("OrderIndex"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("ExerciseName"));
            int sets = cursor.getInt(cursor.getColumnIndexOrThrow("Sets"));
            int reps = cursor.getInt(cursor.getColumnIndexOrThrow("Reps"));
            int rest = cursor.getInt(cursor.getColumnIndexOrThrow("RestMinutes"));

            if (order == 0) etRestMinutes.setText(String.valueOf(rest));

            if (order == 0) {
                etItem1Name.setText(name);
                etItem1Sets.setText(String.valueOf(sets));
                etItem1Reps.setText(String.valueOf(reps));
            } else if (order == 1) {
                etItem2Name.setText(name);
                etItem2Sets.setText(String.valueOf(sets));
                etItem2Reps.setText(String.valueOf(reps));
            } else if (order == 2) {
                etItem3Name.setText(name);
                etItem3Sets.setText(String.valueOf(sets));
                etItem3Reps.setText(String.valueOf(reps));
            }
        }
        cursor.close();
    }

    private void saveAllData() {

        String planName = etPlanName.getText().toString().trim();
        if (!planName.isEmpty()) {
            dbHelper.updatePlan(planId, planName, null);
        }


        dbHelper.getWritableDatabase()
                .delete("ExerciseItem", "PlanID = ?", new String[]{String.valueOf(planId)});


        int restMinutes = parseIntOrZero(etRestMinutes.getText().toString());

        saveItemIfNotEmpty(etItem1Name, etItem1Sets, etItem1Reps, restMinutes, 0);
        saveItemIfNotEmpty(etItem2Name, etItem2Sets, etItem2Reps, restMinutes, 1);
        saveItemIfNotEmpty(etItem3Name, etItem3Sets, etItem3Reps, restMinutes, 2);

        Toast.makeText(this, "The plan have been saved", Toast.LENGTH_SHORT).show();
    }

    private void saveItemIfNotEmpty(EditText nameEt, EditText setsEt, EditText repsEt, int restMinutes, int order) {
        String name = nameEt.getText().toString().trim();
        if (name.isEmpty()) return;

        int sets = parseIntOrZero(setsEt.getText().toString());
        int reps = parseIntOrZero(repsEt.getText().toString());

        dbHelper.addExerciseItem(planId, name, sets, reps, restMinutes, order);
    }

    private int parseIntOrZero(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveAllData();
    }
}