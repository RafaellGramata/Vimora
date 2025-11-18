package com.example.vimora;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.SimpleCursorAdapter;

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
    final static int DATABASE_VERSION = 6;
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
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS User");
        db.execSQL("DROP TABLE IF EXISTS WeightSnapshot");
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

    public Cursor listViewTrainers() {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        return sqLiteDatabase.rawQuery("SELECT rowid _id,* FROM User WHERE isTrainer=1 ORDER BY name",new String[]{});
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

    public int getLatestWeight(long trainee) { // assumes input is a valid trainee with an existent weightSnapshot
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor result = sqLiteDatabase.rawQuery("SELECT weight FROM WeightSnapshot WHERE traineeID=? ORDER BY time DESC LIMIT 1",new String[]{String.valueOf(trainee)});
        result.moveToFirst();
        int weight = result.getInt(0);
        result.close();
        return weight;
    }

    public boolean setTraineeHeight(long trainee, int height) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("traineeHeight",height);
        long r = sqLiteDatabase.update("User",values,"userID=?",new String[]{String.valueOf(trainee)});
        sqLiteDatabase.close();
        return r>0;
    }
    public int getTraineeHeight(long trainee) { // assumes input is a valid trainee
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor result = sqLiteDatabase.rawQuery("SELECT traineeHeight FROM User WHERE userID=?",new String[]{String.valueOf(trainee)});
        result.moveToFirst();
        int weight = result.getInt(0);
        result.close();
        return weight;
    }
    public int getTraineeAge(long trainee) { // assumes input is a valid trainee
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor result = sqLiteDatabase.rawQuery("SELECT traineeAge FROM User WHERE userID=?",new String[]{String.valueOf(trainee)});
        result.moveToFirst();
        int age = result.getInt(0);
        result.close();
        return age;
    }
    public boolean setTraineeAge(long trainee, int age) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("traineeAge",age);
        long r = sqLiteDatabase.update("User",values,"userID=?",new String[]{String.valueOf(trainee)});
        sqLiteDatabase.close();
        return r>0;
    }

    public String getName(long userID) { // assumes input is a valid user
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor result = sqLiteDatabase.rawQuery("SELECT name FROM User WHERE userID=?",new String[]{String.valueOf(userID)});
        result.moveToFirst();
        String name = result.getString(0);
        result.close();
        return name;
    }
    public boolean setName(long userID, String name) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name",name);
        long r = sqLiteDatabase.update("User",values,"userID=?",new String[]{String.valueOf(userID)});
        sqLiteDatabase.close();
        return r>0;
    }

    private static String hashPassword(String password) { // we don't want to store the password raw in the database
        String normalized = java.text.Normalizer.normalize(password, Normalizer.Form.NFKD);
        byte[] digest = md.digest(normalized.getBytes(StandardCharsets.UTF_8));
        return android.util.Base64.encodeToString(digest,0);
    }
}
