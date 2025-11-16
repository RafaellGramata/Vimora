package com.example.vimora.trainer;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;

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

        traineeId = getIntent().getLongExtra("traineeId", -1);
        if (traineeId == -1) {
            finish();
            return;
        }

        initViews();
        setupButtons();
        loadTraineeData();
    }

    private void initViews() {
        tvName = findViewById(R.id.tvName);
        tvAssignedPlan = findViewById(R.id.tvAssignedPlan);
        tvAge = findViewById(R.id.tvAge);
        tvWeight = findViewById(R.id.tvWeight);
        tvHeight = findViewById(R.id.tvHeight);
        tvBMI = findViewById(R.id.tvBMI);
    }

    private void setupButtons() {
        findViewById(R.id.toTrainerProfile2).setOnClickListener(v -> finish());
        findViewById(R.id.btnReminder).setOnClickListener(v ->
                startActivity(new Intent(this, TrainerProfileActivity2.class)));
        findViewById(R.id.btnTrackOfProfile1).setOnClickListener(v ->
                startActivity(new Intent(this, TrainerTrackActivity.class)));
        findViewById(R.id.btnPlanOfProfile1).setOnClickListener(v ->
                startActivity(new Intent(this, TrainerPlanActivity1.class)));
    }

    private void loadTraineeData() {
        Cursor c = dbHelper.getTraineeDetails(traineeId);
        if (c.moveToFirst()) {
            String name = c.getString(c.getColumnIndexOrThrow("name"));
            int age = c.getInt(c.getColumnIndexOrThrow("traineeAge"));
            int heightCm = c.getInt(c.getColumnIndexOrThrow("traineeHeight"));
            int weightKg = c.getInt(c.getColumnIndexOrThrow("latestWeight"));

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
        }
        c.close();
        tvAssignedPlan.setText("Basic Plan");
        }

    @Override
    protected void onResume() {
        super.onResume();
        loadTraineeData();
    }
}
