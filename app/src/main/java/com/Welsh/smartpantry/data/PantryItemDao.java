package com.welsh.smartpantry.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Data Access Object for the pantry table.
 * Room generates the SQL implementation of this interface at compile time.
 * These four groups of methods are the full CRUD set the brief asks for.
 */
@Dao
public interface PantryItemDao {

    // CREATE: returns the new row id
    @Insert
    long insert(PantryItem item);

    // READ (observed): the list screen watches this, so it refreshes
    // automatically whenever a row is inserted, updated or deleted
    @Query("SELECT * FROM pantry_items ORDER BY name COLLATE NOCASE ASC")
    LiveData<List<PantryItem>> getAllLive();

    // READ (one off): used by the matching engine on a background thread
    @Query("SELECT * FROM pantry_items")
    List<PantryItem> getAllNow();

    // READ (single): used to load an item into the edit screen
    @Query("SELECT * FROM pantry_items WHERE id = :id")
    PantryItem getById(long id);

    // UPDATE: Room matches the row by its primary key
    @Update
    void update(PantryItem item);

    // DELETE: Room matches the row by its primary key
    @Delete
    void delete(PantryItem item);
}
