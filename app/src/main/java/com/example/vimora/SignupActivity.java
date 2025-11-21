package com.example.vimora;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {
    DatabaseHelper databaseHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        databaseHelper = new DatabaseHelper(this);

        ImageView signupButton = findViewById(R.id.signupNext);
        TextView name = findViewById(R.id.edittxtName);
        TextView phone = findViewById(R.id.edittxtPhone);
        TextView email = findViewById(R.id.edittxtEmailSignup);
        TextView password = findViewById(R.id.edittxtPassword2);
        Spinner trainerTrainee = findViewById(R.id.spnType);
        signupButton.setOnClickListener(v -> {
            boolean isTrainer = trainerTrainee.getSelectedItemPosition()==1;
            if (!isTrainer) { // only trainee goes to 2nd signup screen
                Intent signup2 = new Intent(SignupActivity.this,Signup2Activity.class);
                signup2.putExtra("name",name.getText().toString());
                signup2.putExtra("phone",phone.getText().toString());
                signup2.putExtra("email",email.getText().toString());
                signup2.putExtra("password",password.getText().toString());
                startActivity(signup2);
            }
            else { // sign up trainer
                boolean signedUp = databaseHelper.signUp(name.getText().toString(),phone.getText().toString(),email.getText().toString(),password.getText().toString(),true,0,0,0); // dummy values for age and [hw]eight; will never be used for a trainer account
                if (signedUp) {
                    Toast.makeText(SignupActivity.this,"Successfully signed up",Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(SignupActivity.this, WelcomeActivity.class);
                    startActivity(intent);
                }
                else Toast.makeText(SignupActivity.this,"Could not sign up",Toast.LENGTH_LONG).show();
            }
        });
    }
}