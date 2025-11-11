package com.example.vimora;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.vimora.trainer.TrainerProfileActivity;

public class SignupActivity extends AppCompatActivity {
    DatabaseHelper databaseHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        databaseHelper = new DatabaseHelper(this);

        TextView name = findViewById(R.id.signupName);
        TextView phone = findViewById(R.id.signupPhone);
        TextView email = findViewById(R.id.signupEmail);
        TextView password = findViewById(R.id.signupPassword);
        Spinner trainerTrainee = findViewById(R.id.signupTrainerTrainee);
        ImageView signupButton = findViewById(R.id.signupSubmit);
        signupButton.setOnClickListener(v -> {
            boolean signedUp = databaseHelper.signUp(name.getText().toString(),phone.getText().toString(),email.getText().toString(),password.getText().toString(),trainerTrainee.getSelectedItemPosition()==1,0.0f,null);
            // TODO for now, height and birthday remain unset. also doing nothing with weight.
            if (signedUp) {
                Toast.makeText(SignupActivity.this,"Successfully signed up",Toast.LENGTH_LONG); // TODO doesn't actually appear
                Intent intent = new Intent(SignupActivity.this, WelcomeActivity.class);
                startActivity(intent);
            }
            else Toast.makeText(SignupActivity.this,"Could not sign up",Toast.LENGTH_LONG);
        });
    }
}