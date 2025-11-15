package com.example.vimora;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.time.Instant;
import java.util.Calendar;

public class DatabaseHelper extends SQLiteOpenHelper {
    static MessageDigest md;

    static {
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            // impossible
        }
    }


    final static String DATABASE_NAME = "Vimora";
    final static int DATABASE_VERSION = 7; // why did I make this "3" for the first version?

    final static String TABLE_PLAN = "PlanTable";
    final static String COL_PLAN_ID = "PlanID";
    final static String COL_EXERCISE_NAME = "ExerciseName";
    final static String COL_EXERCISE_CONTENT = "ExerciseContent";

    final static String TABLE_REMIND = "RemindTable";
    final static String COL_REMIND_ID = "RemindID";
    final static String COL_REMIND_DATE = "RemindDate";
    final static String COL_REMIND_CONTENT = "RemindContent";
    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onConfigure(SQLiteDatabase sqLiteDatabase) {
        super.onConfigure(sqLiteDatabase);
        sqLiteDatabase.setForeignKeyConstraintsEnabled(true);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE User (" +
                "userID INTEGER PRIMARY KEY, " +
                "name TEXT NOT NULL, " +
                "email TEXT UNIQUE NOT NULL, " +
                "phone TEXT, " +
                "passwordHash TEXT NOT NULL, " +
                "isTrainer INTEGER NOT NULL, " +
                "trainerID INTEGER, " +
                "traineeHeight INTEGER, " +
                "traineeAge INTEGER, " +
                "trainerAbout TEXT, " +
                "FOREIGN KEY (trainerID) REFERENCES User(userID))");
        db.execSQL("CREATE TABLE WeightSnapshot (" +
                "snapshotID INTEGER PRIMARY KEY," +
                "traineeID INTEGER," +
                "time INTEGER," +
                "weight INTEGER," +
                "FOREIGN KEY (traineeID) REFERENCES User(userID) ON DELETE CASCADE)");

        db.execSQL("CREATE TABLE " + TABLE_PLAN + " (" +
                COL_PLAN_ID + " INTEGER PRIMARY KEY, " +
                COL_EXERCISE_NAME + " TEXT NOT NULL," +
                COL_EXERCISE_CONTENT + " TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_REMIND + " (" +
                COL_REMIND_ID + " INTEGER PRIMARY KEY, " +
                COL_REMIND_DATE + " TEXT NOT NULL," +
                COL_REMIND_CONTENT + " TEXT NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS User");
        db.execSQL("DROP TABLE IF EXISTS WeightSnapshot");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAN);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REMIND);
        onCreate(db);
    }

    public boolean signUp(String name, String phone, String email, String password, boolean isTrainer, int height, int weight, int age) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name",name);
        values.put("phone",phone);
        values.put("email",email);
        values.put("passwordHash",hashPassword(password));
        values.put("isTrainer",isTrainer);
        if (!isTrainer) {
            values.put("traineeHeight",height);
            values.put("traineeAge",age);
        }
        long r = sqLiteDatabase.insert("User",null,values);
        sqLiteDatabase.close();
        if (r>0) {
            if (!isTrainer) return addWeightSnapshot(r,weight);
            else return true;
        }
        else return false;
    }

    @SuppressLint("Range")
    public long login(String email, String password) {
        // returns -1 if invalid. returns userID if valid.
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor result = sqLiteDatabase.rawQuery("SELECT * FROM User WHERE email=?",new String[]{email});
        if (result.getCount()==0) { // user with given email address does not exist
            result.close();
            return -1;
        }
        result.moveToFirst();
        String hashedPassword = result.getString(result.getColumnIndex("passwordHash"));
        if (!hashPassword(password).equals(hashedPassword)) { // passwords must be equal
            result.close();
            return -1;
        }
        else {
            long id = result.getLong(result.getColumnIndex("userID"));
            result.close();
            return id;
        }
    }

    public boolean isTrainer(long userID) { // check whether user is trainer. assumes valid user id.
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor result = sqLiteDatabase.rawQuery("SELECT * FROM User WHERE userID=?",new String[]{String.valueOf(userID)});
        result.moveToFirst();
        @SuppressLint("Range") boolean trainer = result.getInt(result.getColumnIndex("isTrainer"))==1;
        result.close();
        return trainer;
    }

    public boolean addWeightSnapshot(long trainee, int weight) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("traineeID",trainee);
        values.put("weight",weight);
        values.put("time", System.currentTimeMillis());
        long r = sqLiteDatabase.insert("WeightSnapshot",null,values);
        sqLiteDatabase.close();
        return r>0;
    }

    private static String hashPassword(String password) { // we don't want to store the password raw in the database
        String normalized = java.text.Normalizer.normalize(password, Normalizer.Form.NFKD);
        byte[] digest = md.digest(normalized.getBytes(StandardCharsets.UTF_8));
        return android.util.Base64.encodeToString(digest,0);
    }

    public boolean addPlan(String exerciseName, String exerciseContent) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_EXERCISE_NAME, exerciseName);
        values.put(COL_EXERCISE_CONTENT, exerciseContent);
        long r = sqLiteDatabase.insert(TABLE_PLAN, null, values);
        sqLiteDatabase.close();
        return r > 0;
    }

    public Cursor getAllPlans() {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        return sqLiteDatabase.rawQuery("SELECT * FROM " + TABLE_PLAN, null);
    }

    public boolean updatePlan(long planId, String exerciseName, String exerciseContent) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_EXERCISE_NAME, exerciseName);
        values.put(COL_EXERCISE_CONTENT, exerciseContent);
        int r = sqLiteDatabase.update(TABLE_PLAN, values, COL_PLAN_ID + " = ?",
                new String[]{String.valueOf(planId)});
        sqLiteDatabase.close();
        return r > 0;
    }

    public boolean deletePlan(long planId) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        int r = sqLiteDatabase.delete(TABLE_PLAN, COL_PLAN_ID + " = ?",
                new String[]{String.valueOf(planId)});
        sqLiteDatabase.close();
        return r > 0;
    }

    public boolean addReminder(String remindDate, String remindContent) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_REMIND_DATE, remindDate);
        values.put(COL_REMIND_CONTENT, remindContent);
        long r = sqLiteDatabase.insert(TABLE_REMIND, null, values);
        sqLiteDatabase.close();
        return r > 0;
    }

    public Cursor getAllReminders() {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        return sqLiteDatabase.rawQuery("SELECT * FROM " + TABLE_REMIND, null);
    }

    public boolean updateReminder(long remindId, String remindDate, String remindContent) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_REMIND_DATE, remindDate);
        values.put(COL_REMIND_CONTENT, remindContent);
        int r = sqLiteDatabase.update(TABLE_REMIND, values, COL_REMIND_ID + " = ?",
                new String[]{String.valueOf(remindId)});
        sqLiteDatabase.close();
        return r > 0;
    }

    public boolean deleteReminder(long remindId) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        int r = sqLiteDatabase.delete(TABLE_REMIND, COL_REMIND_ID + " = ?",
                new String[]{String.valueOf(remindId)});
        sqLiteDatabase.close();
        return r > 0;
    }
}
