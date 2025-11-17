package com.example.vimora.trainee;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.vimora.DatabaseHelper;
import com.example.vimora.R;

public class TraineeProfileActivity extends AppCompatActivity {
    DatabaseHelper databaseHelper;
    int height;
    int weight;
    TextView txtBMI;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_trainee_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        databaseHelper = new DatabaseHelper(this);
        Intent intent = getIntent();
        long userID = intent.getLongExtra("userID",-1); // must exist!    @Override


        TextView name = findViewById(R.id.editTxtTraineeName);
        TextView txtHeight = findViewById(R.id.editTxtTraineeHeight);
        TextView txtWeight = findViewById(R.id.editTxtTraineeWeight);
        txtBMI = findViewById(R.id.txtOutputBMI);

        height = databaseHelper.getTraineeHeight(userID);
        weight = databaseHelper.getLatestWeight(userID);


        name.setText(databaseHelper.getName(userID));
        txtHeight.setText(Integer.toString(height));
        txtWeight.setText(Integer.toString(weight));
        updateBMI();

        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                databaseHelper.setName(userID,editable.toString());
            }
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        });

        txtHeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                height = Integer.parseInt(editable.toString());
                databaseHelper.setTraineeHeight(userID,height);
                updateBMI();
            }
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        });

        txtWeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                weight = Integer.parseInt(editable.toString());
                databaseHelper.addWeightSnapshot(userID,weight);
                updateBMI();
            }
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        });
    }
    private void updateBMI() {
        float BMI = ((float)weight*10000)/((float)height*(float)height); // formula for kg and cm. change if we change units.
        txtBMI.setText(Float.toString(BMI));
    }
}