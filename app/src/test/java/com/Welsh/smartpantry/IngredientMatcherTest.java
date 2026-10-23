package com.Welsh.smartpantry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.Welsh.smartpantry.data.PantryItem;
import com.Welsh.smartpantry.data.Recipe;
import com.Welsh.smartpantry.data.RecipeIngredient;
import com.Welsh.smartpantry.data.RecipeWithIngredients;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Local unit tests for the strict matching rule.
 * Run with right-click on this file, then Run 'IngredientMatcherTest'.
 */
public class IngredientMatcherTest {

    private static RecipeWithIngredients recipe(RecipeIngredient... ingredients) {
        RecipeWithIngredients r = new RecipeWithIngredients();
        r.recipe = new Recipe("Test", "Test recipe", 5, "Step");
        r.ingredients = new ArrayList<>(Arrays.asList(ingredients));
        return r;
    }

    private static RecipeIngredient need(String name, double qty, String unit) {
        return new RecipeIngredient(1, name, qty, unit);
    }

    private static PantryItem have(String name, double qty, String unit) {
        return new PantryItem(name, qty, unit, null);
    }

    private static IngredientMatcher.MatchResult run(RecipeWithIngredients r, PantryItem... pantry) {
        return IngredientMatcher.matchAll(Collections.singletonList(r),
                Arrays.asList(pantry), false).get(0);
    }

    @Test
    public void normalise_handlesPluralsCaseAndSpaces() {
        assertEquals("tomato", IngredientMatcher.normalise("  Tomatoes "));
        assertEquals("potato", IngredientMatcher.normalise("POTATOES"));
        assertEquals("egg", IngredientMatcher.normalise("eggs"));
        assertEquals("baked bean", IngredientMatcher.normalise("Baked   Beans"));
        assertEquals("maize meal", IngredientMatcher.normalise("Mealie meal"));
    }

    @Test
    public void allIngredientsInEnoughQuantity_isSuggested() {
        RecipeWithIngredients r = recipe(need("egg", 3, "pcs"), need("milk", 50, "ml"));
        assertTrue(run(r, have("Eggs", 6, "pcs"), have("Milk", 1, "l")).canCook());
    }

    @Test
    public void notEnoughQuantity_isNotSuggested() {
        RecipeWithIngredients r = recipe(need("egg", 3, "pcs"));
        IngredientMatcher.MatchResult result = run(r, have("eggs", 2, "pcs"));
        assertFalse(result.canCook());
        assertEquals("1 pcs egg", result.missing.get(0));
    }

    @Test
    public void missingIngredient_isNotSuggested() {
        RecipeWithIngredients r = recipe(need("egg", 3, "pcs"), need("butter", 10, "g"));
        assertFalse(run(r, have("egg", 12, "pcs")).canCook());
    }

    @Test
    public void unitsAreConverted() {
        RecipeWithIngredients r = recipe(need("flour", 500, "g"));
        assertTrue(run(r, have("Flour", 1, "kg")).canCook());
        assertFalse(run(r, have("Flour", 0.4, "kg")).canCook());
    }

    @Test
    public void incompatibleUnits_doNotCount() {
        // 3 pcs of tomato cannot satisfy a requirement measured in grams
        RecipeWithIngredients r = recipe(need("tomato", 200, "g"));
        assertFalse(run(r, have("tomatoes", 3, "pcs")).canCook());
    }

    @Test
    public void duplicatePantryEntries_areAddedTogether() {
        RecipeWithIngredients r = recipe(need("egg", 3, "pcs"));
        assertTrue(run(r, have("egg", 2, "pcs"), have("Eggs", 1, "pcs")).canCook());
    }

    @Test
    public void oneIngredientShort_isAlmostThere() {
        RecipeWithIngredients r = recipe(need("egg", 2, "pcs"), need("bread", 2, "pcs"));
        IngredientMatcher.MatchResult result = run(r, have("egg", 2, "pcs"));
        assertFalse(result.canCook());
        assertTrue(result.isAlmostThere());
    }

    @Test
    public void emptyPantry_matchesNothing() {
        RecipeWithIngredients r = recipe(need("egg", 1, "pcs"));
        List<IngredientMatcher.MatchResult> results = IngredientMatcher.matchAll(
                Collections.singletonList(r), new ArrayList<>(), false);
        assertFalse(results.get(0).canCook());
    }
}
