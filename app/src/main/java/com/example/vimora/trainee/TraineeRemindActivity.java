package com.example.vimora.trainee;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.vimora.DatabaseHelper;
import com.example.vimora.R;
import com.example.vimora.trainer.TrainerRemindActivity02;
import com.example.vimora.trainer.TrainerTrackActivity;

public class TraineeRemindActivity extends AppCompatActivity {
    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_trainee_remind01);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tvName), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        databaseHelper = new DatabaseHelper(this);
        Intent intent = getIntent();
        long userID = intent.getLongExtra("userID",-1);

        TextView trainerName = findViewById(R.id.txtCoach);
        long trainerID = databaseHelper.getTraineeTrainer(userID);
        if (trainerID!=-1) {
            trainerName.setText(databaseHelper.getName(trainerID));

            ListView listReminders = findViewById(R.id.listReminders);

            SimpleCursorAdapter remindersAdapter = new SimpleCursorAdapter(
                    TraineeRemindActivity.this,
                    R.layout.list_reminders_entry,
                    databaseHelper.getRemindersForTraineeTrainer(userID, trainerID),
                    new String[]{"RemindDate", "RemindContent"},
                    new int[]{R.id.reminderDate, R.id.reminderMessage}, 0
            );
            listReminders.setAdapter(remindersAdapter);

            listReminders.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @SuppressLint("Range")
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Cursor cursor = (Cursor) adapterView.getItemAtPosition(i);
                    int idIndex = cursor.getColumnIndex("RemindID");
                    if (idIndex != -1) {
                        long remindID = cursor.getLong(idIndex);
                        Intent newIntent = new Intent(TraineeRemindActivity.this, TraineeRemindActivity02.class);
                        newIntent.putExtra("userID", userID);
                        newIntent.putExtra("remindID", remindID);
                        startActivity(newIntent);
                    }
                }
            });
        } else trainerName.setText("Must select trainer.");

        ImageButton back = findViewById(R.id.btnCloseOfReminder);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newIntent = new Intent(TraineeRemindActivity.this, TraineeProfileActivity.class);
                newIntent.putExtra("userID", userID);
                startActivity(newIntent);
            }
        });
    }
}