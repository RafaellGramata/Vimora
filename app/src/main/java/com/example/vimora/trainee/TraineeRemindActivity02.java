package com.example.vimora.trainee;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.vimora.R;

public class TraineeRemindActivity02 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_trainee_remind02);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tvName), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String trainerName = getIntent().getStringExtra("trainerName");
        String message = getIntent().getStringExtra("message");
        String timeStr = getIntent().getStringExtra("time");

        TextView tvContent = findViewById(R.id.txtRemindOutput);
        ImageButton btnClose = findViewById(R.id.btnCloseOfReminder);

        String displayText = "Comes from：" + trainerName + "\n\n" +
                message + "\n\n" +
                "Sent at：" + timeStr;

        tvContent.setText(displayText);

        btnClose.setOnClickListener(v -> finish());
    }

}