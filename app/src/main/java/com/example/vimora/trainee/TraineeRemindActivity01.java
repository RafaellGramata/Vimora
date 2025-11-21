package com.example.vimora.trainee;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateFormat;
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

import java.util.Calendar;

public class TraineeRemindActivity01 extends AppCompatActivity {

    private DatabaseHelper db;
    private ListView listReminders;
    private long currentTraineeId = -1;

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
        db = new DatabaseHelper(this);
        listReminders = findViewById(R.id.listReminders);
        ImageButton btnBack = findViewById(R.id.btnCloseOfReminder);

        currentTraineeId = getIntent().getLongExtra("traineeId", -1);

        if (currentTraineeId == -1) {
            Toast.makeText(this, "Please log in", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(this, TraineeProfileActivity01.class);
            intent.putExtra("traineeId", currentTraineeId);
            startActivity(intent);
            finish();
        });

        loadReminders();
    }

    private void loadReminders() {
        Cursor cursor1 = db.getRemindersForTrainee(currentTraineeId);

        if (cursor1 == null || cursor1.getCount() == 0) {
            Toast.makeText(this, "No new reminders", Toast.LENGTH_SHORT).show();
            if (cursor1 != null) cursor1.close();
            return;
        }

        String[] from = {"trainerName", "time", "message"};
        int[] to = {android.R.id.text1, android.R.id.text1, android.R.id.text2};

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_list_item_2,cursor1,from,to,0);


        adapter.setViewBinder((view, cursor, columnIndex) -> {
            if (view.getId() == android.R.id.text1) {
                String trainerName = cursor.getString(cursor.getColumnIndexOrThrow("trainerName"));
                long timeMillis = cursor.getLong(cursor.getColumnIndexOrThrow("time"));

                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(timeMillis);
                String timeStr = DateFormat.format("MM/dd HH:mm", cal).toString();

                ((TextView) view).setText(trainerName + " · " + timeStr);
                return true;
            }
            return false;
        });

        listReminders.setAdapter(adapter);

        listReminders.setOnItemClickListener((parent, view, position, id) -> {
            Cursor c = (Cursor) parent.getItemAtPosition(position);

            long reminderId = c.getLong(c.getColumnIndexOrThrow("RemindID"));
            String message = c.getString(c.getColumnIndexOrThrow("message"));
            String trainerName = c.getString(c.getColumnIndexOrThrow("trainerName"));
            long timeMillis = c.getLong(c.getColumnIndexOrThrow("time"));

            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(timeMillis);
            String timeStr = DateFormat.format("yyyy-MM-dd HH:mm", cal).toString();

            Intent intent = new Intent(TraineeRemindActivity01.this, TraineeRemindActivity02.class);
            intent.putExtra("reminderId", reminderId);
            intent.putExtra("message", message);
            intent.putExtra("trainerName", trainerName);
            intent.putExtra("time", timeStr);
            startActivity(intent);
        });
    }

}
