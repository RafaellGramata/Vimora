package com.example.vimora.trainer;


import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.vimora.DatabaseHelper;
import com.example.vimora.R;

public class TrainerProfileActivity1 extends AppCompatActivity {

    private EditText etSpec, etName, etHandleNum, etAbout;
    private TextView tvTraineeCount;
    private DatabaseHelper dbHelper;
    private long currentTrainerId;
    private static final String PREF_NAME = "TrainerProfilePref";
    private static final String KEY_TRAINER_ID = "trainerId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_trainer_profile1);
        dbHelper = new DatabaseHelper(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tvName), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        currentTrainerId = pref.getLong(KEY_TRAINER_ID, -1);
        if (currentTrainerId == -1) {
            Toast.makeText(this, "Please log in!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupButtons();
        loadProfile();
    }

    private void initViews() {
        etSpec      = findViewById(R.id.inputTrainerSpec);
        etName      = findViewById(R.id.outputTraineeName);
        etHandleNum = findViewById(R.id.inputHandleNum);
        etAbout     = findViewById(R.id.inputTrainerInfo);
        tvTraineeCount = findViewById(R.id.outputAssignNum);

        tvTraineeCount.setEnabled(false);
    }

    private void setupButtons() {
        Button btnTrack = findViewById(R.id.btnTrackOfProfile1);
        Button btnPlan = findViewById(R.id.btnPlanOfProfile1);
        ImageButton btnProfile2 = findViewById(R.id.toTrainerProfile2);
        ImageButton btnReminder = findViewById(R.id.btnReminder);

        btnReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TrainerProfileActivity1.this, TrainerProfileActivity2.class);
                startActivity(intent);
            }
        });

        btnProfile2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TrainerProfileActivity1.this, TrainerProfileActivity2.class);
                startActivity(intent);
            }
        });

        btnTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TrainerProfileActivity1.this, TrainerTrackActivity.class);
                startActivity(intent);
            }
        });

        btnPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TrainerProfileActivity1.this, TrainerPlanActivity1.class);
                startActivity(intent);
            }
        });

    }
    private void loadProfile() {
        Cursor c = dbHelper.getTrainerProfile(currentTrainerId);
        if (c.moveToFirst()) {
            etName.setText(c.getString(c.getColumnIndexOrThrow("name")));
            etSpec.setText(c.getString(c.getColumnIndexOrThrow("trainerSpecialization")));
            etHandleNum.setText(String.valueOf(c.getInt(c.getColumnIndexOrThrow("trainerHandleNum"))));
            etAbout.setText(c.getString(c.getColumnIndexOrThrow("trainerAbout")));
        }
        c.close();

        int count = dbHelper.countTrainees(currentTrainerId);
        tvTraineeCount.setText(String.valueOf(count));
    }

    private void saveProfile() {
        String name   = etName.getText().toString().trim();
        String spec   = etSpec.getText().toString().trim();
        String about  = etAbout.getText().toString().trim();

        int handleNum = 0;
        try {
            handleNum = Integer.parseInt(etHandleNum.getText().toString().trim());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Handle Number should be a number", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = dbHelper.updateTrainerProfile(
                currentTrainerId, name, spec, handleNum, about);

        if (success) {
            Toast.makeText(this, "The data is saved.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Saving failed.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveProfile();
    }
        }
