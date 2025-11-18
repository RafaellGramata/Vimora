package com.example.vimora.trainee;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.vimora.DatabaseHelper;
import com.example.vimora.R;

public class TraineeListTrainersActivity extends AppCompatActivity {
    DatabaseHelper databaseHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_trainee_list_trainers);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        databaseHelper = new DatabaseHelper(this);
        Intent intent = getIntent();
        long userID = intent.getLongExtra("userID",-1);
        ListView listTrainers = findViewById(R.id.listTrainers);
        SimpleCursorAdapter trainers = new SimpleCursorAdapter(
                TraineeListTrainersActivity.this,
                R.layout.list_trainers_entry,
                databaseHelper.listViewTrainers(),
                new String[]{"name"},
                new int[]{R.id.trainerListName},
                0);
        listTrainers.setAdapter(trainers);

        listTrainers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("Range")
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(i);
                long trainerID = cursor.getLong(cursor.getColumnIndex("userID"));
                Intent newIntent = new Intent(TraineeListTrainersActivity.this, TraineeProfileActivity01.class);
                newIntent.putExtra("userID",userID);
                newIntent.putExtra("trainerID",trainerID);
                startActivity(newIntent);
            }
        });
    }
}