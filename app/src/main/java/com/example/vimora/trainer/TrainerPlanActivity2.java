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
    private EditText etExercisePlan;
    private long planId;
    private long currentTrainerId;
    private boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        dbHelper = new DatabaseHelper(this);
        setContentView(R.layout.activity_trainer_plan2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.etExercisePlan), (v, insets) -> {
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

        etExercisePlan = findViewById(R.id.etExercisePlan);
        etExercisePlan.setEnabled(true);
        loadPlanContent();

        ImageButton btnAssign = findViewById(R.id.btnAssign);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            finish();
        });

        ImageButton btnReminder = findViewById(R.id.btnReminder);
        btnReminder.setOnClickListener(v -> {
            Intent i = new Intent(this, TrainerProfileActivity2.class);
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
            Intent i = new Intent(this, TrainerTrackActivity.class);
            i.putExtra("userID", currentTrainerId);
            startActivity(i);
        });
    }


    private void loadPlanContent() {
        Cursor c = dbHelper.getAllPlans();
        if (c != null) {
            while (c.moveToNext()) {
                try {
                    if (c.getLong(c.getColumnIndexOrThrow("PlanID")) == planId) {
                        String content = c.getString(c.getColumnIndexOrThrow("ExerciseContent"));
                        if (content != null) {
                            etExercisePlan.setText(content);
                            etExercisePlan.setSelection(content.length());
                        }
                        break;
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
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
        if (etExercisePlan == null) return;
        String content = etExercisePlan.getText().toString().trim();
        boolean success = dbHelper.updatePlan(planId, null, content);
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
