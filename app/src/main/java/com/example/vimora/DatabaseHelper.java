package com.example.vimora;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {
    final static String DATABASE_NAME = "Vimora.db";
    final static int DATABASE_VERSION = 1;
    final static String TABLE1 = "TrainerTable";
    final static String T1COL1 = "TrainerID";
    final static String T1COL2 = "TrainerName";
    final static String T1COL3 = "Specialization";
    final static String T1COL4 = "TraineeNumber";
    final static String T1COL5 = "HandleNumber";
    final static String T1COL6 = "AboutMe";

    final static String TABLE2 = "PlanTable";
    final static String T2COL1 = "PlanID";
    final static String T2COL2 = "ExerciseName";
    final static String T2COL3 = "ExerciseContent";

    final static String TABLE3 = "RemindTable";
    final static String T3COL1 = "RemindID";
    final static String T3COL2 = "RemindDate";
    final static String T3COL3 = "RemindContent";


    public DatabaseHelper(@Nullable Context context){
        super(context, DATABASE_NAME,null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase sqLiteDatabase){
        super.onConfigure(sqLiteDatabase);
        sqLiteDatabase.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        String query = "CREATE TABLE " + TABLE1 +
                    "(" + T1COL1 + " INTEGER PRIMARY KEY, " +
                T1COL2 + " TEXT," +
                T1COL3 + " TEXT," +
                T1COL4 + " TEXT," +
                T1COL5 + " TEXT," +
                T1COL6 + " TEXT)";
        db.execSQL(query);

        query = "CREATE TABLE " + TABLE2 +
                "(" + T2COL1 + " INTEGER PRIMARY KEY, " +
                T2COL2 + " TEXT," +
                T2COL3 + " TEXT)";
        db.execSQL(query);

        query = "CREATE TABLE " + TABLE3 +
                "(" + T3COL1 + " INTEGER PRIMARY KEY, " +
                T3COL2 + " TEXT," +
                T3COL3 + " TEXT)";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE1);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE2);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE3);
        onCreate(db);
    }



}
