package com.example.vimora.trainee;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.vimora.DatabaseHelper;
import com.example.vimora.R;
import com.example.vimora.WelcomeActivity;

public class TraineeProfileActivity extends AppCompatActivity {
    DatabaseHelper databaseHelper;
    int height;
    int weight;
    TextView txtBMI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_trainee_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        databaseHelper = new DatabaseHelper(this);
        Intent intent = getIntent();
        long userID = intent.getLongExtra("userID",-1); // must exist!

        // Save userID to SharedPreferences for nutrition tracking activities
        SharedPreferences prefs = getSharedPreferences("VimoraPrefs", MODE_PRIVATE);
        prefs.edit().putLong("userID", userID).apply();

        Button btnPlan = findViewById(R.id.btnPlanOfProfile);
        Button btnTrack = findViewById(R.id.btnTrackOfProfile);
        ImageButton btnLogout = findViewById(R.id.btnLogout);
        ImageButton btnReminder = findViewById(R.id.btnReminder);

        TextView name = findViewById(R.id.editTxtTraineeName);
        TextView txtHeight = findViewById(R.id.editTxtTraineeHeight);
        TextView txtWeight = findViewById(R.id.editTxtTraineeWeight);
        TextView txtAge = findViewById(R.id.editTxtTraineeAge);
        txtBMI = findViewById(R.id.txtOutputBMI);
        TextView txtTrainerName = findViewById(R.id.txtTrainerName);

        height = databaseHelper.getTraineeHeight(userID);
        weight = databaseHelper.getLatestWeight(userID);

        long trainerID = databaseHelper.getTraineeTrainer(userID);
        String trainerName = trainerID>=0?databaseHelper.getName(trainerID):"";

        name.setText(databaseHelper.getName(userID));
        txtHeight.setText(Integer.toString(height));
        txtWeight.setText(Integer.toString(weight));
        txtAge.setText(Integer.toString(databaseHelper.getTraineeAge(userID)));
        updateBMI();
        txtTrainerName.setText(trainerName);

        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                databaseHelper.setName(userID,editable.toString());
            }
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        });

        txtHeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                height = Integer.parseInt(editable.toString());
                databaseHelper.setTraineeHeight(userID,height);
                updateBMI();
            }
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        });

        txtWeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                weight = Integer.parseInt(editable.toString());
                databaseHelper.addWeightSnapshot(userID,weight);
                updateBMI();
            }
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        });

        txtAge.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                databaseHelper.setTraineeAge(userID,Integer.parseInt(editable.toString()));
            }
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        });

        btnPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newIntent = new Intent(TraineeProfileActivity.this, TraineePlanActivity01.class);
                newIntent.putExtra("userID",userID);
                startActivity(newIntent);
            }
        });

        // UPDATED: Navigate to TraineeTrackActivity04 (Daily Meal Tracking)
        btnTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newIntent = new Intent(TraineeProfileActivity.this, TraineeTrackActivity01.class);
                newIntent.putExtra("userID",userID);
                startActivity(newIntent);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(TraineeProfileActivity.this, WelcomeActivity.class));
            }
        });
        btnReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newIntent = new Intent(TraineeProfileActivity.this, TraineeRemindActivity.class);
                newIntent.putExtra("userID",userID);
                startActivity(newIntent);
            }
        });
        txtTrainerName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newIntent = new Intent(TraineeProfileActivity.this, TraineeListTrainersActivity.class);
                newIntent.putExtra("userID",userID);
                startActivity(newIntent);
            }
        });
    }

    private void updateBMI() {
        float BMI = ((float)weight*10000)/((float)height*(float)height); // formula for kg and cm. change if we change units.
        txtBMI.setText(Float.toString(BMI));
    }
}