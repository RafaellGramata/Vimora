package com.example.vimora.trainer;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.vimora.R;

public class TrainerReminder3 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_trainer_remind03);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String traineeName = getIntent().getStringExtra("traineeName");
        String message = getIntent().getStringExtra("message");
        String timeStr = getIntent().getStringExtra("time");

        TextView tvTitle = findViewById(R.id.readReminder);
        TextView tvContent = findViewById(R.id.readReminderContent);
        ImageButton btnClose = findViewById(R.id.btnCloseOfReminder);

        tvTitle.setText(traineeName);
        tvContent.setText(message + "\n\nSending time：" + timeStr);


        btnClose.setOnClickListener(v -> {
            finish();
            finish();
        });
    }
}