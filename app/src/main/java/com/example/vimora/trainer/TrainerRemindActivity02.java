package com.example.vimora.trainer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
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

        // Week 9 & 10 SQLite
        Intent intent = getIntent();
        long userID = intent.getLongExtra("userID", -1);
        long traineeID = intent.getLongExtra("traineeId",-1);

        TextView txtTrainee = findViewById(R.id.txtTrainee);
        txtTrainee.setText(databaseHelper.getName(traineeID));

        EditText textRemind = findViewById(R.id.txtRemindInput);
        EditText txtDate = findViewById(R.id.editTextDate2);

        ListView listReminders = findViewById(R.id.listReminders);

        // For reading previous reminders
        // SimpleCursorAdapter
        // https://developer.android.com/reference/androidx/cursoradapter/widget/SimpleCursorAdapter?hl=en
        SimpleCursorAdapter remindersAdapter = new SimpleCursorAdapter(
                TrainerRemindActivity02.this,
                R.layout.list_reminders_entry,
                databaseHelper.getRemindersForTraineeTrainer(traineeID,userID),
                new String[]{"RemindDate","RemindContent"},
                new int[]{R.id.reminderDate,R.id.reminderMessage},0
        );
        listReminders.setAdapter(remindersAdapter);

        listReminders.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("Range")
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(i);

                // getColumnIndex()
                // https://developer.android.com/reference/android/database/Cursor#getColumnIndex(java.lang.String)
                // return -1 if not found
                int idIndex = cursor.getColumnIndex("RemindID");
                if (idIndex != -1) {
                    long remindID = cursor.getLong(idIndex);
                    Intent newIntent = new Intent(TrainerRemindActivity02.this, TrainerRemindActivity03.class);
                    newIntent.putExtra("userID", userID);
                    newIntent.putExtra("remindID", remindID);
                    startActivity(newIntent);
                }
            }
        });

        ImageButton btnSend = findViewById(R.id.btnSendReminder);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!txtDate.getText().toString().isBlank() && !textRemind.getText().toString().isBlank()) {
                    // addReminder()
                    // DatabaseHelper line 521
                    databaseHelper.addReminder(txtDate.getText().toString(), textRemind.getText().toString(), traineeID, userID);
                    txtDate.setText("");
                    textRemind.setText("");
                    // renew the adapter with the new data
                    // https://developer.android.com/reference/android/widget/SimpleCursorAdapter#changeCursor(android.database.Cursor)
                    // getRemindersForTraineeTrainer() line 534
                    remindersAdapter.changeCursor(databaseHelper.getRemindersForTraineeTrainer(traineeID,userID));
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
                Intent newIntent = new Intent(TrainerRemindActivity02.this, TrainerRemindActivity01.class);
                newIntent.putExtra("userID",userID);
                startActivity(newIntent);
            }
        });
    }
}