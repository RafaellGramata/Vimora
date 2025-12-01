package com.example.vimora.trainer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import androidx.appcompat.app.AppCompatActivity;
import com.example.vimora.DatabaseHelper;
import com.example.vimora.R;

import java.util.ArrayList;
import java.util.List;

// this activity shows a list of trainees for the trainer to track
public class TrainerTrackActivity01 extends AppCompatActivity {
    // database helper to get trainee data from the database
    DatabaseHelper databaseHelper;
    // adapter to connect the trainee list to the listview
    SimpleCursorAdapter traineeAdapter;
    // stores the logged-in trainer's id
    long trainerID;
    // stores all trainees assigned to this trainer
    List<TraineeData> allTrainees;
    // stores filtered trainees based on search
    List<TraineeData> filteredTrainees;

    // simple inner class to hold trainee information
    // created this to store trainee id and name together
    private static class TraineeData {
        long id;
        String name;
        TraineeData(long id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // connect this activity to its layout file
        setContentView(R.layout.activity_trainer_track01);

        // initialize database helper
        databaseHelper = new DatabaseHelper(this);
        // get the trainer id that was passed from the previous screen
        Intent intent = getIntent();
        trainerID = intent.getLongExtra("userID", -1);

        // find all the views in the layout
        Button btnProfile = findViewById(R.id.btnProfileOfTrack);
        Button btnPlan = findViewById(R.id.btnPlanOfTrack);
        ImageButton btnReminder = findViewById(R.id.btnReminder);
        EditText editTxtSearch = findViewById(R.id.editTxtTrainerName);
        ListView listView = findViewById(R.id.listView);

        // load all trainees from the database
        loadTrainees();

        // setup the adapter to display trainee names in the listview
        // the adapter connects data to the ui elements
        traineeAdapter = new SimpleCursorAdapter(
                this,
                R.layout.list_trainee_entry, // layout for each list item
                createCursorFromList(allTrainees), // data source
                new String[]{"name"}, // column name from cursor
                new int[]{R.id.traineeListName}, // textview id in list item
                0
        );

        // connect the adapter to the listview so it displays the data
        listView.setAdapter(traineeAdapter);

        // search functionality - filters trainees as the trainer types
        editTxtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            // this runs every time the text changes in the search box
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // filter the trainee list based on what's typed
                filterTrainees(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // when a trainer clicks on a trainee, go to detailed tracking screen
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("Range")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // get the cursor at the clicked position
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                // find the column that has the trainee id
                int idIndex = cursor.getColumnIndex("userID");
                if (idIndex != -1) {
                    // get the trainee's id from the cursor
                    long traineeID = cursor.getLong(idIndex);
                    // create intent to go to the detailed tracking screen
                    Intent newIntent = new Intent(TrainerTrackActivity01.this, TrainerTrackActivity02.class);
                    // pass both trainer and trainee ids to the next screen
                    newIntent.putExtra("userID", trainerID);
                    newIntent.putExtra("traineeID", traineeID);
                    startActivity(newIntent);
                }
            }
        });

        // profile button - go to trainer profile screen
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TrainerTrackActivity01.this, TrainerProfileActivity1.class);
                i.putExtra("userID", trainerID);
                startActivity(i);
            }
        });

        // plan button - go to plan management screen
        btnPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TrainerTrackActivity01.this, TrainerPlanActivity1.class);
                i.putExtra("userID", trainerID);
                startActivity(i);
            }
        });

        // reminder button - go to reminder screen
        btnReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TrainerTrackActivity01.this, TrainerRemindActivity01.class);
                i.putExtra("userID", trainerID);
                startActivity(i);
            }
        });
    }

    // loads all trainees assigned to this trainer from the database
    @SuppressLint("Range")
    private void loadTrainees() {
        // create a new empty list to store trainees
        allTrainees = new ArrayList<>();
        // query database to get all trainees for this trainer
        Cursor cursor = databaseHelper.getTraineesByTrainer(trainerID);

        // check if cursor has data and move to first row
        if (cursor != null && cursor.moveToFirst()) {
            // loop through all rows in the cursor
            do {
                // get trainee id from the current row
                long id = cursor.getLong(cursor.getColumnIndex("userID"));
                // get trainee name from the current row
                String name = cursor.getString(cursor.getColumnIndex("name"));
                // add this trainee to our list
                allTrainees.add(new TraineeData(id, name));
            } while (cursor.moveToNext()); // move to next row
            // close cursor to free up memory
            cursor.close();
        }

        // initially, filtered list contains all trainees
        filteredTrainees = new ArrayList<>(allTrainees);
    }

    // filters the trainee list based on search query
    private void filterTrainees(String query) {
        // if search box is empty, show all trainees
        if (query == null || query.trim().isEmpty()) {
            filteredTrainees = new ArrayList<>(allTrainees);
        } else {
            // search is not empty, so filter the list
            filteredTrainees = new ArrayList<>();
            // convert search query to lowercase for case-insensitive search
            String lowerQuery = query.toLowerCase();
            // loop through all trainees
            for (TraineeData trainee : allTrainees) {
                // check if trainee name contains the search query
                if (trainee.name.toLowerCase().contains(lowerQuery)) {
                    // if it matches, add to filtered list
                    filteredTrainees.add(trainee);
                }
            }
        }

        // update the adapter with the new filtered list
        traineeAdapter.changeCursor(createCursorFromList(filteredTrainees));
    }

    // converts our list of trainees into a cursor for the adapter
    // had to do this because SimpleCursorAdapter needs a cursor, not a list
    private Cursor createCursorFromList(List<TraineeData> trainees) {
        // create a cursor with three columns: _id, userID, and name
        // _id is required by android adapters
        MatrixCursor cursor = new MatrixCursor(new String[]{"_id", "userID", "name"});
        // loop through each trainee in the list
        for (TraineeData trainee : trainees) {
            // add a row to the cursor with trainee data
            // using trainee.id for both _id and userID
            cursor.addRow(new Object[]{trainee.id, trainee.id, trainee.name});
        }
        // return the cursor so the adapter can use it
        return cursor;
    }
}