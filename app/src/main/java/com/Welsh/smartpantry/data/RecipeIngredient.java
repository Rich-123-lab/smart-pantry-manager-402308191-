package com.welsh.smartpantry.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * One ingredient line of a recipe, e.g. "3 pcs egg".
 * recipe_id is a foreign key to recipes.id, so each row belongs to exactly
 * one recipe, and deleting a recipe deletes its ingredient rows (CASCADE).
 */
@Entity(tableName = "recipe_ingredients",
        foreignKeys = @ForeignKey(entity = Recipe.class,
                parentColumns = "id",
                childColumns = "recipe_id",
                onDelete = ForeignKey.CASCADE),
        indices = @Index("recipe_id"))
public class RecipeIngredient {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "recipe_id")
    private long recipeId;

    @NonNull
    private String name;

    private double quantity;

    @NonNull
    private String unit;

    public RecipeIngredient(long recipeId, @NonNull String name,
                            double quantity, @NonNull String unit) {
        this.recipeId = recipeId;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getRecipeId() { return recipeId; }
    public void setRecipeId(long recipeId) { this.recipeId = recipeId; }

    @NonNull
    public String getName() { return name; }
    public void setName(@NonNull String name) { this.name = name; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    @NonNull
    public String getUnit() { return unit; }
    public void setUnit(@NonNull String unit) { this.unit = unit; }
}
