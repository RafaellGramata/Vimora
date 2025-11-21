package com.example.vimora.trainer;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.vimora.DatabaseHelper;
import com.example.vimora.R;
import com.example.vimora.WelcomeActivity;

public class TrainerReminder1 extends AppCompatActivity {

    private DatabaseHelper db;
    private ListView listView;
    private long currentTrainerId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Test", "Button clicked");
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_trainer_remind01);
        findViewById(R.id.main).post(() -> {
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        });

        db = new DatabaseHelper(this);
        listView = findViewById(R.id.traineeListOfRemind);
        ImageButton btnClose = findViewById(R.id.btnCloseOfReminder);
        currentTrainerId = getIntent().getLongExtra("userID", -1);

        if (currentTrainerId == -1) {
            Toast.makeText(this, "Log in again", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
            return;
        }

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TrainerReminder1.this, TrainerProfileActivity1.class);
                i.putExtra("userID", currentTrainerId);
                startActivity(i);
                finish();
            }
        });

        loadTrainees();
    }

    private void loadTrainees() {
        Cursor cursor = db.getTraineesByTrainer(currentTrainerId);

        if (cursor.getCount() == 0) {
            cursor.close();
            Toast.makeText(this, "Currently with no trainees", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] from = {"name"};
        int[] to = {android.R.id.text1};

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_list_item_1,cursor,from,to,0);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Cursor clickedCursor = (Cursor) parent.getItemAtPosition(position);
            long traineeId = clickedCursor.getLong(clickedCursor.getColumnIndexOrThrow("_id"));
            String traineeName = clickedCursor.getString(clickedCursor.getColumnIndexOrThrow("name"));

            Intent intent = new Intent(TrainerReminder1.this, TrainerReminder2.class);
            intent.putExtra("traineeId", traineeId);
            intent.putExtra("traineeName", traineeName);
            intent.putExtra("userID", currentTrainerId);
            startActivity(intent);
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTrainees();
    }
}
