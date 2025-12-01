package com.example.vimora;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.vimora.DatabaseHelper;
import com.example.vimora.NutritionEntry;

import java.util.HashMap;
import java.util.Map;

// helper class for nutrition-related database operations
// to keep nutrition logic organized
// instead of putting everything in the main DatabaseHelper
public class NutritionDatabaseHelper {

    // reference to the main database helper
    private DatabaseHelper dbHelper;

    // constructor - takes the main database helper as parameter
    public NutritionDatabaseHelper(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    // saves or updates a nutrition entry for a specific meal
    // if an entry already exists for this date and meal type, it updates it
    // if not, it creates a new entry
    public boolean saveNutritionEntry(long traineeID, String date, String mealType,
                                      int calories, int protein, int totalFat) {
        // get writable database to make changes
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // check if an entry already exists for this trainee, date, and meal type
        Cursor cursor = db.rawQuery(
                "SELECT entryID FROM NutritionLog WHERE traineeID=? AND date=? AND mealType=?",
                new String[]{String.valueOf(traineeID), date, mealType}
        );

        // if cursor moves to first, that means an entry exists
        boolean exists = cursor.moveToFirst();
        // get the entry id if it exists, otherwise -1
        long entryID = exists ? cursor.getLong(0) : -1;
        cursor.close();

        // prepare the values to save
        ContentValues cv = new ContentValues();
        cv.put("traineeID", traineeID);
        cv.put("date", date);
        cv.put("mealType", mealType);
        cv.put("calories", calories);
        cv.put("protein", protein);
        cv.put("totalFat", totalFat);

        long result;
        if (exists) {
            // entry exists - update it
            result = db.update("NutritionLog", cv, "entryID=?",
                    new String[]{String.valueOf(entryID)});
        } else {
            // entry doesn't exist - insert new one
            result = db.insert("NutritionLog", null, cv);
        }

        // return true if operation was successful
        return result != -1;
    }

    // gets nutrition data for a specific date and meal type
    // returns a NutritionEntry object if found, null if not found
    public NutritionEntry getNutritionEntry(long traineeID, String date, String mealType) {
        // get readable database to query data
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // query for this specific entry
        Cursor cursor = db.rawQuery(
                "SELECT * FROM NutritionLog WHERE traineeID=? AND date=? AND mealType=?",
                new String[]{String.valueOf(traineeID), date, mealType}
        );

        NutritionEntry entry = null;
        // if entry exists, create a NutritionEntry object with the data
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

    // gets total nutrition for a specific date
    // adds up all meals (breakfast, lunch, dinner) for that day
    // returns a map with total calories, protein, and fat
    public Map<String, Integer> getDailyTotal(long traineeID, String date) {
        // get readable database
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // sum up all nutrition values for this date
        Cursor cursor = db.rawQuery(
                "SELECT SUM(calories) as totalCal, SUM(protein) as totalProt, SUM(totalFat) as totalFat " +
                        "FROM NutritionLog WHERE traineeID=? AND date=?",
                new String[]{String.valueOf(traineeID), date}
        );

        // create a map to store the totals
        Map<String, Integer> totals = new HashMap<>();
        if (cursor.moveToFirst()) {
            // if data found, store the totals
            totals.put("calories", cursor.getInt(0));
            totals.put("protein", cursor.getInt(1));
            totals.put("totalFat", cursor.getInt(2));
        } else {
            // no data found, return zeros
            totals.put("calories", 0);
            totals.put("protein", 0);
            totals.put("totalFat", 0);
        }
        cursor.close();
        return totals;
    }

    // gets monthly average nutrition data
    // calculates average calories, protein, and fat per day for a month
    // yearMonth should be in format "yyyy-MM" like "2025-11"
    public Map<String, Double> getMonthlyAverage(long traineeID, String yearMonth) {
        // get readable database
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // get daily totals for each day in the month
        // group by date to get one row per day
        Cursor cursor = db.rawQuery(
                "SELECT date, SUM(calories) as dailyCal, SUM(protein) as dailyProt, SUM(totalFat) as dailyFat " +
                        "FROM NutritionLog WHERE traineeID=? AND date LIKE ? " +
                        "GROUP BY date",
                new String[]{String.valueOf(traineeID), yearMonth + "%"}
        );

        // variables to accumulate totals
        double totalCalories = 0;
        double totalProtein = 0;
        double totalFat = 0;
        int dayCount = 0;

        // loop through each day and add to totals
        while (cursor.moveToNext()) {
            totalCalories += cursor.getInt(1);
            totalProtein += cursor.getInt(2);
            totalFat += cursor.getInt(3);
            dayCount++;
        }
        cursor.close();

        // create a map to store the averages
        Map<String, Double> averages = new HashMap<>();
        if (dayCount > 0) {
            // calculate averages by dividing total by number of days
            averages.put("avgCalories", totalCalories / dayCount);
            averages.put("avgProtein", totalProtein / dayCount);
            averages.put("avgTotalFat", totalFat / dayCount);
        } else {
            // no data found, return zeros
            averages.put("avgCalories", 0.0);
            averages.put("avgProtein", 0.0);
            averages.put("avgTotalFat", 0.0);
        }

        return averages;
    }

    // gets all nutrition entries for a specific date
    // returns a cursor with all meals for that day
    public Cursor getAllEntriesForDate(long traineeID, String date) {
        // get readable database
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // query all entries for this date, ordered by meal type
        return db.rawQuery(
                "SELECT * FROM NutritionLog WHERE traineeID=? AND date=? ORDER BY mealType",
                new String[]{String.valueOf(traineeID), date}
        );
    }

    // deletes a nutrition entry by its id
    // returns true if deletion was successful
    public boolean deleteNutritionEntry(long entryID) {
        // get writable database to make changes
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // delete the entry with this id
        int rows = db.delete("NutritionLog", "entryID=?",
                new String[]{String.valueOf(entryID)});
        // if rows > 0, that means deletion was successful
        return rows > 0;
    }
}