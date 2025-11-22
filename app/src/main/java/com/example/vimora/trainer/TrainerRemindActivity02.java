package com.example.vimora.trainer;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.vimora.DatabaseHelper;
import com.example.vimora.R;

public class TrainerRemindActivity02 extends AppCompatActivity {
    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        databaseHelper = new DatabaseHelper(this);
        setContentView(R.layout.activity_trainer_remind02);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Intent intent = getIntent();
        long userID = intent.getLongExtra("userID", -1);
        long traineeID = intent.getLongExtra("traineeId",-1);

        TextView txtTrainee = findViewById(R.id.txtTrainee);
        txtTrainee.setText(databaseHelper.getName(traineeID));

        EditText textRemind = findViewById(R.id.txtRemindInput);
        EditText txtDate = findViewById(R.id.editTextDate2);

        ListView listReminders = findViewById(R.id.listReminders);
        SimpleCursorAdapter remindersAdapter = new SimpleCursorAdapter(
                TrainerRemindActivity02.this,
                R.layout.list_reminders_entry,
                databaseHelper.getRemindersForTrainee(traineeID),
                new String[]{"RemindDate","RemindContent"},
                new int[]{R.id.reminderDate,R.id.reminderMessage},0
        );
        listReminders.setAdapter(remindersAdapter);

        ImageButton btnSend = findViewById(R.id.btnSendReminder);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!txtDate.getText().toString().isBlank() && !textRemind.getText().toString().isBlank()) {
                    databaseHelper.addReminder(txtDate.getText().toString(), textRemind.getText().toString(), traineeID, userID);
                    txtDate.setText("");
                    textRemind.setText("");
                    remindersAdapter.changeCursor(databaseHelper.getRemindersForTrainee(traineeID));
                }
                else {
                    Toast.makeText(TrainerRemindActivity02.this,"Date and message must both be present",Toast.LENGTH_LONG);
                }
            }
        });


        ImageButton back = findViewById(R.id.btnCloseOfReminder);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newIntent = new Intent(TrainerRemindActivity02.this,TrainerTrackActivity.class);
                newIntent.putExtra("userID",userID);
                startActivity(newIntent);
            }
        });
    }
}