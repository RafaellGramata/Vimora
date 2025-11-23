package com.example.vimora.trainee;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TraineePlanActivity01 extends AppCompatActivity {
    DatabaseHelper databaseHelper;
    Calendar date;
    TextView txtDate;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    long userID;
    ImageButton btnChoose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_trainee_plan01);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tvName), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseHelper = new DatabaseHelper(this);
        Intent intent = getIntent();
        userID = intent.getLongExtra("userID", -1);
        date = Calendar.getInstance();

        ImageButton btnReminder = findViewById(R.id.btnReminder);
        Button btnTrack = findViewById(R.id.btnTrackOfPlan);
        Button btnProfile = findViewById(R.id.btnProfileOfPlan);
        txtDate = findViewById(R.id.txtDate);
        btnChoose = findViewById(R.id.btnChoose);

        updateDate();
        updateCompletionButton();

        Button previousDay = findViewById(R.id.btnPlanYesterday);
        Button nextDay = findViewById(R.id.btnPlanNextday);

        previousDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                date.add(Calendar.DAY_OF_MONTH, -1);
                updateDate();
                updateCompletionButton();
            }
        });

        nextDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                date.add(Calendar.DAY_OF_MONTH, 1);
                updateDate();
                updateCompletionButton();
            }
        });

        // Mark as Complete button functionality
        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String currentDate = sdf.format(date.getTime());

                // Using planID = 1 as default
                // TODO: If you have different plans, you'll need to get the actual planID
                long planID = 1;

                // Check if already completed
                if (databaseHelper.isWorkoutCompletedOnDate(userID, currentDate)) {
                    // Already completed, offer to unmark
                    boolean success = databaseHelper.unmarkWorkoutComplete(userID, currentDate);
                    if (success) {
                        Toast.makeText(TraineePlanActivity01.this,
                                "Workout unmarked for " + currentDate,
                                Toast.LENGTH_SHORT).show();
                        updateCompletionButton();
                    }
                } else {
                    // Not completed yet, mark as complete
                    boolean success = databaseHelper.markWorkoutComplete(userID, planID, currentDate);

                    if (success) {
                        Toast.makeText(TraineePlanActivity01.this,
                                "Workout marked as complete! ✓",
                                Toast.LENGTH_SHORT).show();
                        updateCompletionButton();
                    } else {
                        Toast.makeText(TraineePlanActivity01.this,
                                "Error marking workout",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        btnTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newIntent = new Intent(TraineePlanActivity01.this, TraineeTrackActivity01.class);
                newIntent.putExtra("userID", userID);
                startActivity(newIntent);
            }
        });

        btnReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newIntent = new Intent(TraineePlanActivity01.this, TraineeRemindActivity.class);
                newIntent.putExtra("userID", userID);
                startActivity(newIntent);
            }
        });

        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newIntent = new Intent(TraineePlanActivity01.this, TraineeProfileActivity.class);
                newIntent.putExtra("userID", userID);
                startActivity(newIntent);
            }
        });
    }

    private void updateDate() {
        txtDate.setText(sdf.format(date.getTime()));
    }

    /**
     * Update the completion button appearance based on whether
     * the current date's workout is completed
     */
    private void updateCompletionButton() {
        String currentDate = sdf.format(date.getTime());
        boolean isCompleted = databaseHelper.isWorkoutCompletedOnDate(userID, currentDate);

        if (isCompleted) {
            // Make button appear checked/completed
            btnChoose.setAlpha(0.5f); // Semi-transparent to show it's already done
            btnChoose.setColorFilter(0xFF4CAF50); // Green tint
        } else {
            // Normal appearance
            btnChoose.setAlpha(1.0f);
            btnChoose.clearColorFilter();
        }
    }
}