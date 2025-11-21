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
    private static final int DATABASE_VERSION = 10;


    private static final String TABLE_PLAN = "PlanTable";
    private static final String COL_PLAN_ID = "PlanID";
    private static final String COL_EXERCISE_NAME = "ExerciseName";
    private static final String COL_EXERCISE_CONTENT = "ExerciseContent";


    private static final String TABLE_REMIND = "Reminder";
    private static final String TABLE_EXERCISE_ITEM = "ExerciseItem";
    private static final String COL_ITEM_ID = "ItemID";
    private static final String COL_PLAN_ID_FK = "PlanID";
    private static final String COL_EXERCISE_NAME_ITEM = "ExerciseName";
    private static final String COL_SETS = "Sets";
    private static final String COL_REPS = "Reps";
    private static final String COL_REST_MINUTES = "RestMinutes";
    private static final String COL_ORDER_INDEX = "OrderIndex";


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

        db.execSQL("CREATE TABLE Reminder (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "trainerID INTEGER, " +
                "traineeID INTEGER, " +
                "time INTEGER, " +
                "message TEXT, " +
                "FOREIGN KEY(trainerID) REFERENCES User(userID), " +
                "FOREIGN KEY(traineeID) REFERENCES User(userID))");

        db.execSQL("CREATE TABLE " + TABLE_ASSIGNED_PLAN + " (" +
                "assignmentID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "planID INTEGER NOT NULL, " +
                "traineeID INTEGER NOT NULL, " +
                "assignedDate TEXT NOT NULL, " +
                "FOREIGN KEY(planID) REFERENCES " + TABLE_PLAN + "(" + COL_PLAN_ID + ") ON DELETE CASCADE, " +
                "FOREIGN KEY(traineeID) REFERENCES User(userID) ON DELETE CASCADE)");

        db.execSQL("CREATE TABLE " + TABLE_EXERCISE_ITEM + " (" +
                COL_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_PLAN_ID_FK + " INTEGER NOT NULL, " +
                COL_EXERCISE_NAME_ITEM + " TEXT NOT NULL, " +
                COL_SETS + " INTEGER DEFAULT 0, " +
                COL_REPS + " INTEGER DEFAULT 0, " +
                COL_REST_MINUTES + " INTEGER DEFAULT 0, " +
                COL_ORDER_INDEX + " INTEGER DEFAULT 0, " +
                "FOREIGN KEY(" + COL_PLAN_ID_FK + ") REFERENCES " + TABLE_PLAN + "(" + COL_PLAN_ID + ") ON DELETE CASCADE)");
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
            db.execSQL("CREATE TABLE IF NOT EXISTS Reminder (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "trainerID INTEGER, " +
                    "traineeID INTEGER, " +
                    "time INTEGER, " +
                    "message TEXT, " +
                    "FOREIGN KEY(trainerID) REFERENCES User(userID), " +
                    "FOREIGN KEY(traineeID) REFERENCES User(userID))");
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_ASSIGNED_PLAN + " (" +
                    "assignmentID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "planID INTEGER NOT NULL, " +
                    "traineeID INTEGER NOT NULL, " +
                    "assignedDate TEXT NOT NULL, " +
                    "FOREIGN KEY(planID) REFERENCES " + TABLE_PLAN + "(" + COL_PLAN_ID + ") ON DELETE CASCADE, " +
                    "FOREIGN KEY(traineeID) REFERENCES User(userID) ON DELETE CASCADE)");
        }
        if (oldVersion < 7) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_EXERCISE_ITEM + " (" +
                    COL_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_PLAN_ID_FK + " INTEGER NOT NULL, " +
                    COL_EXERCISE_NAME_ITEM + " TEXT NOT NULL, " +
                    COL_SETS + " INTEGER DEFAULT 0, " +
                    COL_REPS + " INTEGER DEFAULT 0, " +
                    COL_REST_MINUTES + " INTEGER DEFAULT 0, " +
                    COL_ORDER_INDEX + " INTEGER DEFAULT 0, " +
                    "FOREIGN KEY(" + COL_PLAN_ID_FK + ") REFERENCES " + TABLE_PLAN + "(" + COL_PLAN_ID + ") ON DELETE CASCADE)");
        }
        if (oldVersion < 8) {
            db.execSQL("DROP TABLE IF EXISTS Reminder");
            db.execSQL("CREATE TABLE Reminder (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "trainerID INTEGER, " +
                    "traineeID INTEGER, " +
                    "time INTEGER, " +
                    "message TEXT, " +
                    "FOREIGN KEY(trainerID) REFERENCES User(userID), " +
                    "FOREIGN KEY(traineeID) REFERENCES User(userID))");
        }
        if (oldVersion < 10) {
            db.execSQL("CREATE TABLE IF NOT EXISTS Reminder (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "trainerID INTEGER, " +
                    "traineeID INTEGER, " +
                    "time INTEGER, " +
                    "message TEXT, " +
                    "FOREIGN KEY(trainerID) REFERENCES User(userID), " +
                    "FOREIGN KEY(traineeID) REFERENCES User(userID))");
        }

    }


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

    public boolean addReminder(long trainerId, long traineeId, String message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("trainerID", trainerId);
        cv.put("traineeID", traineeId);
        cv.put("time", System.currentTimeMillis());
        cv.put("message", message);
        long result = db.insert("Reminder", null, cv);
        db.close();
        return result != -1;
    }

    public Cursor getRemindersForTrainee(long traineeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT r.*, u.name AS trainerName FROM Reminder r " +
                        "LEFT JOIN User u ON r.trainerID = u.userID " +
                        "WHERE r.traineeID = ? ORDER BY r.time DESC",
                new String[]{String.valueOf(traineeId)}
        );
    }

    public Cursor getAllReminders() {
        return this.getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_REMIND, null);
    }

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
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT userID AS _id, userID, name FROM User WHERE trainerID = ? AND isTrainer = 0 ORDER BY name",
                new String[]{String.valueOf(trainerId)});
    }

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

    public long addExerciseItem(long planId, String exerciseName, int sets, int reps, int restMinutes, int orderIndex) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_PLAN_ID_FK, planId);
        cv.put(COL_EXERCISE_NAME_ITEM, exerciseName);
        cv.put(COL_SETS, sets);
        cv.put(COL_REPS, reps);
        cv.put(COL_REST_MINUTES, restMinutes);
        cv.put(COL_ORDER_INDEX, orderIndex);
        long id = db.insert(TABLE_EXERCISE_ITEM, null, cv);
        db.close();
        return id;
    }

    public Cursor getExerciseItemsByPlanId(long planId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM " + TABLE_EXERCISE_ITEM +
                        " WHERE " + COL_PLAN_ID_FK + " = ? ORDER BY " + COL_ORDER_INDEX + " ASC",
                new String[]{String.valueOf(planId)}
        );
    }

    public boolean updateExerciseItem(long itemId, String name, int sets, int reps, int restMinutes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_EXERCISE_NAME_ITEM, name);
        cv.put(COL_SETS, sets);
        cv.put(COL_REPS, reps);
        cv.put(COL_REST_MINUTES, restMinutes);
        int rows = db.update(TABLE_EXERCISE_ITEM, cv, COL_ITEM_ID + " = ?", new String[]{String.valueOf(itemId)});
        db.close();
        return rows > 0;
    }
}