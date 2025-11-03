package com.example.vimora.trainer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.example.vimora.R;

public class TrainerPlanActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer_plan);

        View trackCard = findViewById(R.id.trackCard);
        View profileCard = findViewById(R.id.profileCard);

        trackCard.setOnClickListener(v ->
                startActivity(new Intent(this, TrainerTrackActivity.class)));

        profileCard.setOnClickListener(v ->
                startActivity(new Intent(this, TrainerProfileActivity.class)));
    }
}
