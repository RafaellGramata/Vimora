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

    private static MessageDigest md;
    static {
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            // impossible
        }
    }

    private static final String DATABASE_NAME = "Vimora";
    private static final int DATABASE_VERSION = 11;   // incremented for nutrition tracking

    // Plan Table
    private static final String TABLE_PLAN = "PlanTable";
    private static final String COL_PLAN_ID = "PlanID";
    private static final String COL_EXERCISE_NAME = "ExerciseName";
    private static final String COL_EXERCISE_CONTENT = "ExerciseContent";

    // Reminder Table
    private static final String TABLE_REMIND = "RemindTable";
    private static final String COL_REMIND_ID = "RemindID";
    private static final String COL_REMIND_DATE = "RemindDate";
    private static final String COL_REMIND_CONTENT = "RemindContent";

    // AssignedPlan Table
    private static final String TABLE_ASSIGNED_PLAN = "AssignedPlan";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // User Table
        db.execSQL("CREATE TABLE User (" +
                "userID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "email TEXT UNIQUE NOT NULL, " +
                "phone TEXT, " +
                "passwordHash TEXT NOT NULL, " +
                "isTrainer INTEGER NOT NULL DEFAULT 0, " +
                "trainerID INTEGER, " +
                "traineeHeight INTEGER, " +
                "traineeAge INTEGER, " +
                "trainerAbout TEXT, " +
                "trainerSpecialization TEXT, " +
                "trainerHandleNum INTEGER DEFAULT 0, " +
                "FOREIGN KEY (trainerID) REFERENCES User(userID))");

        db.execSQL("CREATE TABLE WeightSnapshot (" +
                "snapshotID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "traineeID INTEGER," +
                "time INTEGER," +
                "weight INTEGER," +
                "FOREIGN KEY (traineeID) REFERENCES User(userID) ON DELETE CASCADE)");

        db.execSQL("CREATE TABLE " + TABLE_PLAN + " (" +
                COL_PLAN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_EXERCISE_NAME + " TEXT NOT NULL," +
                COL_EXERCISE_CONTENT + " TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_REMIND + " (" +
                COL_REMIND_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_REMIND_DATE + " TEXT NOT NULL," +
                COL_REMIND_CONTENT + " TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_ASSIGNED_PLAN + " (" +
                "assignmentID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "planID INTEGER NOT NULL, " +
                "traineeID INTEGER NOT NULL, " +
                "assignedDate TEXT NOT NULL, " +
                "FOREIGN KEY(planID) REFERENCES " + TABLE_PLAN + "(" + COL_PLAN_ID + ") ON DELETE CASCADE, " +
                "FOREIGN KEY(traineeID) REFERENCES User(userID) ON DELETE CASCADE)");

        // Nutrition tracking table
        db.execSQL("CREATE TABLE NutritionLog (" +
                "entryID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "traineeID INTEGER NOT NULL, " +
                "date TEXT NOT NULL, " +
                "mealType TEXT NOT NULL, " +
                "calories INTEGER DEFAULT 0, " +
                "protein INTEGER DEFAULT 0, " +
                "totalFat INTEGER DEFAULT 0, " +
                "FOREIGN KEY(traineeID) REFERENCES User(userID) ON DELETE CASCADE, " +
                "UNIQUE(traineeID, date, mealType))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE User ADD COLUMN trainerSpecialization TEXT");
            db.execSQL("ALTER TABLE User ADD COLUMN trainerHandleNum INTEGER DEFAULT 0");
        }
        if (oldVersion < 5) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_PLAN + " (" +
                    COL_PLAN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_EXERCISE_NAME + " TEXT NOT NULL," +
                    COL_EXERCISE_CONTENT + " TEXT NOT NULL)");
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_REMIND + " (" +
                    COL_REMIND_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_REMIND_DATE + " TEXT NOT NULL," +
                    COL_REMIND_CONTENT + " TEXT NOT NULL)");
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_ASSIGNED_PLAN + " (" +
                    "assignmentID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "planID INTEGER NOT NULL, " +
                    "traineeID INTEGER NOT NULL, " +
                    "assignedDate TEXT NOT NULL, " +
                    "FOREIGN KEY(planID) REFERENCES " + TABLE_PLAN + "(" + COL_PLAN_ID + ") ON DELETE CASCADE, " +
                    "FOREIGN KEY(traineeID) REFERENCES User(userID) ON DELETE CASCADE)");
        }
        if (oldVersion < 7) {
            db.execSQL("CREATE TABLE IF NOT EXISTS NutritionLog (" +
                    "entryID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "traineeID INTEGER NOT NULL, " +
                    "date TEXT NOT NULL, " +
                    "mealType TEXT NOT NULL, " +
                    "calories INTEGER DEFAULT 0, " +
                    "protein INTEGER DEFAULT 0, " +
                    "totalFat INTEGER DEFAULT 0, " +
                    "FOREIGN KEY(traineeID) REFERENCES User(userID) ON DELETE CASCADE, " +
                    "UNIQUE(traineeID, date, mealType))");
        }
        // Add nutrition tracking table for versions 7-10
        if (oldVersion < 11 && oldVersion >= 7) {
            db.execSQL("CREATE TABLE IF NOT EXISTS NutritionLog (" +
                    "entryID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "traineeID INTEGER NOT NULL, " +
                    "date TEXT NOT NULL, " +
                    "mealType TEXT NOT NULL, " +
                    "calories INTEGER DEFAULT 0, " +
                    "protein INTEGER DEFAULT 0, " +
                    "totalFat INTEGER DEFAULT 0, " +
                    "FOREIGN KEY(traineeID) REFERENCES User(userID) ON DELETE CASCADE, " +
                    "UNIQUE(traineeID, date, mealType))");
        }
    }

    /* ====================== Sign Up & Login ====================== */
    public boolean signUp(String name, String phone, String email, String password, boolean isTrainer,
                          int height, int weight, int age) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("phone", phone);
        values.put("email", email);
        values.put("passwordHash", hashPassword(password));
        values.put("isTrainer", isTrainer ? 1 : 0);
        if (!isTrainer) {
            values.put("traineeHeight", height);
            values.put("traineeAge", age);
        }
        long r = db.insert("User", null, values);
        db.close();
        if (r > 0) {
            if (!isTrainer) return addWeightSnapshot(r, weight);
            return true;
        }
        return false;
    }

    @SuppressLint("Range")
    public long login(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM User WHERE email=?", new String[]{email});
        if (!c.moveToFirst()) { c.close(); return -1; }
        String stored = c.getString(c.getColumnIndex("passwordHash"));
        if (!hashPassword(password).equals(stored)) { c.close(); return -1; }
        long id = c.getLong(c.getColumnIndex("userID"));
        c.close();
        return id;
    }

    public boolean isTrainer(long userID) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT isTrainer FROM User WHERE userID=?", new String[]{String.valueOf(userID)});
        c.moveToFirst();
        boolean res = c.getInt(0) == 1;
        c.close();
        return res;
    }

    /* ====================== Trainer Profile ====================== */
    public boolean updateTrainerProfile(long trainerId, String name, String specialization,
                                        int handleNum, String about) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        if (name != null) cv.put("name", name);
        if (specialization != null) cv.put("trainerSpecialization", specialization);
        cv.put("trainerHandleNum", handleNum);
        if (about != null) cv.put("trainerAbout", about);
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
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM User WHERE trainerID = ?",
                new String[]{String.valueOf(trainerId)});
        int cnt = c.moveToFirst() ? c.getInt(0) : 0;
        c.close();
        return cnt;
    }

    /* ====================== Weight ====================== */
    public boolean addWeightSnapshot(long trainee, int weight) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("traineeID", trainee);
        cv.put("weight", weight);
        cv.put("time", System.currentTimeMillis());
        long r = db.insert("WeightSnapshot", null, cv);
        db.close();
        return r > 0;
    }

    public int getLatestWeight(long trainee) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT weight FROM WeightSnapshot WHERE traineeID=? ORDER BY time DESC LIMIT 1",
                new String[]{String.valueOf(trainee)});
        int w = 0;
        if (c.moveToFirst()) w = c.getInt(0);
        c.close();
        return w;
    }

    /* ====================== Plan Related ====================== */
    public boolean addPlan(String exerciseName, String exerciseContent) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_EXERCISE_NAME, exerciseName);
        cv.put(COL_EXERCISE_CONTENT, exerciseContent);
        long r = db.insert(TABLE_PLAN, null, cv);
        db.close();
        return r > 0;
    }

    public Cursor getAllPlans() {
        return this.getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_PLAN, null);
    }

    public Cursor getPlanById(long planId) {
        return this.getReadableDatabase().rawQuery(
                "SELECT * FROM " + TABLE_PLAN + " WHERE " + COL_PLAN_ID + " = ?",
                new String[]{String.valueOf(planId)});
    }

    public boolean updatePlan(long planId, String exerciseName, String exerciseContent) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        if (exerciseName != null) cv.put(COL_EXERCISE_NAME, exerciseName);
        if (exerciseContent != null) cv.put(COL_EXERCISE_CONTENT, exerciseContent);
        int r = db.update(TABLE_PLAN, cv, COL_PLAN_ID + " = ?",
                new String[]{String.valueOf(planId)});
        db.close();
        return r > 0;
    }

    public boolean deletePlan(long planId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int r = db.delete(TABLE_PLAN, COL_PLAN_ID + " = ?", new String[]{String.valueOf(planId)});
        db.close();
        return r > 0;
    }

    /* ====================== Assign Plan to Trainee ====================== */
    public boolean assignPlanToTrainee(long planId, long traineeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("planID", planId);
        cv.put("traineeID", traineeId);
        cv.put("assignedDate", getCurrentDateTime());
        long r = db.insert(TABLE_ASSIGNED_PLAN, null, cv);
        db.close();
        return r != -1;
    }

    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    /* ====================== Reminder ====================== */
    public boolean addReminder(String remindDate, String remindContent) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_REMIND_DATE, remindDate);
        cv.put(COL_REMIND_CONTENT, remindContent);
        long r = db.insert(TABLE_REMIND, null, cv);
        db.close();
        return r > 0;
    }

    public Cursor getAllReminders() {
        return this.getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_REMIND, null);
    }

    /* ====================== Other Common Methods ====================== */
    public String getName(long userID) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT name FROM User WHERE userID=?", new String[]{String.valueOf(userID)});
        c.moveToFirst();
        String name = c.getString(0);
        c.close();
        return name;
    }

    public boolean setName(long userID, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        int r = db.update("User", cv, "userID=?", new String[]{String.valueOf(userID)});
        db.close();
        return r > 0;
    }

    public long getTraineeTrainer(long userID) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT trainerID FROM User WHERE userID=?", new String[]{String.valueOf(userID)});
        c.moveToFirst();
        long trainerID = c.isNull(0) ? -1 : c.getLong(0);
        c.close();
        return trainerID;
    }

    public boolean setTraineeTrainer(long userID, long trainerID) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("trainerID", trainerID);
        int r = db.update("User", cv, "userID=?", new String[]{String.valueOf(userID)});
        db.close();
        return r > 0;
    }

    public int getTraineeHeight(long trainee) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT traineeHeight FROM User WHERE userID=?", new String[]{String.valueOf(trainee)});
        c.moveToFirst();
        int h = c.getInt(0);
        c.close();
        return h;
    }

    public boolean setTraineeHeight(long trainee, int height) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("traineeHeight", height);
        int r = db.update("User", cv, "userID=?", new String[]{String.valueOf(trainee)});
        db.close();
        return r > 0;
    }

    public int getTraineeAge(long trainee) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT traineeAge FROM User WHERE userID=?", new String[]{String.valueOf(trainee)});
        c.moveToFirst();
        int age = c.getInt(0);
        c.close();
        return age;
    }

    public boolean setTraineeAge(long trainee, int age) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("traineeAge", age);
        int r = db.update("User", cv, "userID=?", new String[]{String.valueOf(trainee)});
        db.close();
        return r > 0;
    }

    public Cursor listViewTrainers() {
        return this.getReadableDatabase()
                .rawQuery("SELECT userID AS _id, userID, name, email, trainerSpecialization FROM User WHERE isTrainer=1 ORDER BY name", null);
    }

    public Cursor getTraineesByTrainer(long trainerId) {
        return this.getReadableDatabase().rawQuery(
                "SELECT userID AS _id, userID, name FROM User WHERE trainerID = ? AND isTrainer = 0",
                new String[]{String.valueOf(trainerId)});
    }

    public int getTraineeCount(long trainerID) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM User WHERE trainerID=?",new String[]{Long.toString(trainerID)});
        c.moveToFirst();
        int count = c.getInt(0);
        c.close();
        return count;
    }

    /* ====================== Password Hashing ====================== */
    private static String hashPassword(String password) {
        String normalized = Normalizer.normalize(password, Normalizer.Form.NFKD);
        byte[] digest = md.digest(normalized.getBytes(StandardCharsets.UTF_8));
        return android.util.Base64.encodeToString(digest, android.util.Base64.NO_WRAP);
    }

    public Cursor getAllTrainees() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT userID AS _id, * FROM User WHERE isTrainer = 0";
        return db.rawQuery(query, null);
    }

    public Cursor getTraineeDetails(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM User WHERE userID = ?";
        return db.rawQuery(query, new String[]{String.valueOf(id)});
    }
}