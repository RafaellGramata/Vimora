package com.example.vimora.trainer;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
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
    private ImageButton btnEdit, btnSave, btnAssign;
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

        initViews();
        setupButtons();
        loadPlanContent();
        setEditMode(false);
    }

    private void initViews() {
        etExercisePlan = findViewById(R.id.etExercisePlan);
        btnEdit = findViewById(R.id.btnEdit);
        btnSave = findViewById(R.id.btnSave);
        btnAssign = findViewById(R.id.btnAssign);
    }

    private void setupButtons() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnReminder).setOnClickListener(v ->
                startActivity(new Intent(this, TrainerProfileActivity2.class)));

        btnEdit.setOnClickListener(v -> setEditMode(true));

        btnSave.setOnClickListener(v -> {
            String content = etExercisePlan.getText().toString().trim();
            if (dbHelper.updatePlan(planId, null, content)) { // name 不變
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
                setEditMode(false);
            }
        });

        btnAssign.setOnClickListener(v -> {
            Intent intent = new Intent(this, TrainerPlanActivity3.class);
            intent.putExtra("planId", planId);
            startActivity(intent);
        });

        findViewById(R.id.btnProfileOfProfile1).setOnClickListener(v ->
                startActivity(new Intent(this, TrainerProfileActivity1.class)));
        findViewById(R.id.btnTrackOfProfile1).setOnClickListener(v ->
                startActivity(new Intent(this, TrainerTrackActivity.class)));
    }

    private void loadPlanContent() {
        Cursor c = dbHelper.getAllPlans();
        while (c.moveToNext()) {
            if (c.getLong(c.getColumnIndexOrThrow("PlanID")) == planId) {
                String content = c.getString(c.getColumnIndexOrThrow("ExerciseContent"));
                etExercisePlan.setText(content);
                break;
            }
        }
        c.close();
    }

    private void setEditMode(boolean enable) {
        isEditing = enable;
        etExercisePlan.setEnabled(enable);
        etExercisePlan.setBackgroundColor(enable ? 0xFFFFFFFF : 0xFFF4F4F4);
        btnEdit.setEnabled(!enable);
        btnSave.setEnabled(enable);
        btnAssign.setEnabled(!enable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isEditing) {
            loadPlanContent();
        }
    }

}
