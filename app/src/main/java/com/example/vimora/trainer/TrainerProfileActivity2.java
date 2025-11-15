package com.example.vimora.trainer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.vimora.R;

public class TrainerProfileActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_trainer_profile2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.toTrainerProfile00), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnTrack = findViewById(R.id.btnTrackOfProfile1);
        Button btnPlan = findViewById(R.id.btnPlanOfProfile1);
        ImageButton btnProfile1 = findViewById(R.id.toTrainerProfile1);
        ImageButton btnReminder = findViewById(R.id.btnReminder);

        btnReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TrainerProfileActivity2.this, TrainerProfileActivity2.class);
                startActivity(intent);
            }
        });

        btnProfile1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TrainerProfileActivity2.this, TrainerProfileActivity1.class);
                startActivity(intent);
            }
        });

        btnTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TrainerProfileActivity2.this, TrainerTrackActivity.class);
                startActivity(intent);
            }
        });

        btnPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TrainerProfileActivity2.this, TrainerPlanActivity.class);
                startActivity(intent);
            }
        });

        TextView outputAssignNum2 = findViewById(R.id.outputAssignNum2);
        TextView outputHandleNum = findViewById(R.id.outputHandleNum);
    }
}
