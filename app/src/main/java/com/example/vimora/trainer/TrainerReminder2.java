package com.example.vimora.trainer;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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

import android.text.format.DateFormat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TrainerReminder2 extends AppCompatActivity {

    private DatabaseHelper db;
    private EditText etReminder;
    private TextView tvTraineeName;
    private ListView lvRecentReminders;
    private long traineeId, trainerId;
    private String traineeName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Test", "TrainerReminder2 started");
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_trainer_remind02);
        findViewById(R.id.main).post(() -> {
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        });
        db = new DatabaseHelper(this);

        traineeId = getIntent().getLongExtra("traineeId", -1);
        traineeName = getIntent().getStringExtra("traineeName");
        trainerId = getIntent().getLongExtra("userID", -1);

        if (traineeId == -1 || trainerId == -1) {
            Toast.makeText(this, "Error, choose another trainee", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvTraineeName = findViewById(R.id.readTraineeName);
        etReminder = findViewById(R.id.txtRemindInput);
        lvRecentReminders = findViewById(R.id.recentReminder);
        ImageView btnSend = findViewById(R.id.sendReminder);
        ImageButton btnClose = findViewById(R.id.btnCloseOfReminder);

        tvTraineeName.setText(traineeName);

        btnSend.setOnClickListener(v -> sendReminder());

        btnClose.setOnClickListener(v -> finish());

        loadRecentReminders();

        lvRecentReminders.setOnItemClickListener((parent, view, position, id) -> {
            Cursor c = (Cursor) parent.getItemAtPosition(position);

            long reminderId = c.getLong(c.getColumnIndexOrThrow("_id"));
            String message = c.getString(c.getColumnIndexOrThrow("message"));
            long timeMillis = c.getLong(c.getColumnIndexOrThrow("time"));

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            String timeStr = sdf.format(new Date(timeMillis));

            Intent intent = new Intent(TrainerReminder2.this, TrainerReminder3.class);
            intent.putExtra("_id", reminderId);
            intent.putExtra("message", message);
            intent.putExtra("time", timeStr);
            intent.putExtra("traineeName", traineeName);
            startActivity(intent);
        });
    }

    private void sendReminder() {
        String message = etReminder.getText().toString().trim();
        if (message.isEmpty()) {
            Toast.makeText(this, "Please type in the content", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean success = db.addReminder(trainerId, traineeId, message);
        if (success) {
            Toast.makeText(this, "Successfully sent reminder to Trainee" + traineeName, Toast.LENGTH_LONG).show();
            etReminder.setText("");
            loadRecentReminders();
        } else {
            Toast.makeText(this, "Failed to send reminder", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadRecentReminders() {
        Cursor cursor = db.getRemindersForTrainee(traineeId);

        String[] from = {"time", "message"};
        int[] to = {android.R.id.text1, android.R.id.text2};

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_list_item_2,cursor,from,to,0);

        adapter.setViewBinder((view, cursor1, columnIndex) -> {
            if (view.getId() == android.R.id.text1) {
                long timeMillis = cursor1.getLong(columnIndex);
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(timeMillis);
                String date = DateFormat.format("MM/dd HH:mm", cal).toString();
                ((TextView) view).setText(date);
                return true;
            }
            return false;
        });

        lvRecentReminders.setAdapter(adapter);

    }



    @Override
    protected void onResume() {
        super.onResume();
        loadRecentReminders();
    }
}
