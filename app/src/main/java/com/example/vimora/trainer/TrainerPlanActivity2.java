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

        planId = getIntent().getLongExtra("planId", -1);
        if (planId == -1) {
            finish();
            return;
        }

        etExercisePlan = findViewById(R.id.etExercisePlan);
        etExercisePlan.setEnabled(true);
        loadPlanContent();

        ImageButton btnAssign = findViewById(R.id.btnAssign);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v ->
                startActivity(new Intent(this, TrainerProfileActivity1.class)));

        ImageButton btnReminder = findViewById(R.id.btnReminder);
        btnReminder.setOnClickListener(v ->
                startActivity(new Intent(this, TrainerProfileActivity2.class)));



        btnAssign.setOnClickListener(v -> {
            savePlanNow();
            Intent intent = new Intent(this, TrainerPlanActivity3.class);
            intent.putExtra("planId", planId);
            startActivity(intent);
        });

        Button btnProfileOfProfile1 = findViewById(R.id.btnProfileOfProfile1);
        btnProfileOfProfile1.setOnClickListener(v ->
                startActivity(new Intent(this, TrainerProfileActivity1.class)));

        Button btnTrackOfProfile1 = findViewById(R.id.btnTrackOfProfile1);
        btnTrackOfProfile1.setOnClickListener(v ->
                startActivity(new Intent(this, TrainerTrackActivity.class)));
    }


    private void loadPlanContent() {
        Cursor c = dbHelper.getAllPlans();
        while (c.moveToNext()) {
            if (c.getLong(c.getColumnIndexOrThrow("PlanID")) == planId) {
                String content = c.getString(c.getColumnIndexOrThrow("ExerciseContent"));
                etExercisePlan.setText(content);
                etExercisePlan.setSelection(content.length());
                break;
            }
        }
        c.close();
    }

    @Override
    protected void onPause() {
        super.onPause();
        savePlanNow();
    }

    private void savePlanNow() {
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
