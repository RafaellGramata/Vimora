package com.example.vimora.trainee;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.vimora.DatabaseHelper;
import com.example.vimora.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TraineePlanActivity01 extends AppCompatActivity {
    DatabaseHelper databaseHelper;
    Calendar date;
    TextView txtDate;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_trainee_plan01);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tvName), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseHelper = new DatabaseHelper(this);
        Intent intent = getIntent();
        long userID = intent.getLongExtra("userID",-1);
        date = Calendar.getInstance();

        ImageButton btnReminder = findViewById(R.id.btnReminder);
        Button btnTrack = findViewById(R.id.btnTrackOfPlan);
        Button btnProfile = findViewById(R.id.btnProfileOfPlan);
        txtDate = findViewById(R.id.txtDate);
        updateDate();
        Button previousDay = findViewById(R.id.btnPlanYesterday);
        Button nextDay = findViewById(R.id.btnPlanNextday);

        previousDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                date.add(Calendar.DAY_OF_MONTH,-1);
                updateDate();
            }
        });

        nextDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                date.add(Calendar.DAY_OF_MONTH,1);
                updateDate();
            }
        });

        btnTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newIntent = new Intent(TraineePlanActivity01.this, TraineeTrackActivity01.class);
                newIntent.putExtra("userID",userID);
                startActivity(newIntent);
            }
        });

        btnReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newIntent = new Intent(TraineePlanActivity01.this, TraineeRemindActivity.class);
                newIntent.putExtra("userID",userID);
                startActivity(newIntent);
            }
        });

        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newIntent = new Intent(TraineePlanActivity01.this, TraineeProfileActivity.class);
                newIntent.putExtra("userID",userID);
                startActivity(newIntent);
            }
        });
    }
    private void updateDate() {
        txtDate.setText(sdf.format(date.getTime()));
    }
}