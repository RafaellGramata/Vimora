package com.example.vimora;

// model class that represents a single nutrition entry for a meal
// this stores data like calories, protein, and fat for one meal
public class NutritionEntry {
    // unique id for this entry in the database
    private long entryID;
    // id of the trainee who logged this meal
    private long traineeID;
    // date of the meal in format "yyyy-MM-dd"
    private String date;
    // type of meal: "breakfast", "lunch", or "dinner"
    private String mealType;
    // number of calories in this meal
    private int calories;
    // grams of protein in this meal
    private int protein;
    // grams of fat in this meal
    private int totalFat;

    // constructor for creating a new entry (without id)
    // use this when creating a new entry that hasn't been saved to database yet
    public NutritionEntry(long traineeID, String date, String mealType,
                          int calories, int protein, int totalFat) {
        this.traineeID = traineeID;
        this.date = date;
        this.mealType = mealType;
        this.calories = calories;
        this.protein = protein;
        this.totalFat = totalFat;
    }

    // constructor with id (for entries retrieved from database)
    // use this when loading an existing entry from the database
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

    // getters and setters for all fields
    // these let us read and update the values

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