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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
    final static int DATABASE_VERSION = 8; // why did I make this "3" for the first version?

    final static String TABLE_PLAN = "PlanTable";
    final static String COL_PLAN_ID = "PlanID";
    final static String COL_EXERCISE_NAME = "ExerciseName";
    final static String COL_EXERCISE_CONTENT = "ExerciseContent";

    final static String TABLE_REMIND = "RemindTable";
    final static String COL_REMIND_ID = "RemindID";
    final static String COL_REMIND_DATE = "RemindDate";
    final static String COL_REMIND_CONTENT = "RemindContent";
    final static String TABLE_ASSIGNED_PLAN = "AssignedPlan";

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

        db.execSQL("CREATE TABLE AssignedPlan (" +
                "assignmentID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "planID INTEGER NOT NULL, " +
                "traineeID INTEGER NOT NULL, " +
                "assignedDate TEXT NOT NULL, " +
                "FOREIGN KEY(planID) REFERENCES " + TABLE_PLAN + "(" + COL_PLAN_ID + ") ON DELETE CASCADE, " +
                "FOREIGN KEY(traineeID) REFERENCES User(userID) ON DELETE CASCADE)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS User");
        if (oldVersion < 8) {
            db.execSQL("CREATE TABLE IF NOT EXISTS AssignedPlan (" +
                    "assignmentID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "planID INTEGER NOT NULL, " +
                    "traineeID INTEGER NOT NULL, " +
                    "assignedDate TEXT NOT NULL, " +
                    "FOREIGN KEY(planID) REFERENCES PlanTable(PlanID) ON DELETE CASCADE, " +
                    "FOREIGN KEY(traineeID) REFERENCES User(userID) ON DELETE CASCADE)");
        }
        else {
            db.execSQL("ALTER TABLE User ADD COLUMN trainerSpecialization TEXT");
            db.execSQL("ALTER TABLE User ADD COLUMN trainerHandleNum INTEGER DEFAULT 0");
            db.execSQL("DROP TABLE IF EXISTS WeightSnapshot");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAN);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_REMIND);
            db.execSQL("DROP TABLE IF EXISTS AssignedPlan");
            onCreate(db);
        }
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

    public boolean updateTrainerProfile(long trainerId,
                                        String name,
                                        String specialization,
                                        int handleNum,
                                        String about) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("trainerName", name);
        cv.put("trainerSpecialization", specialization);
        cv.put("trainerHandleNumber", handleNum);
        cv.put("trainerAboutMe", about);

        int rows = db.update("User", cv, "userID = ?", new String[]{String.valueOf(trainerId)});
        db.close();
        return rows > 0;
    }

    public Cursor getTrainerProfile(long trainerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT name, trainerSpecialization, trainerHandleNum, trainerAbout " +
                        "FROM User WHERE userID = ?", new String[]{String.valueOf(trainerId)});
    }

    public int countTrainees(long trainerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM User WHERE trainerID = ?",
                new String[]{String.valueOf(trainerId)});
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();
        db.close();
        return count;
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

    public Cursor getTraineesByTrainer(long trainerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT userID, name FROM User WHERE trainerID = ? AND isTrainer = 0",
                new String[]{String.valueOf(trainerId)}
        );
    }

    public Cursor getAllTrainees() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT userID, name FROM User WHERE isTrainer = 0", null);
    }

    public int getLatestWeight(long traineeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT weight FROM WeightSnapshot WHERE traineeID = ? ORDER BY time DESC LIMIT 1",
                new String[]{String.valueOf(traineeId)}
        );
        int weight = 0;
        if (c.moveToFirst()) {
            weight = c.getInt(0);
        }
        c.close();
        return weight;
    }

    public Cursor getTraineeDetails(long traineeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT u.name, u.traineeAge, u.traineeHeight, " +
                        "(SELECT weight FROM WeightSnapshot WHERE traineeID = u.userID ORDER BY time DESC LIMIT 1) as latestWeight " +
                        "FROM User u WHERE u.userID = ?",
                new String[]{String.valueOf(traineeId)}
        );
    }

    public boolean assignPlanToTrainee(long planId, long traineeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("planID", planId);
        cv.put("traineeID", traineeId);
        cv.put("assignedDate", getCurrentDateTime());

        long result = db.insert("AssignedPlan", null, cv);
        db.close();
        return result != -1;
    }

    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }
}
