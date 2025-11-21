package com.example.vimora.trainee;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
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

public class TraineeProfileActivity01 extends AppCompatActivity {
    DatabaseHelper databaseHelper;
    @SuppressLint("Range")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_trainee_profile02);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        databaseHelper = new DatabaseHelper(this);
        Intent intent = getIntent();
        long userID = intent.getLongExtra("userID",-1);
        long trainerID = intent.getLongExtra("trainerID",-1);

        ImageButton btnChoose = findViewById(R.id.btnChoose);
        ImageButton btnBack = findViewById(R.id.btnBack);
        TextView txtName = findViewById(R.id.txtName);
        TextView txtTrainerName = findViewById(R.id.txtTrainerName);
        TextView trainerSpecialization = findViewById(R.id.traineeTrainerSpecialization);
        TextView trainerDescription = findViewById(R.id.traineeTrainerDescription);
        TextView traineeCount = findViewById(R.id.traineeTrainerCount);

        Cursor trainerProfile = databaseHelper.getTrainerProfile(trainerID);
        trainerProfile.moveToFirst();
        String trainerName = trainerProfile.getString(trainerProfile.getColumnIndex("name"));
        int trainerHandleNum = trainerProfile.getInt(trainerProfile.getColumnIndex("trainerHandleNum"));
        int trainees = databaseHelper.getTraineeCount(trainerID);
        boolean trainerCanAcceptTrainees = trainees<trainerHandleNum;
        txtName.setText(trainerName);
        txtTrainerName.setText(trainerName); // don't know why we have two of these but here we are
        trainerSpecialization.setText(trainerProfile.getString(trainerProfile.getColumnIndex("trainerSpecialization")));
        trainerDescription.setText(trainerProfile.getString(trainerProfile.getColumnIndex("trainerAbout")));
        traineeCount.setText(trainees+"/"+trainerHandleNum);

        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (databaseHelper.getTraineeTrainer(userID)==trainerID) {
                    Toast.makeText(TraineeProfileActivity01.this,"This is already your trainer.",Toast.LENGTH_LONG).show();
                }
                else if (trainerCanAcceptTrainees) {
                    databaseHelper.setTraineeTrainer(userID, trainerID);
                    Intent newIntent = new Intent(TraineeProfileActivity01.this, TraineeProfileActivity.class);
                    newIntent.putExtra("userID", userID);
                    startActivity(newIntent);
                }
                else Toast.makeText(TraineeProfileActivity01.this,"Trainer cannot accept more trainees.",Toast.LENGTH_LONG).show();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newIntent = new Intent(TraineeProfileActivity01.this, TraineeProfileActivity.class);
                newIntent.putExtra("userID",userID);
                startActivity(newIntent);
            }
        });
    }
}