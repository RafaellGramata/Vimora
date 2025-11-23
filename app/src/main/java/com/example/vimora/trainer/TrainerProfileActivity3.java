package com.example.vimora.trainer;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.vimora.DatabaseHelper;
import com.example.vimora.R;

public class TrainerProfileActivity3 extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private long traineeId;
    private long currentTrainerId;

    private TextView tvName, tvAssignedPlan, tvAge, tvWeight, tvHeight, tvBMI;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        dbHelper = new DatabaseHelper(this);
        setContentView(R.layout.activity_trainer_profile3);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tvName), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        traineeId = intent.getLongExtra("traineeId", -1);

        currentTrainerId = intent.getLongExtra("userID", -1);
        if (traineeId == -1) {
            finish();
            return;
        }

        initViews();
        setupButtons();
        loadTraineeData();
    }

    private void initViews() {
        tvName = findViewById(R.id.outputTraineeName);
        tvAssignedPlan = findViewById(R.id.tvAssignedPlan);
        tvAge = findViewById(R.id.tvAge);
        tvWeight = findViewById(R.id.tvWeight);
        tvHeight = findViewById(R.id.tvHeight);
        tvBMI = findViewById(R.id.tvBMI);
    }

    private void setupButtons() {
        findViewById(R.id.toTrainerProfile01).setOnClickListener(v -> finish());
        findViewById(R.id.btnReminder).setOnClickListener(v -> {
            Intent intent = new Intent(this, TrainerRemindActivity01.class);
            intent.putExtra("userID", currentTrainerId);
            startActivity(intent);
        });

        findViewById(R.id.btnTrackOfProfile1).setOnClickListener(v -> {
            Intent intent = new Intent(this, TrainerTrackActivity01.class);
            intent.putExtra("userID", currentTrainerId);
            startActivity(intent);
        });

        findViewById(R.id.btnPlanOfProfile1).setOnClickListener(v -> {
            Intent intent = new Intent(this, TrainerPlanActivity1.class);
            intent.putExtra("userID", currentTrainerId);
            startActivity(intent);
        });
    }

    private void loadTraineeData() {
        Cursor c = dbHelper.getTraineeDetails(traineeId);
        if (c.moveToFirst()) {
            String name = c.getString(c.getColumnIndexOrThrow("name"));
            try {
                int age = c.getInt(c.getColumnIndexOrThrow("traineeAge"));
                int heightCm = c.getInt(c.getColumnIndexOrThrow("traineeHeight"));

                int weightKg = dbHelper.getLatestWeight(traineeId);

                tvName.setText(name);
                tvAge.setText(String.valueOf(age));
                tvHeight.setText(heightCm + " cm");
                tvWeight.setText(weightKg + " kg");

                if (heightCm > 0 && weightKg > 0) {
                    double heightM = heightCm / 100.0;
                    double bmi = weightKg / (heightM * heightM);
                    tvBMI.setText(String.format("%.1f", bmi));
                } else {
                    tvBMI.setText("N/A");
                }
            }catch (IllegalArgumentException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show();
            }
        }
        String assignedPlanName = getAssignedPlanName(traineeId);
        tvAssignedPlan.setText(assignedPlanName);
    }
    private String getAssignedPlanName(long traineeId) {
        String planName = "Unassigned";

        String query = "SELECT p." + DatabaseHelper.COL_EXERCISE_NAME +
                " FROM " + DatabaseHelper.TABLE_ASSIGNED_PLAN + " a" +
                " JOIN " + DatabaseHelper.TABLE_PLAN + " p ON a.planID = p." + DatabaseHelper.COL_PLAN_ID +
                " WHERE a.traineeID = ?" +
                " ORDER BY a.assignedDate DESC" +
                " LIMIT 1";

        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(query, new String[]{String.valueOf(traineeId)});

        if (cursor.moveToFirst()) {
            planName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EXERCISE_NAME));
            if (planName == null || planName.trim().isEmpty()) {
                planName = "No plan assigned";
            }
        }
        cursor.close();
        return planName;
    }



    @Override
    protected void onResume() {
        super.onResume();
        loadTraineeData();
    }
}
