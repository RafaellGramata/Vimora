package com.example.vimora.trainer;

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

public class TrainerRemindActivity03 extends AppCompatActivity {

    DatabaseHelper databaseHelper;

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

        databaseHelper = new DatabaseHelper(this);
        Intent intent = getIntent();
        long userID = intent.getLongExtra("userID",-1);
        long remindID = intent.getLongExtra("remindID",-1);


        TextView txtDate = findViewById(R.id.readChosenDate);
        txtDate.setText(databaseHelper.getDateFromReminder(intent.getLongExtra("remindID",-1)));

        TextView txtMessage = findViewById(R.id.readChoseReminder);
        txtMessage.setText(databaseHelper.getMessageFromReminder(intent.getLongExtra("remindID",-1)));

        ImageButton back = findViewById(R.id.btnBackOfReminder);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newIntent = new Intent(TrainerRemindActivity03.this,TrainerRemindActivity02.class);
                newIntent.putExtra("userID",userID);
                newIntent.putExtra("remindID",remindID);
                startActivity(newIntent);
            }
        });


    }


}