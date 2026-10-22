package com.welsh.smartpantry.data;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

/**
 * Read access to the recipe collection. Recipes are seeded once when the
 * database is first created (see RecipeSeeder), so the app only reads them.
 * @Transaction makes Room run the recipe query and the ingredient query
 * together, so the two reads always see consistent data.
 */
@Dao
public interface RecipeDao {

    @Transaction
    @Query("SELECT * FROM recipes ORDER BY name COLLATE NOCASE ASC")
    List<RecipeWithIngredients> getAllWithIngredients();

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :id")
    RecipeWithIngredients getById(long id);

    @Query("SELECT COUNT(*) FROM recipes")
    int count();
}
