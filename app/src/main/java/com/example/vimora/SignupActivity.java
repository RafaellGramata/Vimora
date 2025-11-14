package com.example.vimora;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.vimora.trainer.TrainerProfileActivity1;

public class SignupActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        ImageView signupButton = findViewById(R.id.signupNext);
        signupButton.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, TrainerProfileActivity1.class);
            startActivity(intent);
        });
    }
}