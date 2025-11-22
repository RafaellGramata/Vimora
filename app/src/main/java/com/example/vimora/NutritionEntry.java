package com.example.vimora;

/**
 * Model class representing a nutrition entry for a meal
 */
public class NutritionEntry {
    private long entryID;
    private long traineeID;
    private String date;
    private String mealType; // "breakfast", "lunch", "dinner"
    private int calories;
    private int protein;
    private int totalFat;

    // Constructor
    public NutritionEntry(long traineeID, String date, String mealType,
                          int calories, int protein, int totalFat) {
        this.traineeID = traineeID;
        this.date = date;
        this.mealType = mealType;
        this.calories = calories;
        this.protein = protein;
        this.totalFat = totalFat;
    }

    // Constructor with ID (for retrieved data)
    public NutritionEntry(long entryID, long traineeID, String date, String mealType,
                          int calories, int protein, int totalFat) {
        this.entryID = entryID;
        this.traineeID = traineeID;
        this.date = date;
        this.mealType = mealType;
        this.calories = calories;
        this.protein = protein;
        this.totalFat = totalFat;
    }

    // Getters and Setters
    public long getEntryID() {
        return entryID;
    }

    public void setEntryID(long entryID) {
        this.entryID = entryID;
    }

    public long getTraineeID() {
        return traineeID;
    }

    public void setTraineeID(long traineeID) {
        this.traineeID = traineeID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public int getProtein() {
        return protein;
    }

    public void setProtein(int protein) {
        this.protein = protein;
    }

    public int getTotalFat() {
        return totalFat;
    }

    public void setTotalFat(int totalFat) {
        this.totalFat = totalFat;
    }
}