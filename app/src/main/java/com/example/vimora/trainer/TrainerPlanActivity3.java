package com.example.vimora.trainer;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.vimora.DatabaseHelper;
import com.example.vimora.R;

import java.util.ArrayList;

public class TrainerPlanActivity3 extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private ListView listViewTrainees;
    private ImageButton btnSave;
    private long planId;
    private ArrayList<Long> traineeIds;
    private ArrayList<String> traineeNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        dbHelper = new DatabaseHelper(this);
        setContentView(R.layout.activity_trainer_plan3);
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
        loadTrainees();
        setupButtons();
    }

    private void initViews() {
        listViewTrainees = findViewById(R.id.listViewTrainees);
        btnSave = findViewById(R.id.btnSave);
        traineeIds = new ArrayList<>();
        traineeNames = new ArrayList<>();
    }

    private void loadTrainees() {
        Cursor cursor = dbHelper.getAllTrainees();
        traineeIds.clear();
        traineeNames.clear();

        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow("userID"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            traineeIds.add(id);
            traineeNames.add(name + "\nTrainee");
        }
        cursor.close();

        listViewTrainees.setAdapter(new android.widget.ArrayAdapter<>(
                this, android.R.layout.simple_list_item_single_choice, traineeNames));
    }
    private void setupButtons() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnReminder).setOnClickListener(v ->
                startActivity(new Intent(this, TrainerProfileActivity2.class)));

        btnSave.setOnClickListener(v -> {
            int pos = listViewTrainees.getCheckedItemPosition();
            if (pos != ListView.INVALID_POSITION) {
                long traineeId = traineeIds.get(pos);
                if (dbHelper.assignPlanToTrainee(planId, traineeId)) {
                    Toast.makeText(this, "Assigned successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                Toast.makeText(this, "Please select a trainee", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btnProfileOfProfile1).setOnClickListener(v ->
                startActivity(new Intent(this, TrainerProfileActivity1.class)));
        findViewById(R.id.btnTrackOfProfile1).setOnClickListener(v ->
                startActivity(new Intent(this, TrainerTrackActivity.class)));
    }
}
