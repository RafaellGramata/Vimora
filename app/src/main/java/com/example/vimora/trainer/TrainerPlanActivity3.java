package com.example.vimora.trainer;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
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

        ListView listViewTrainees = findViewById(R.id.listViewTrainees);

        traineeIds = new ArrayList<>();
        traineeNames = new ArrayList<>();

        ImageButton btnBack2 = findViewById(R.id.btnBack2);
        btnBack2.setOnClickListener(v -> finish());

        ImageButton btnReminder = findViewById(R.id.btnReminder);
        btnReminder.setOnClickListener(v ->
                startActivity(new Intent(this, TrainerProfileActivity2.class)));

        ImageButton btnSave2 = findViewById(R.id.btnSave2);
        btnSave2.setOnClickListener(v -> {
            int position = listViewTrainees.getCheckedItemPosition();
            if (position != ListView.INVALID_POSITION) {
                long traineeId = traineeIds.get(position);
                if (dbHelper.assignPlanToTrainee(planId, traineeId)) {
                    Toast.makeText(this, "Assigned successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                Toast.makeText(this, "Please select a trainee", Toast.LENGTH_SHORT).show();
            }
        });

        Button btnProfileOfProfile1 = findViewById(R.id.btnProfileOfProfile1);
        btnProfileOfProfile1.setOnClickListener(v ->
                startActivity(new Intent(this, TrainerProfileActivity1.class)));

        Button btnTrackOfProfile1 = findViewById(R.id.btnTrackOfProfile1);
        btnTrackOfProfile1.setOnClickListener(v ->
                startActivity(new Intent(this, TrainerTrackActivity.class)));

        loadTrainees();
        setupButtons();
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

    }
}
