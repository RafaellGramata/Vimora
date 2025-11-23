package com.example.vimora.trainer;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.vimora.DatabaseHelper;
import com.example.vimora.R;

import java.util.ArrayList;

public class TrainerPlanActivity1 extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private ListView listViewExercises;
    private EditText etSearch;
    private ImageButton btnAdd;
    private long currentTrainerId;

    private ArrayAdapter<String> adapter;
    private ArrayList<String> exerciseNames;
    private ArrayList<Long> exerciseIds;
    private ArrayList<String> allExerciseNames;
    private ArrayList<Long> allExerciseIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        dbHelper = new DatabaseHelper(this);
        currentTrainerId = getIntent().getLongExtra("userID", -1);
        setContentView(R.layout.activity_trainer_plan1);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tvName), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        listViewExercises = findViewById(R.id.listViewTrainees);
        etSearch = findViewById(R.id.etSearchExercise);
        btnAdd = findViewById(R.id.btnAdd);

        exerciseNames = new ArrayList<>();
        exerciseIds = new ArrayList<>();
        allExerciseNames = new ArrayList<>();
        allExerciseIds = new ArrayList<>();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, exerciseNames);
        listViewExercises.setAdapter(adapter);

        ImageButton btnReminder = findViewById(R.id.btnReminder);
        btnReminder.setOnClickListener(v -> {
            Intent intent = new Intent(this, TrainerRemindActivity01.class);
            intent.putExtra("userID", currentTrainerId);
            startActivity(intent);
        });

        Button btnProfileOfProfile1 = findViewById(R.id.btnProfileOfProfile1);
        btnProfileOfProfile1.setOnClickListener(v -> {
            Intent intent = new Intent(this, TrainerProfileActivity1.class);
            intent.putExtra("userID", currentTrainerId);
            startActivity(intent);
        });

        Button btnTrackOfProfile1 = findViewById(R.id.btnTrackOfProfile1);
        btnTrackOfProfile1.setOnClickListener(v -> {
            Intent intent = new Intent(this, TrainerTrackActivity01.class);
            intent.putExtra("userID", currentTrainerId);
            startActivity(intent);
        });

        Button btnPlanOfProfile1 = findViewById(R.id.btnPlanOfProfile1);
        btnPlanOfProfile1.setOnClickListener(v -> {
            Intent intent = new Intent(this, TrainerPlanActivity1.class);
            intent.putExtra("userID", currentTrainerId);
            startActivity(intent);
        });

        btnAdd.setOnClickListener(v -> showAddDialog());
        listViewExercises.setOnItemLongClickListener((parent, view, position, id) -> {
            long planId = exerciseIds.get(position);
            String name = exerciseNames.get(position);

            new AlertDialog.Builder(this)
                    .setTitle("Delete")
                    .setMessage("Delete \"" + name + "\"?")
                    .setPositiveButton("Yes", (d, w) -> {
                        dbHelper.deletePlan(planId);
                        loadExercises();
                        Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", null)
                    .show();
            return true;
        });

        listViewExercises.setOnItemClickListener((parent, view, position, id) -> {
            long planId = exerciseIds.get(position);
            Intent intent = new Intent(this, TrainerPlanActivity2.class);
            intent.putExtra("planId", planId);
            intent.putExtra("userID", currentTrainerId);
            startActivity(intent);
        });

        loadExercises();
        setupSearch();
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterExercises(s.toString());
            }
        });
    }

    private void loadExercises() {
        exerciseNames.clear();
        exerciseIds.clear();
        allExerciseNames.clear();
        allExerciseIds.clear();

        android.database.Cursor cursor = dbHelper.getAllPlans();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow("PlanID"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("ExerciseName"));

                allExerciseNames.add(name);
                allExerciseIds.add(id);
            } while (cursor.moveToNext());
            cursor.close();
        }

        String currentQuery = etSearch.getText().toString();
        filterExercises(currentQuery);
    }

    private void filterExercises(String query) {
        exerciseNames.clear();
        exerciseIds.clear();

        if (query.isEmpty()) {
            exerciseNames.addAll(allExerciseNames);
            exerciseIds.addAll(allExerciseIds);
        } else {
            String lowerQuery = query.toLowerCase();
            for (int i = 0; i < allExerciseNames.size(); i++) {
                if (allExerciseNames.get(i).toLowerCase().contains(lowerQuery)) {
                    exerciseNames.add(allExerciseNames.get(i));
                    exerciseIds.add(allExerciseIds.get(i));
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void showAddDialog() {
        EditText input = new EditText(this);
        input.setHint("Exercise Name");

        new AlertDialog.Builder(this)
                .setTitle("Add Exercise")
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        if (dbHelper.addPlan(name, "")) { // content 留空，之後編輯
                            loadExercises();
                            Toast.makeText(this, "Added", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadExercises();
    }
}
