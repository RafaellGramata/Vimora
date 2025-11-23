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
import com.example.vimora.WelcomeActivity;
import com.example.vimora.trainee.TraineeProfileActivity;

public class TrainerProfileActivity1 extends AppCompatActivity {

    private EditText etSpec, etName, etHandleNum, etAbout;
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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tvName), (v, insets) -> { // 注意：請確認 tvName ID 是否存在於 xml，若無可改用 main
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        currentTrainerId = intent.getLongExtra("userID", -1);
        if (currentTrainerId == -1) {
            SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            currentTrainerId = pref.getLong(KEY_TRAINER_ID, -1);
        }

        if (currentTrainerId == -1) {
            Toast.makeText(this, "User ID not found, please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        pref.edit().putLong(KEY_TRAINER_ID, currentTrainerId).apply();

        etSpec = findViewById(R.id.inputTrainerSpec);
        etName = findViewById(R.id.outputTraineeName);
        etHandleNum = findViewById(R.id.inputHandleNum);
        etAbout = findViewById(R.id.inputTrainerInfo);
        TextView tvTraineeCount = findViewById(R.id.outputAssignNum);
        tvTraineeCount.setEnabled(false);

        Button btnTrack = findViewById(R.id.btnTrackOfProfile1);
        Button btnPlan = findViewById(R.id.btnPlanOfProfile1);
        ImageButton btnProfile2 = findViewById(R.id.toTrainerProfile2);
        ImageButton btnReminder = findViewById(R.id.btnReminder);
        ImageButton btnLogout = findViewById(R.id.btnLogout);

        btnReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TrainerProfileActivity1.this, TrainerRemindActivity.class);
                intent.putExtra("userID", currentTrainerId);
                startActivity(intent);
            }
        });

        btnProfile2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TrainerProfileActivity1.this, TrainerProfileActivity2.class);
                intent.putExtra("userID", currentTrainerId);
                startActivity(intent);
            }
        });

        btnTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TrainerProfileActivity1.this, TrainerTrackActivity.class);
                intent.putExtra("userID", currentTrainerId);
                startActivity(intent);
            }
        });

        btnPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TrainerProfileActivity1.this, TrainerPlanActivity1.class);
                intent.putExtra("userID", currentTrainerId);
                startActivity(intent);
            }
        });
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(TrainerProfileActivity1.this, WelcomeActivity.class));
            }
        });

        Cursor c = dbHelper.getTrainerProfile(currentTrainerId);
        if (c.moveToFirst()) {
            try {
            etName.setText(c.getString(c.getColumnIndexOrThrow("name")));
            etSpec.setText(c.getString(c.getColumnIndexOrThrow("trainerSpecialization")));
            etHandleNum.setText(String.valueOf(c.getInt(c.getColumnIndexOrThrow("trainerHandleNum"))));
            etAbout.setText(c.getString(c.getColumnIndexOrThrow("trainerAbout")));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        c.close();

        int count = dbHelper.countTrainees(currentTrainerId);
        tvTraineeCount.setText(String.valueOf(count));

    }


    private void saveProfile() {

        if (etName == null || etSpec == null || etAbout == null || etHandleNum == null) {
            return;
        }
        String name   = etName.getText().toString().trim();
        String spec   = etSpec.getText().toString().trim();
        String about  = etAbout.getText().toString().trim();
        String handleStr = etHandleNum.getText().toString().trim();

        int handleNum = 0;
        if (!handleStr.isEmpty()) {
            try {
                handleNum = Integer.parseInt(handleStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid Handle Number", Toast.LENGTH_SHORT).show();
                return;
            }
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
