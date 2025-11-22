package com.example.vimora;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.vimora.DatabaseHelper;
import com.example.vimora.NutritionEntry;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for nutrition-related database operations
 * Keeps nutrition logic separate from the main DatabaseHelper
 */
public class NutritionDatabaseHelper {

    private DatabaseHelper dbHelper;

    public NutritionDatabaseHelper(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * Add or update a nutrition entry for a specific meal
     * @param traineeID User ID
     * @param date Date in format "yyyy-MM-dd"
     * @param mealType "breakfast", "lunch", or "dinner"
     * @param calories Calories consumed
     * @param protein Protein in grams
     * @param totalFat Total fat in grams
     * @return true if successful
     */
    public boolean saveNutritionEntry(long traineeID, String date, String mealType,
                                      int calories, int protein, int totalFat) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Check if entry already exists
        Cursor cursor = db.rawQuery(
                "SELECT entryID FROM NutritionLog WHERE traineeID=? AND date=? AND mealType=?",
                new String[]{String.valueOf(traineeID), date, mealType}
        );

        boolean exists = cursor.moveToFirst();
        long entryID = exists ? cursor.getLong(0) : -1;
        cursor.close();

        ContentValues cv = new ContentValues();
        cv.put("traineeID", traineeID);
        cv.put("date", date);
        cv.put("mealType", mealType);
        cv.put("calories", calories);
        cv.put("protein", protein);
        cv.put("totalFat", totalFat);

        long result;
        if (exists) {
            // Update existing entry
            result = db.update("NutritionLog", cv, "entryID=?",
                    new String[]{String.valueOf(entryID)});
        } else {
            // Insert new entry
            result = db.insert("NutritionLog", null, cv);
        }

        return result != -1;
    }

    /**
     * Get nutrition data for a specific date and meal
     */
    public NutritionEntry getNutritionEntry(long traineeID, String date, String mealType) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM NutritionLog WHERE traineeID=? AND date=? AND mealType=?",
                new String[]{String.valueOf(traineeID), date, mealType}
        );

        NutritionEntry entry = null;
        if (cursor.moveToFirst()) {
            entry = new NutritionEntry(
                    cursor.getLong(cursor.getColumnIndexOrThrow("entryID")),
                    cursor.getLong(cursor.getColumnIndexOrThrow("traineeID")),
                    cursor.getString(cursor.getColumnIndexOrThrow("date")),
                    cursor.getString(cursor.getColumnIndexOrThrow("mealType")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("calories")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("protein")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("totalFat"))
            );
        }
        cursor.close();
        return entry;
    }

    /**
     * Get total nutrition for a specific date
     * @return Map with keys: "calories", "protein", "totalFat"
     */
    public Map<String, Integer> getDailyTotal(long traineeID, String date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT SUM(calories) as totalCal, SUM(protein) as totalProt, SUM(totalFat) as totalFat " +
                        "FROM NutritionLog WHERE traineeID=? AND date=?",
                new String[]{String.valueOf(traineeID), date}
        );

        Map<String, Integer> totals = new HashMap<>();
        if (cursor.moveToFirst()) {
            totals.put("calories", cursor.getInt(0));
            totals.put("protein", cursor.getInt(1));
            totals.put("totalFat", cursor.getInt(2));
        } else {
            totals.put("calories", 0);
            totals.put("protein", 0);
            totals.put("totalFat", 0);
        }
        cursor.close();
        return totals;
    }

    /**
     * Get monthly average nutrition data
     * @param traineeID User ID
     * @param yearMonth Year and month in format "yyyy-MM"
     * @return Map with keys: "avgCalories", "avgProtein", "avgTotalFat"
     */
    public Map<String, Double> getMonthlyAverage(long traineeID, String yearMonth) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Get all entries for the month
        Cursor cursor = db.rawQuery(
                "SELECT date, SUM(calories) as dailyCal, SUM(protein) as dailyProt, SUM(totalFat) as dailyFat " +
                        "FROM NutritionLog WHERE traineeID=? AND date LIKE ? " +
                        "GROUP BY date",
                new String[]{String.valueOf(traineeID), yearMonth + "%"}
        );

        double totalCalories = 0;
        double totalProtein = 0;
        double totalFat = 0;
        int dayCount = 0;

        while (cursor.moveToNext()) {
            totalCalories += cursor.getInt(1);
            totalProtein += cursor.getInt(2);
            totalFat += cursor.getInt(3);
            dayCount++;
        }
        cursor.close();

        Map<String, Double> averages = new HashMap<>();
        if (dayCount > 0) {
            averages.put("avgCalories", totalCalories / dayCount);
            averages.put("avgProtein", totalProtein / dayCount);
            averages.put("avgTotalFat", totalFat / dayCount);
        } else {
            averages.put("avgCalories", 0.0);
            averages.put("avgProtein", 0.0);
            averages.put("avgTotalFat", 0.0);
        }

        return averages;
    }

    /**
     * Get all nutrition entries for a specific date
     */
    public Cursor getAllEntriesForDate(long traineeID, String date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM NutritionLog WHERE traineeID=? AND date=? ORDER BY mealType",
                new String[]{String.valueOf(traineeID), date}
        );
    }

    /**
     * Delete a nutrition entry
     */
    public boolean deleteNutritionEntry(long entryID) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete("NutritionLog", "entryID=?",
                new String[]{String.valueOf(entryID)});
        return rows > 0;
    }
}