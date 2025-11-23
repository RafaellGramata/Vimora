package com.example.vimora.trainer;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.vimora.DatabaseHelper;
import com.example.vimora.R;

import java.util.ArrayList;

public class TrainerRemindActivity extends AppCompatActivity {
    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        databaseHelper = new DatabaseHelper(this);
        setContentView(R.layout.activity_trainer_remind01);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Intent intent = getIntent();
        long userID = intent.getLongExtra("userID", -1);

        ListView listViewTrainees = findViewById(R.id.listViewTrainees);

        Cursor trainees = databaseHelper.getTraineesByTrainer(userID);
        ArrayList<String> traineeNames = new ArrayList<>();
        final ArrayList<Long> traineeIds = new ArrayList<>();

        while (trainees.moveToNext()) {
            long id = trainees.getLong(trainees.getColumnIndexOrThrow("userID"));
            String name = trainees.getString(trainees.getColumnIndexOrThrow("name"));
            traineeNames.add(name);
            traineeIds.add(id);
        }
        trainees.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, traineeNames);
        listViewTrainees.setAdapter(adapter);

        listViewTrainees.setOnItemClickListener((parent, view, position, id) -> {
            long selectedTraineeId = traineeIds.get(position);
            Intent newIntent = new Intent(this, TrainerRemindActivity02.class);
            newIntent.putExtra("userID",userID);
            newIntent.putExtra("traineeId", selectedTraineeId);
            startActivity(newIntent);
        });

        ImageButton back = findViewById(R.id.btnCloseOfReminder);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newIntent = new Intent(TrainerRemindActivity.this,TrainerTrackActivity.class);
                newIntent.putExtra("userID",userID);
                startActivity(newIntent);
            }
        });
    }
}