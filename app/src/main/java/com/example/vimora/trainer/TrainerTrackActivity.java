package com.example.vimora.trainer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import com.example.vimora.R;

public class TrainerTrackActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer_track01);
        Intent intent = getIntent();
        long userID = intent.getLongExtra("userID", -1);

        Button btnProfile = findViewById(R.id.btnProfileOfTrack);
        Button btnPlan = findViewById(R.id.btnPlanOfTrack);
        ImageButton btnReminder = findViewById(R.id.btnReminder);

        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TrainerTrackActivity.this, TrainerProfileActivity1.class);
                i.putExtra("userID", userID);
                startActivity(i);
            }
        });

        btnPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TrainerTrackActivity.this, TrainerPlanActivity1.class);
                i.putExtra("userID", userID);
                startActivity(i);
            }
        });

        btnReminder.setOnClickListener(v -> {
            Intent i = new Intent(this, TrainerRemindActivity.class);
            i.putExtra("userID", userID);
            startActivity(i);
        });
    }
}