package com.example.vimora.trainee;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.vimora.R;
import com.example.vimora.WelcomeActivity;

public class TraineeTrackActivity01 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_trainee_track02);

        // Find the root view - use tvName instead of main
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tvName), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get userID from intent
        Intent intent = getIntent();
        long userID = intent.getLongExtra("userID", -1);

        // Save to SharedPreferences for other activities
        SharedPreferences prefs = getSharedPreferences("VimoraPrefs", MODE_PRIVATE);
        prefs.edit().putLong("userID", userID).apply();

        // Initialize buttons from activity_trainee_track02.xml
        Button btnPlan = findViewById(R.id.btnPlanOfTrack00);
        Button btnProfile = findViewById(R.id.btnProfileOfTrack00);
        Button btnTrack = findViewById(R.id.btnTrackOfTrack00);
        ImageButton btnMeal = findViewById(R.id.btnMeal);
        ImageButton btnInfo = findViewById(R.id.btnInfo);
        ImageButton btnLogout = findViewById(R.id.btnLogout);
        ImageButton btnReminder = findViewById(R.id.btnReminder);

        // Navigation listeners
        btnPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newIntent = new Intent(TraineeTrackActivity01.this, TraineePlanActivity01.class);
                newIntent.putExtra("userID", userID);
                startActivity(newIntent);
            }
        });

        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newIntent = new Intent(TraineeTrackActivity01.this, TraineeProfileActivity.class);
                newIntent.putExtra("userID", userID);
                startActivity(newIntent);
            }
        });

        // Meal button - goes to nutrition tracking (TraineeTrackActivity04)
        btnMeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newIntent = new Intent(TraineeTrackActivity01.this, TraineeTrackActivity04.class);
                newIntent.putExtra("userID", userID);
                startActivity(newIntent);
            }
        });

        // Back/Info button
        btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Go back to previous screen
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TraineeTrackActivity01.this, WelcomeActivity.class));
                finish();
            }
        });

        btnReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newIntent = new Intent(TraineeTrackActivity01.this, TraineeRemindActivity.class);
                newIntent.putExtra("userID", userID);
                startActivity(newIntent);
            }
        });
    }
}