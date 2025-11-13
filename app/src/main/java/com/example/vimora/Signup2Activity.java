package com.example.vimora;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Signup2Activity extends AppCompatActivity {
    DatabaseHelper databaseHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup2);
        databaseHelper = new DatabaseHelper(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView txtage = findViewById(R.id.edittxtAge);
        TextView txtheight = findViewById(R.id.edittxtHeight);
        TextView txtweight = findViewById(R.id.edittxtWeight);
        ImageView signUp = findViewById(R.id.signupNext);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getIntent(); // from SignupActivity.java
                String name = intent.getStringExtra("name");
                String phone = intent.getStringExtra("phone");
                String email = intent.getStringExtra("email");
                String password = intent.getStringExtra("password");
                int age = Integer.parseInt(txtage.getText().toString());
                int height = Integer.parseInt(txtheight.getText().toString());
                int weight = Integer.parseInt(txtweight.getText().toString());
                System.out.println(name);
                boolean signedUp = databaseHelper.signUp(name,phone,email,password,false,height,weight,age); // dummy values for age and [hw]eight; will never be used for a trainer account
                if (signedUp) {
                    Toast.makeText(Signup2Activity.this,"Successfully signed up",Toast.LENGTH_LONG).show();
                    Intent login = new Intent(Signup2Activity.this, WelcomeActivity.class);
                    startActivity(login);
                }
                else Toast.makeText(Signup2Activity.this,"Could not sign up",Toast.LENGTH_LONG).show();

            }
        });
    }
}