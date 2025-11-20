package com.example.vimora.trainer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.vimora.DatabaseHelper;
import com.example.vimora.R;

import java.util.ArrayList;

public class TrainerProfileActivity2 extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private long currentTrainerId;
    private ListView listViewTrainees;
    private TextView tvCurrentTrainees, tvMaxTrainees;

    private static final String PREF_NAME = "TrainerProfilePref";
    private static final String KEY_TRAINER_ID = "trainerId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        dbHelper = new DatabaseHelper(this);
        setContentView(R.layout.activity_trainer_profile2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tvName), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        currentTrainerId = intent.getLongExtra("userID", -1);
        if (currentTrainerId == -1) {
            SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            currentTrainerId = pref.getLong(KEY_TRAINER_ID, -1);
        }

        if (currentTrainerId == -1) {
            Toast.makeText(this, "User ID missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        initViews();
        setupButtons();
        loadTraineeData();
    }

    private void initViews() {
        listViewTrainees = findViewById(R.id.listViewTrainees);
        tvCurrentTrainees = findViewById(R.id.tvCurrentTrainees);
        tvMaxTrainees = findViewById(R.id.tvMaxTrainees);
    }


    private void setupButtons() {
        findViewById(R.id.btnReminder).setOnClickListener(v ->
                startActivity(new Intent(this, TrainerProfileActivity1.class)));

        findViewById(R.id.toTrainerProfile1).setOnClickListener(v -> {
            Intent intent = new Intent(this, TrainerProfileActivity1.class);
            intent.putExtra("userID", currentTrainerId); // 帶上 ID
            startActivity(intent);
        });

        findViewById(R.id.btnTrackOfProfile1).setOnClickListener(v -> {
            Intent intent = new Intent(this, TrainerTrackActivity.class);
            intent.putExtra("userID", currentTrainerId); // 帶上 ID
            startActivity(intent);
        });

        findViewById(R.id.btnPlanOfProfile1).setOnClickListener(v -> {
            Intent intent = new Intent(this, TrainerPlanActivity1.class);
            intent.putExtra("userID", currentTrainerId); // 帶上 ID
            startActivity(intent);
        });
    }

    private void loadTraineeData() {

        Cursor profile = dbHelper.getTrainerProfile(currentTrainerId);
        int maxHandle = 0;
        if (profile.moveToFirst()) {
            maxHandle = profile.getInt(profile.getColumnIndexOrThrow("trainerHandleNum"));
        }
        profile.close();
        tvMaxTrainees.setText(String.valueOf(maxHandle));

        Cursor trainees = dbHelper.getTraineesByTrainer(currentTrainerId);
        ArrayList<String> traineeNames = new ArrayList<>();
        final ArrayList<Long> traineeIds = new ArrayList<>();

        while (trainees.moveToNext()) {
            long id = trainees.getLong(trainees.getColumnIndexOrThrow("userID"));
            String name = trainees.getString(trainees.getColumnIndexOrThrow("name"));
            traineeNames.add(name);
            traineeIds.add(id);
        }
        trainees.close();

        tvCurrentTrainees.setText(String.valueOf(traineeNames.size()));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, traineeNames);
        listViewTrainees.setAdapter(adapter);

        listViewTrainees.setOnItemClickListener((parent, view, position, id) -> {
            long selectedTraineeId = traineeIds.get(position);
            Intent intent = new Intent(this, TrainerProfileActivity3.class);
            intent.putExtra("traineeId", selectedTraineeId);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTraineeData();
    }
}