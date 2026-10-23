package com.Welsh.smartpantry;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.Welsh.smartpantry.data.AppDatabase;
import com.Welsh.smartpantry.data.PantryItem;
import com.Welsh.smartpantry.data.RecipeIngredient;
import com.Welsh.smartpantry.data.RecipeWithIngredients;

import java.util.List;
import java.util.Map;

/**
 * Recipe Detail screen. Receives the recipe id through the Intent, then shows
 * the description, each ingredient with a tick or cross (do I have enough?)
 * and the numbered method steps.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "com.Welsh.smartpantry.EXTRA_RECIPE_ID";

    private TextView textDescription;
    private TextView textMeta;
    private TextView textIngredients;
    private TextView textSteps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recipe_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        textDescription = findViewById(R.id.textDescription);
        textMeta = findViewById(R.id.textMeta);
        textIngredients = findViewById(R.id.textIngredients);
        textSteps = findViewById(R.id.textSteps);

        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1L);
        boolean ignoreExpired = AppPrefs.ignoreExpired(this);
        AppDatabase db = AppDatabase.getInstance(this);

        AppDatabase.databaseExecutor.execute(() -> {
            RecipeWithIngredients recipe = db.recipeDao().getById(recipeId);
            List<PantryItem> pantry = db.pantryItemDao().getAllNow();
            runOnUiThread(() -> {
                if (recipe == null) {
                    finish();
                    return;
                }
                show(recipe, IngredientMatcher.buildStock(pantry, ignoreExpired));
            });
        });
    }

    private void show(RecipeWithIngredients data, Map<String, Double> stock) {
        setTitle(data.recipe.getName());
        textDescription.setText(data.recipe.getDescription());
        textMeta.setText(getString(R.string.recipe_info,
                data.recipe.getPrepMinutes(), data.ingredients.size()));

        StringBuilder ingredients = new StringBuilder();
        for (RecipeIngredient ingredient : data.ingredients) {
            boolean enough = IngredientMatcher.hasEnough(stock, ingredient);
            ingredients.append(enough ? "âœ”  " : "âœ˜  ")   // tick or cross
                    .append(AppUtils.formatQuantity(ingredient.getQuantity()))
                    .append(' ').append(ingredient.getUnit())
                    .append(' ').append(ingredient.getName())
                    .append('\n');
        }
        textIngredients.setText(ingredients.toString().trim());

        StringBuilder steps = new StringBuilder();
        String[] lines = data.recipe.getSteps().split("\n");
        for (int i = 0; i < lines.length; i++) {
            steps.append(i + 1).append(". ").append(lines[i]).append("\n\n");
        }
        textSteps.setText(steps.toString().trim());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
