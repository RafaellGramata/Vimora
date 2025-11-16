package com.example.vimora;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vimora.trainee.TraineePlanActivity01;
import com.example.vimora.trainer.TrainerPlanActivity1;


public class LoginActivity extends AppCompatActivity {
    DatabaseHelper databaseHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        databaseHelper = new DatabaseHelper(this);
        TextView txtemail = findViewById(R.id.edittxtEmail);
        TextView txtpassword = findViewById(R.id.edittxtPassword);
        ImageView buttonLogin = findViewById(R.id.imageButton2);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long userID = databaseHelper.login(txtemail.getText().toString(),txtpassword.getText().toString());
                if (userID>0) {
                    boolean isTrainer = databaseHelper.isTrainer(userID);
                    Intent intent;
                    if (isTrainer) {
                        intent = new Intent(LoginActivity.this, TrainerPlanActivity1.class);
                    }
                    else {
                        intent = new Intent(LoginActivity.this, TraineePlanActivity01.class);
                    }
                    intent.putExtra("userID",userID);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(LoginActivity.this,"Could not log in",Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
