package com.example.vimora.trainee;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.vimora.DatabaseHelper;
import com.example.vimora.R;

public class TraineeRemindActivity02 extends AppCompatActivity {
    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_trainee_remind);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tvName), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        databaseHelper = new DatabaseHelper(this);
        Intent intent = getIntent();
        long userID = intent.getLongExtra("userID",-1);
        long remindID = intent.getLongExtra("remindID",-1);

        TextView header = findViewById(R.id.txtCoach);
        TextView message = findViewById(R.id.txtRemindOutput);
        header.setText(databaseHelper.getTrainerNameFromReminder(remindID)+" \u2014 "+databaseHelper.getDateFromReminder(remindID));
        message.setText(databaseHelper.getMessageFromReminder(remindID));

        ImageButton close = findViewById(R.id.btnCloseOfReminder);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newIntent = new Intent(TraineeRemindActivity02.this,TraineeRemindActivity.class);
                newIntent.putExtra("userID",userID);
                startActivity(newIntent);
            }
        });
    }
}