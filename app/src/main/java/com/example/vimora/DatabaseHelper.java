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
    private static final int DATABASE_VERSION = 14;   // ← CHANGED from 13 to 14

    // Plan Table
    public static final String TABLE_PLAN = "PlanTable";
    public static final String COL_PLAN_ID = "PlanID";
    public static final String COL_EXERCISE_NAME = "ExerciseName";
    private static final String COL_EXERCISE_CONTENT = "ExerciseContent";

    // Update Plan Table
    private static final String COL_ITEM1 = "item1";
    private static final String COL_REPS1 = "reps1";
    private static final String COL_SETS1 = "sets1";
    private static final String COL_ITEM2 = "item2";
    private static final String COL_REPS2 = "reps2";
    private static final String COL_SETS2 = "sets2";
    private static final String COL_ITEM3 = "item3";
    private static final String COL_REPS3 = "reps3";
    private static final String COL_SETS3 = "sets3";
    private static final String COL_REST_DURATION = "rest_duration";
    private static final String COL_WORKOUT_DURATION = "workout_duration";

    // Reminder Table
    private static final String TABLE_REMIND = "RemindTable";
    private static final String COL_REMIND_ID = "RemindID";
    private static final String COL_REMIND_DATE = "RemindDate";
    private static final String COL_REMIND_CONTENT = "RemindContent";

    // AssignedPlan Table
    public static final String TABLE_ASSIGNED_PLAN = "AssignedPlan";

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
                COL_EXERCISE_CONTENT + " TEXT," +
                COL_ITEM1 + " TEXT," +
                COL_REPS1 + " TEXT," +
                COL_SETS1 + " TEXT," +
                COL_ITEM2 + " TEXT," +
                COL_REPS2 + " TEXT," +
                COL_SETS2 + " TEXT," +
                COL_ITEM3 + " TEXT," +
                COL_REPS3 + " TEXT," +
                COL_SETS3 + " TEXT," +
                COL_REST_DURATION + " TEXT," +
                COL_WORKOUT_DURATION + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_REMIND + " (" +
                COL_REMIND_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_REMIND_DATE + " TEXT NOT NULL," +
                COL_REMIND_CONTENT + " TEXT NOT NULL," +
                "trainerID INTEGER NOT NULL," +
                "traineeID INTEGER NOT NULL," +
                "FOREIGN KEY (trainerID) REFERENCES User(userID) ON DELETE CASCADE," +
                "FOREIGN KEY (traineeID) REFERENCES User(userID) ON DELETE CASCADE)");

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

        // TABLE FOR WORKOUT COMPLETION TRACKING - VERSION 14 WITH CALORIES & DURATION
        db.execSQL("CREATE TABLE WorkoutCompletion (" +
                "completionID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "traineeID INTEGER NOT NULL, " +
                "planID INTEGER, " +
                "completionDate TEXT NOT NULL, " +
                "caloriesBurned INTEGER DEFAULT 0, " +
                "duration INTEGER DEFAULT 0, " +
                "FOREIGN KEY(traineeID) REFERENCES User(userID) ON DELETE CASCADE, " +
                "FOREIGN KEY(planID) REFERENCES " + TABLE_PLAN + "(" + COL_PLAN_ID + ") ON DELETE CASCADE, " +
                "UNIQUE(traineeID, completionDate))");
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

        // UPGRADE LOGIC for version 12
        if (oldVersion < 12) {
            db.execSQL("CREATE TABLE IF NOT EXISTS WorkoutCompletion (" +
                    "completionID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "traineeID INTEGER NOT NULL, " +
                    "planID INTEGER NOT NULL, " +
                    "completionDate TEXT NOT NULL, " +
                    "FOREIGN KEY(traineeID) REFERENCES User(userID) ON DELETE CASCADE, " +
                    "FOREIGN KEY(planID) REFERENCES PlanTable(PlanID) ON DELETE CASCADE, " +
                    "UNIQUE(traineeID, planID, completionDate))");
        }

        // UPGRADE LOGIC for version 13 - Add PlanTable columns
        if (oldVersion < 13) {
            db.execSQL("ALTER TABLE " + TABLE_PLAN + " ADD COLUMN " + COL_ITEM1 + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_PLAN + " ADD COLUMN " + COL_REPS1 + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_PLAN + " ADD COLUMN " + COL_SETS1 + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_PLAN + " ADD COLUMN " + COL_ITEM2 + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_PLAN + " ADD COLUMN " + COL_REPS2 + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_PLAN + " ADD COLUMN " + COL_SETS2 + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_PLAN + " ADD COLUMN " + COL_ITEM3 + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_PLAN + " ADD COLUMN " + COL_REPS3 + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_PLAN + " ADD COLUMN " + COL_SETS3 + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_PLAN + " ADD COLUMN " + COL_REST_DURATION + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_PLAN + " ADD COLUMN " + COL_WORKOUT_DURATION + " TEXT");
        }

        // UPGRADE LOGIC for version 14 - SAFE MIGRATION: Add calories & duration to WorkoutCompletion
        if (oldVersion < 14) {
            try {
                android.util.Log.i("DatabaseHelper", "Starting WorkoutCompletion migration to v14");

                // Check if table exists and has data
                Cursor checkCursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='WorkoutCompletion'", null);
                boolean tableExists = checkCursor.getCount() > 0;
                checkCursor.close();

                if (tableExists) {
                    Cursor dataCursor = db.rawQuery("SELECT COUNT(*) FROM WorkoutCompletion", null);
                    boolean hasData = dataCursor.moveToFirst() && dataCursor.getInt(0) > 0;
                    dataCursor.close();

                    if (hasData) {

                        android.util.Log.i("DatabaseHelper", "Migrating existing WorkoutCompletion data");

                        // Rename old table
                        db.execSQL("ALTER TABLE WorkoutCompletion RENAME TO WorkoutCompletion_backup");

                        // Create new table with updated schema
                        db.execSQL("CREATE TABLE WorkoutCompletion (" +
                                "completionID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "traineeID INTEGER NOT NULL, " +
                                "planID INTEGER, " +
                                "completionDate TEXT NOT NULL, " +
                                "caloriesBurned INTEGER DEFAULT 0, " +
                                "duration INTEGER DEFAULT 0, " +
                                "FOREIGN KEY(traineeID) REFERENCES User(userID) ON DELETE CASCADE, " +
                                "FOREIGN KEY(planID) REFERENCES PlanTable(PlanID) ON DELETE CASCADE, " +
                                "UNIQUE(traineeID, completionDate))");

                        // potential duplicates due to new UNIQUE constraint
                        // Takes the first record per traineeID+date if duplicates exist
                        db.execSQL("INSERT INTO WorkoutCompletion (traineeID, planID, completionDate, caloriesBurned, duration) " +
                                "SELECT traineeID, planID, completionDate, 0, 0 " +
                                "FROM WorkoutCompletion_backup " +
                                "GROUP BY traineeID, completionDate");

                        // Drop backup table
                        db.execSQL("DROP TABLE WorkoutCompletion_backup");

                        android.util.Log.i("DatabaseHelper", "WorkoutCompletion migration to v14 completed successfully");
                    } else {
                        // No data, safe to drop and recreate
                        android.util.Log.i("DatabaseHelper", "No data in WorkoutCompletion, recreating table");
                        db.execSQL("DROP TABLE IF EXISTS WorkoutCompletion");
                        db.execSQL("CREATE TABLE WorkoutCompletion (" +
                                "completionID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "traineeID INTEGER NOT NULL, " +
                                "planID INTEGER, " +
                                "completionDate TEXT NOT NULL, " +
                                "caloriesBurned INTEGER DEFAULT 0, " +
                                "duration INTEGER DEFAULT 0, " +
                                "FOREIGN KEY(traineeID) REFERENCES User(userID) ON DELETE CASCADE, " +
                                "FOREIGN KEY(planID) REFERENCES PlanTable(PlanID) ON DELETE CASCADE, " +
                                "UNIQUE(traineeID, completionDate))");
                    }
                } else {
                    // Table doesn't exist, create it
                    android.util.Log.i("DatabaseHelper", "Creating WorkoutCompletion table for first time");
                    db.execSQL("CREATE TABLE WorkoutCompletion (" +
                            "completionID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "traineeID INTEGER NOT NULL, " +
                            "planID INTEGER, " +
                            "completionDate TEXT NOT NULL, " +
                            "caloriesBurned INTEGER DEFAULT 0, " +
                            "duration INTEGER DEFAULT 0, " +
                            "FOREIGN KEY(traineeID) REFERENCES User(userID) ON DELETE CASCADE, " +
                            "FOREIGN KEY(planID) REFERENCES PlanTable(PlanID) ON DELETE CASCADE, " +
                            "UNIQUE(traineeID, completionDate))");
                }
            } catch (Exception e) {
                // if migration fails, drop and recreate
                android.util.Log.e("DatabaseHelper", "Migration to v14 failed, recreating table: " + e.getMessage());
                db.execSQL("DROP TABLE IF EXISTS WorkoutCompletion");
                db.execSQL("DROP TABLE IF EXISTS WorkoutCompletion_backup");
                db.execSQL("CREATE TABLE WorkoutCompletion (" +
                        "completionID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "traineeID INTEGER NOT NULL, " +
                        "planID INTEGER, " +
                        "completionDate TEXT NOT NULL, " +
                        "caloriesBurned INTEGER DEFAULT 0, " +
                        "duration INTEGER DEFAULT 0, " +
                        "FOREIGN KEY(traineeID) REFERENCES User(userID) ON DELETE CASCADE, " +
                        "FOREIGN KEY(planID) REFERENCES PlanTable(PlanID) ON DELETE CASCADE, " +
                        "UNIQUE(traineeID, completionDate))");
            }
        }

        db.execSQL("DROP TABLE IF EXISTS RemindTable");
        onCreate(db);
    }

    // Sign Up & Login
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

    // Trainer Profile
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

    // Weight
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

    // Plan Related
    public boolean addPlan(String exerciseName, String exerciseContent) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_EXERCISE_NAME, exerciseName);
        cv.put(COL_EXERCISE_CONTENT, exerciseContent);
        long r = db.insert(TABLE_PLAN, null, cv);
        db.close();
        return r > 0;
    }

    // Plan Related Update
    public boolean addPlan(String exerciseName, String item1, String reps1, String sets1,
                           String item2, String reps2, String sets2,
                           String item3, String reps3, String sets3,
                           String restDuration, String workoutDuration) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_EXERCISE_NAME, exerciseName);
        String legacyContent = item1 + "," + reps1 + "," + sets1 + "," + item2 + "," + reps2 + "," + sets2 + "," +
                item3 + "," + reps3 + "," + sets3 + "," + restDuration + "," + workoutDuration;
        cv.put(COL_EXERCISE_CONTENT, legacyContent);
        cv.put(COL_ITEM1, item1);
        cv.put(COL_REPS1, reps1);
        cv.put(COL_SETS1, sets1);
        cv.put(COL_ITEM2, item2);
        cv.put(COL_REPS2, reps2);
        cv.put(COL_SETS2, sets2);
        cv.put(COL_ITEM3, item3);
        cv.put(COL_REPS3, reps3);
        cv.put(COL_SETS3, sets3);
        cv.put(COL_REST_DURATION, restDuration);
        cv.put(COL_WORKOUT_DURATION, workoutDuration);
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


    public boolean updatePlan(long planId, String exerciseName, String item1, String reps1, String sets1,
                              String item2, String reps2, String sets2,
                              String item3, String reps3, String sets3,
                              String restDuration, String workoutDuration) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        if (exerciseName != null) cv.put(COL_EXERCISE_NAME, exerciseName);
        String legacyContent = item1 + "," + reps1 + "," + sets1 + "," + item2 + "," + reps2 + "," + sets2 + "," +
                item3 + "," + reps3 + "," + sets3 + "," + restDuration + "," + workoutDuration;
        cv.put(COL_EXERCISE_CONTENT, legacyContent);
        cv.put(COL_ITEM1, item1);
        cv.put(COL_REPS1, reps1);
        cv.put(COL_SETS1, sets1);
        cv.put(COL_ITEM2, item2);
        cv.put(COL_REPS2, reps2);
        cv.put(COL_SETS2, sets2);
        cv.put(COL_ITEM3, item3);
        cv.put(COL_REPS3, reps3);
        cv.put(COL_SETS3, sets3);
        cv.put(COL_REST_DURATION, restDuration);
        cv.put(COL_WORKOUT_DURATION, workoutDuration);
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

    // Assign Plan to Trainee
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

    // Reminder
    public boolean addReminder(String remindDate, String remindContent, long trainee, long trainer) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_REMIND_DATE, remindDate);
        cv.put(COL_REMIND_CONTENT, remindContent);
        cv.put("traineeID",trainee);
        cv.put("trainerID",trainer);
        long r = db.insert(TABLE_REMIND, null, cv);
        db.close();
        return r > 0;
    }


    public Cursor getRemindersForTraineeTrainer(long traineeID, long trainerID) {
        return this.getReadableDatabase().rawQuery("SELECT RemindID as _id,* FROM RemindTable WHERE traineeID = ? AND trainerID=? ORDER BY RemindDate DESC",new String[]{Long.toString(traineeID),Long.toString(trainerID)});
    }

    public String getTrainerNameFromReminder(long reminderID) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT name FROM User WHERE userID=(SELECT trainerID FROM RemindTable WHERE RemindID=?)", new String[]{String.valueOf(reminderID)});
        c.moveToFirst();
        String name = c.getString(0);
        c.close();
        return name;
    }

    public String getDateFromReminder(long reminderID) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT RemindDate FROM RemindTable WHERE RemindID=?", new String[]{String.valueOf(reminderID)});
        c.moveToFirst();
        String date = c.getString(0);
        c.close();
        return date;
    }

    public String getMessageFromReminder(long reminderID) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT RemindContent FROM RemindTable WHERE RemindID=?", new String[]{String.valueOf(reminderID)});
        c.moveToFirst();
        String message = c.getString(0);
        c.close();
        return message;
    }

    // Other Common Methods
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

    /* Password Hashing */
    private static String hashPassword(String password) {
        String normalized = Normalizer.normalize(password, Normalizer.Form.NFKD);
        byte[] digest = md.digest(normalized.getBytes(StandardCharsets.UTF_8));
        return android.util.Base64.encodeToString(digest, android.util.Base64.NO_WRAP);
    }

    public Cursor getTraineeDetails(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM User WHERE userID = ?";
        return db.rawQuery(query, new String[]{String.valueOf(id)});
    }

    public boolean markWorkoutComplete(long traineeID, long planID, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("traineeID", traineeID);
        if (planID > 0) {
            cv.put("planID", planID);
        } else {
            cv.putNull("planID");
        }
        cv.put("completionDate", date);
        long result = db.insert("WorkoutCompletion", null, cv);
        db.close();
        return result != -1;
    }

    public boolean isWorkoutCompletedOnDate(long traineeID, String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM WorkoutCompletion WHERE traineeID = ? AND completionDate = ?",
                new String[]{String.valueOf(traineeID), date}
        );
        boolean completed = false;
        if (c.moveToFirst()) {
            completed = c.getInt(0) > 0;
        }
        c.close();
        db.close();
        return completed;
    }

    public Cursor getCompletedDatesForMonth(long traineeID, String yearMonth) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT DISTINCT completionDate FROM WorkoutCompletion " +
                        "WHERE traineeID = ? AND completionDate LIKE ? " +
                        "ORDER BY completionDate",
                new String[]{String.valueOf(traineeID), yearMonth + "%"}
        );
    }


}