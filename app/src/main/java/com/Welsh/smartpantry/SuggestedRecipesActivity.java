package com.Welsh.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.Welsh.smartpantry.data.AppDatabase;
import com.Welsh.smartpantry.data.PantryItem;
import com.Welsh.smartpantry.data.RecipeWithIngredients;

import java.util.ArrayList;
import java.util.List;

/**
 * Suggested Recipes screen. Shows ONLY the recipes the user can cook with
 * what is in the pantry right now (strict rule), plus an optional
 * "Almost there" section for recipes exactly one ingredient short.
 */
public class SuggestedRecipesActivity extends AppCompatActivity {

    private RecipeAdapter adapter;
    private TextView textEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_suggested_recipes);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.title_recipes);

        textEmpty = findViewById(R.id.textEmpty);
        RecyclerView recyclerView = findViewById(R.id.recyclerRecipes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Tapping a recipe opens its detail screen, passing the id in the Intent
        adapter = new RecipeAdapter(recipe -> {
            Intent intent = new Intent(this, RecipeDetailActivity.class);
            intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        BottomNavigationView nav = findViewById(R.id.bottomNav);
        NavHelper.setup(this, nav, R.id.nav_recipes);
    }

    /** Runs every time the screen becomes visible, so results reflect pantry changes. */
    @Override
    protected void onResume() {
        super.onResume();
        boolean ignoreExpired = AppPrefs.ignoreExpired(this);
        boolean showAlmost = AppPrefs.showAlmostThere(this);
        AppDatabase db = AppDatabase.getInstance(this);

        AppDatabase.databaseExecutor.execute(() -> {
            List<PantryItem> pantry = db.pantryItemDao().getAllNow();
            List<RecipeWithIngredients> recipes = db.recipeDao().getAllWithIngredients();
            List<IngredientMatcher.MatchResult> results =
                    IngredientMatcher.matchAll(recipes, pantry, ignoreExpired);

            List<IngredientMatcher.MatchResult> ready = new ArrayList<>();
            List<IngredientMatcher.MatchResult> almost = new ArrayList<>();
            for (IngredientMatcher.MatchResult result : results) {
                if (result.canCook()) {
                    ready.add(result);
                } else if (result.isAlmostThere()) {
                    almost.add(result);
                }
            }
            runOnUiThread(() -> show(ready, showAlmost ? almost : new ArrayList<>()));
        });
    }

    private void show(List<IngredientMatcher.MatchResult> ready,
                      List<IngredientMatcher.MatchResult> almost) {
        adapter.clear();
        if (!ready.isEmpty()) {
            adapter.addHeader(getString(R.string.header_ready, ready.size()));
            adapter.addResults(ready);
        }
        if (!almost.isEmpty()) {
            adapter.addHeader(getString(R.string.header_almost, almost.size()));
            adapter.addResults(almost);
        }
        adapter.refresh();
        // Friendly message instead of a blank screen when nothing can be cooked
        textEmpty.setVisibility(ready.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
