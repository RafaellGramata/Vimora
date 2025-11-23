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
    private EditText etExerciseName;
    private EditText etExerciseItem1;
    private EditText etExerciseItem2;
    private EditText etExerciseItem3;
    private EditText item1Reps;
    private EditText item2Reps;
    private EditText item3Reps;
    private EditText item1Sets;
    private EditText item2Sets;
    private EditText item3Sets;
    private EditText restDurationMin;
    private EditText workoutDurationMin;
    private long planId;
    private long currentTrainerId;
    private boolean isEditing = false;

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

        Intent intent = getIntent();
        planId = intent.getLongExtra("planId", -1);
        currentTrainerId = intent.getLongExtra("userID", -1);

        if (planId == -1) {
            finish();
            return;
        }

        etExerciseName = findViewById(R.id.etExerciseName);
        etExerciseItem1 = findViewById(R.id.etExerciseItem1);
        etExerciseItem2 = findViewById(R.id.etExerciseIItem2);
        etExerciseItem3 = findViewById(R.id.etExerciseIItem3);
        item1Reps = findViewById(R.id.item1Reps);
        item2Reps = findViewById(R.id.item2Reps);
        item3Reps = findViewById(R.id.item3Reps);
        item1Sets = findViewById(R.id.Item1Sets);
        item2Sets = findViewById(R.id.item2Sets);
        item3Sets = findViewById(R.id.item3Sets);
        restDurationMin = findViewById(R.id.restDurationMin);
        workoutDurationMin = findViewById(R.id.workoutDurationMin);

        etExerciseName.setEnabled(true);
        etExerciseItem1.setEnabled(true);
        etExerciseItem2.setEnabled(true);
        etExerciseItem3.setEnabled(true);
        item1Reps.setEnabled(true);
        item2Reps.setEnabled(true);
        item3Reps.setEnabled(true);
        item1Sets.setEnabled(true);
        item2Sets.setEnabled(true);
        item3Sets.setEnabled(true);
        restDurationMin.setEnabled(true);
        workoutDurationMin.setEnabled(true);
        loadPlanContent();

        ImageButton btnAssign = findViewById(R.id.btnAssign);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            finish();
        });

        ImageButton btnReminder = findViewById(R.id.btnReminder);
        btnReminder.setOnClickListener(v -> {
            Intent i = new Intent(this, TrainerRemindActivity01.class);
            i.putExtra("userID", currentTrainerId);
            startActivity(i);
        });



        btnAssign.setOnClickListener(v -> {
            savePlanNow();
            Intent i = new Intent(this, TrainerPlanActivity3.class);
            i.putExtra("planId", planId);
            i.putExtra("userID", currentTrainerId);
            startActivity(i);
        });

        Button btnProfileOfProfile1 = findViewById(R.id.btnProfileOfProfile1);
        btnProfileOfProfile1.setOnClickListener(v -> {
            Intent i = new Intent(this, TrainerProfileActivity1.class);
            i.putExtra("userID", currentTrainerId);
            startActivity(i);
        });

        Button btnTrackOfProfile1 = findViewById(R.id.btnTrackOfProfile1);
        btnTrackOfProfile1.setOnClickListener(v -> {
            Intent i = new Intent(this, TrainerTrackActivity01.class);
            i.putExtra("userID", currentTrainerId);
            startActivity(i);
        });
    }


    private void loadPlanContent() {
        Cursor c = dbHelper.getPlanById(planId);
        if (c != null && c.moveToFirst()) {
            try {
                etExerciseName.setText(c.getString(c.getColumnIndexOrThrow("ExerciseName")));
                etExerciseItem1.setText(c.getString(c.getColumnIndexOrThrow("item1")));
                item1Reps.setText(c.getString(c.getColumnIndexOrThrow("reps1")));
                item1Sets.setText(c.getString(c.getColumnIndexOrThrow("sets1")));
                etExerciseItem2.setText(c.getString(c.getColumnIndexOrThrow("item2")));
                item2Reps.setText(c.getString(c.getColumnIndexOrThrow("reps2")));
                item2Sets.setText(c.getString(c.getColumnIndexOrThrow("sets2")));
                etExerciseItem3.setText(c.getString(c.getColumnIndexOrThrow("item3")));
                item3Reps.setText(c.getString(c.getColumnIndexOrThrow("reps3")));
                item3Sets.setText(c.getString(c.getColumnIndexOrThrow("sets3")));
                restDurationMin.setText(c.getString(c.getColumnIndexOrThrow("rest_duration")));
                workoutDurationMin.setText(c.getString(c.getColumnIndexOrThrow("workout_duration")));

                if (etExerciseItem1.getText().toString().isEmpty()) {
                    String oldContent = c.getString(c.getColumnIndexOrThrow("ExerciseContent"));
                    if (oldContent != null) {
                        String[] parts = oldContent.split(",");
                        if (parts.length >= 11) {
                            etExerciseItem1.setText(parts[0]);
                            item1Reps.setText(parts[1]);
                            item1Sets.setText(parts[2]);
                            etExerciseItem2.setText(parts[3]);
                            item2Reps.setText(parts[4]);
                            item2Sets.setText(parts[5]);
                            etExerciseItem3.setText(parts[6]);
                            item3Reps.setText(parts[7]);
                            item3Sets.setText(parts[8]);
                            restDurationMin.setText(parts[9]);
                            workoutDurationMin.setText(parts[10]);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            c.close();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        savePlanNow();
    }

    private void savePlanNow() {
        String name = etExerciseName.getText().toString().trim();
        String item1 = etExerciseItem1.getText().toString().trim();
        String reps1 = item1Reps.getText().toString().trim();
        String sets1 = item1Sets.getText().toString().trim();
        String item2 = etExerciseItem2.getText().toString().trim();
        String reps2 = item2Reps.getText().toString().trim();
        String sets2 = item2Sets.getText().toString().trim();
        String item3 = etExerciseItem3.getText().toString().trim();
        String reps3 = item3Reps.getText().toString().trim();
        String sets3 = item3Sets.getText().toString().trim();
        String restDuration = restDurationMin.getText().toString().trim();
        String workoutDuration = workoutDurationMin.getText().toString().trim();
        boolean success = dbHelper.updatePlan(planId, name, item1, reps1, sets1, item2, reps2, sets2, item3, reps3, sets3, restDuration, workoutDuration);
        if (success && !isFinishing()) {
            Toast.makeText(this, "Automatically saved", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isEditing) {
            loadPlanContent();
        }
    }

}
