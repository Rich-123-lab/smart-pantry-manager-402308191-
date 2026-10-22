package com.welsh.smartpantry.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The single Room database for the app, stored on the device in the file
 * smart_pantry.db. Because the file lives on internal storage, everything
 * survives closing and reopening the app.
 */
@Database(entities = {PantryItem.class, Recipe.class, RecipeIngredient.class},
        version = 1,
        exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract PantryItemDao pantryItemDao();

    public abstract RecipeDao recipeDao();

    // Room refuses to run queries on the main (UI) thread, so all database
    // work is handed to this small pool of background threads
    public static final ExecutorService databaseExecutor = Executors.newFixedThreadPool(2);

    // volatile + synchronized = only one instance is ever created,
    // even if two screens ask for it at the same moment (singleton)
    private static volatile AppDatabase instance;

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "smart_pantry.db")
                            .addCallback(seedCallback)
                            .build();
                }
            }
        }
        return instance;
    }

    // onCreate runs exactly once: the very first time the database file is
    // created on this device. That is where the recipe collection is loaded.
    // It runs before any query returns, so recipes are always there.
    private static final RoomDatabase.Callback seedCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            RecipeSeeder.seed(db);
        }
    };
}
