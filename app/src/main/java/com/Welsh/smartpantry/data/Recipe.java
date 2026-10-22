package com.Welsh.smartpantry.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * A recipe in the built in collection. Its ingredients live in a separate
 * table (recipe_ingredients) because one recipe has many ingredients.
 */
@Entity(tableName = "recipes")
public class Recipe {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private String name;

    @NonNull
    private String description;

    @ColumnInfo(name = "prep_minutes")
    private int prepMinutes;

    // Method steps separated by new line characters
    @NonNull
    private String steps;

    public Recipe(@NonNull String name, @NonNull String description,
                  int prepMinutes, @NonNull String steps) {
        this.name = name;
        this.description = description;
        this.prepMinutes = prepMinutes;
        this.steps = steps;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    @NonNull
    public String getName() { return name; }
    public void setName(@NonNull String name) { this.name = name; }

    @NonNull
    public String getDescription() { return description; }
    public void setDescription(@NonNull String description) { this.description = description; }

    public int getPrepMinutes() { return prepMinutes; }
    public void setPrepMinutes(int prepMinutes) { this.prepMinutes = prepMinutes; }

    @NonNull
    public String getSteps() { return steps; }
    public void setSteps(@NonNull String steps) { this.steps = steps; }
}
