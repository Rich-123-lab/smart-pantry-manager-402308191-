package com.welsh.smartpantry.data;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

/**
 * Not a table. Room fills this object by reading one row from recipes and
 * then every row in recipe_ingredients whose recipe_id matches that row's id
 * (a one to many relationship).
 */
public class RecipeWithIngredients {

    @Embedded
    public Recipe recipe;

    @Relation(parentColumn = "id", entityColumn = "recipe_id")
    public List<RecipeIngredient> ingredients;
}
