package com.example.vimora.trainer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import androidx.appcompat.app.AppCompatActivity;
import com.example.vimora.DatabaseHelper;
import com.example.vimora.R;

import java.util.ArrayList;
import java.util.List;

public class TrainerTrackActivity01 extends AppCompatActivity {
    DatabaseHelper databaseHelper;
    SimpleCursorAdapter traineeAdapter;
    long trainerID;
    List<TraineeData> allTrainees;
    List<TraineeData> filteredTrainees;

    // Simple inner class to hold trainee data
    private static class TraineeData {
        long id;
        String name;
        TraineeData(long id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer_track01);

        databaseHelper = new DatabaseHelper(this);
        Intent intent = getIntent();
        trainerID = intent.getLongExtra("userID", -1);

        Button btnProfile = findViewById(R.id.btnProfileOfTrack);
        Button btnPlan = findViewById(R.id.btnPlanOfTrack);
        ImageButton btnReminder = findViewById(R.id.btnReminder);
        EditText editTxtSearch = findViewById(R.id.editTxtTrainerName);
        ListView listView = findViewById(R.id.listView);

        // Load all trainees from database
        loadTrainees();

        // Setup SimpleCursorAdapter for trainees
        traineeAdapter = new SimpleCursorAdapter(
                this,
                R.layout.list_trainee_entry,
                createCursorFromList(allTrainees),
                new String[]{"name"},
                new int[]{R.id.traineeListName},
                0
        );

        listView.setAdapter(traineeAdapter);

        // Search functionality
        editTxtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTrainees(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // ListView item click listener - navigate to TrainerTrackActivity02
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("Range")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                int idIndex = cursor.getColumnIndex("userID");
                if (idIndex != -1) {
                    long traineeID = cursor.getLong(idIndex);
                    Intent newIntent = new Intent(TrainerTrackActivity01.this, TrainerTrackActivity02.class);
                    newIntent.putExtra("userID", trainerID);
                    newIntent.putExtra("traineeID", traineeID);
                    startActivity(newIntent);
                }
            }
        });

        // Navigation buttons
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TrainerTrackActivity01.this, TrainerProfileActivity1.class);
                i.putExtra("userID", trainerID);
                startActivity(i);
            }
        });

        btnPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TrainerTrackActivity01.this, TrainerPlanActivity1.class);
                i.putExtra("userID", trainerID);
                startActivity(i);
            }
        });

        btnReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TrainerTrackActivity01.this, TrainerRemindActivity01.class);
                i.putExtra("userID", trainerID);
                startActivity(i);
            }
        });
    }

    @SuppressLint("Range")
    private void loadTrainees() {
        allTrainees = new ArrayList<>();
        Cursor cursor = databaseHelper.getTraineesByTrainer(trainerID);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndex("userID"));
                String name = cursor.getString(cursor.getColumnIndex("name"));
                allTrainees.add(new TraineeData(id, name));
            } while (cursor.moveToNext());
            cursor.close();
        }

        filteredTrainees = new ArrayList<>(allTrainees);
    }

    private void filterTrainees(String query) {
        if (query == null || query.trim().isEmpty()) {
            filteredTrainees = new ArrayList<>(allTrainees);
        } else {
            filteredTrainees = new ArrayList<>();
            String lowerQuery = query.toLowerCase();
            for (TraineeData trainee : allTrainees) {
                if (trainee.name.toLowerCase().contains(lowerQuery)) {
                    filteredTrainees.add(trainee);
                }
            }
        }

        // Update adapter with filtered cursor
        traineeAdapter.changeCursor(createCursorFromList(filteredTrainees));
    }

    private Cursor createCursorFromList(List<TraineeData> trainees) {
        MatrixCursor cursor = new MatrixCursor(new String[]{"_id", "userID", "name"});
        for (TraineeData trainee : trainees) {
            cursor.addRow(new Object[]{trainee.id, trainee.id, trainee.name});
        }
        return cursor;
    }
}