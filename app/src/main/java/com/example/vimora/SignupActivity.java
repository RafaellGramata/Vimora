package com.example.vimora;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Redirect to SignupActivity when image is clicked
        ImageView signupButton = findViewById(R.id.signupSubmit);
        signupButton.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, TrainerPlanActivity.class);
            startActivity(intent);
        });
    }
}
