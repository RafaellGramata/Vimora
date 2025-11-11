package com.example.vimora;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.Calendar;

public class DatabaseHelper extends SQLiteOpenHelper {
    MessageDigest md;
    final static String DATABASE_NAME = "Vimora";
    final static int DATABASE_VERSION = 3; // why did I make this "3" for the first version?
    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e ) {
            // impossible
        }
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
                "trainieeHeight REAL, " +
                "trainieeBirthday INTEGER, " +
                "trainerAbout TEXT, " +
                "FOREIGN KEY (trainerID) REFERENCES User(userID))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS User");
        onCreate(db);
    }

    public boolean signUp(String name, String phone, String email, String password, boolean isTrainer, float height, Calendar birthday) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name",name);
        values.put("phone",phone);
        values.put("email",email);
        values.put("passwordHash",hashPassword(password));
        values.put("isTrainer",isTrainer);
        if (!isTrainer) {
            values.put("traineeHeight",height);
            values.put("traineeBirthday",birthday.get(Calendar.YEAR)*1000+birthday.get(Calendar.DAY_OF_YEAR));
        }
        long r = sqLiteDatabase.insert("User",null,values);
        return r>0;
    }

    private String hashPassword(String password) { // we don't want to store the password raw in the database
        String normalized = java.text.Normalizer.normalize(password, Normalizer.Form.NFKD);
        byte[] digest = md.digest(normalized.getBytes(StandardCharsets.UTF_8));
        return android.util.Base64.encodeToString(digest,0);
    }
}
