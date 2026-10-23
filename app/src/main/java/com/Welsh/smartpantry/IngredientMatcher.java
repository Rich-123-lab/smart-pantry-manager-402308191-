package com.Welsh.smartpantry;

import com.Welsh.smartpantry.data.PantryItem;
import com.Welsh.smartpantry.data.RecipeIngredient;
import com.Welsh.smartpantry.data.RecipeWithIngredients;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The recipe matching engine.
 *
 * STRICT RULE: a recipe is suggested only if EVERY required ingredient is in
 * the pantry in AT LEAST the required quantity. Before comparing, names are
 * normalised (so "Tomatoes" matches "tomato") and quantities are converted
 * to base units (so 1 kg matches 500 g).
 *
 * This class has no Android code in it, so it is tested with plain JUnit
 * tests (see IngredientMatcherTest).
 */
public final class IngredientMatcher {

    // Allows for rounding, e.g. 0.1 + 0.2 is 0.30000000000000004 in Java
    private static final double EPSILON = 0.0001;

    // Different words people type for the same ingredient
    private static final Map<String, String> SYNONYMS = new HashMap<>();

    static {
        SYNONYMS.put("mince", "beef mince");
        SYNONYMS.put("minced beef", "beef mince");
        SYNONYMS.put("ground beef", "beef mince");
        SYNONYMS.put("tuna", "canned tuna");
        SYNONYMS.put("tinned tuna", "canned tuna");
        SYNONYMS.put("mealie meal", "maize meal");
        SYNONYMS.put("spaghetti", "pasta");
        SYNONYMS.put("macaroni", "pasta");
        SYNONYMS.put("penne", "pasta");
        SYNONYMS.put("cooking oil", "oil");
        SYNONYMS.put("sunflower oil", "oil");
        SYNONYMS.put("vegetable oil", "oil");
        SYNONYMS.put("olive oil", "oil");
        SYNONYMS.put("cheddar", "cheese");
        SYNONYMS.put("cheddar cheese", "cheese");
        SYNONYMS.put("mayo", "mayonnaise");
        SYNONYMS.put("garlic clove", "garlic");
        SYNONYMS.put("chicken", "chicken breast");
        SYNONYMS.put("white sugar", "sugar");
        SYNONYMS.put("brown sugar", "sugar");
        SYNONYMS.put("cake flour", "flour");
        SYNONYMS.put("bread slice", "bread");
        SYNONYMS.put("slice of bread", "bread");
        SYNONYMS.put("capsicum", "green pepper");
    }

    private IngredientMatcher() { }

    // ------------------------------------------------------------------
    // Name normalisation
    // ------------------------------------------------------------------

    /**
     * Lower case, trimmed, single spaced, singular form, synonyms resolved.
     * "  Tomatoes " becomes "tomato", "Baked Beans" becomes "baked bean",
     * "Mealie Meal" becomes "maize meal".
     */
    public static String normalise(String name) {
        if (name == null) {
            return "";
        }
        String text = name.toLowerCase(Locale.US)
                .replaceAll("[^a-z ]", " ")     // drop digits and punctuation
                .trim()
                .replaceAll("\\s+", " ");
        if (text.isEmpty()) {
            return "";
        }
        // Only the last word is made singular: "baked beans" -> "baked bean"
        int lastSpace = text.lastIndexOf(' ');
        String start = text.substring(0, lastSpace + 1);
        String last = singular(text.substring(lastSpace + 1));
        String result = start + last;

        String synonym = SYNONYMS.get(result);
        return synonym != null ? synonym : result;
    }

    /** Simple English plural rules that cover common food names. */
    static String singular(String word) {
        if (word.length() <= 3) {
            return word;                                         // "egg", "oil"
        }
        if (word.endsWith("ies")) {
            return word.substring(0, word.length() - 3) + "y";   // berries -> berry
        }
        if (word.endsWith("oes")) {
            return word.substring(0, word.length() - 2);         // tomatoes -> tomato
        }
        if (word.endsWith("ches") || word.endsWith("shes")) {
            return word.substring(0, word.length() - 2);         // peaches -> peach
        }
        if (word.endsWith("ss") || word.endsWith("us")) {
            return word;                                         // grass, hummus
        }
        if (word.endsWith("s")) {
            return word.substring(0, word.length() - 1);         // eggs -> egg
        }
        return word;
    }

    // ------------------------------------------------------------------
    // Matching
    // ------------------------------------------------------------------

    /**
     * Adds up what the pantry holds, per normalised name and base unit.
     * Two entries such as "Eggs 6 pcs" and "egg 2 pcs" combine into 8 pcs.
     * Key format: "name|unit", e.g. "flour|g".
     */
    public static Map<String, Double> buildStock(List<PantryItem> pantry, boolean ignoreExpired) {
        Map<String, Double> stock = new HashMap<>();
        for (PantryItem item : pantry) {
            if (ignoreExpired && item.getExpiryDate() != null
                    && AppUtils.daysUntil(item.getExpiryDate()) < 0) {
                continue;
            }
            String key = key(item.getName(), item.getUnit());
            double amount = UnitConverter.toBase(item.getQuantity(), item.getUnit());
            Double current = stock.get(key);
            stock.put(key, current == null ? amount : current + amount);
        }
        return stock;
    }

    /** How much of this ingredient the pantry has, in base units (0 if none). */
    public static double available(Map<String, Double> stock, RecipeIngredient ingredient) {
        Double amount = stock.get(key(ingredient.getName(), ingredient.getUnit()));
        return amount == null ? 0 : amount;
    }

    /** True if the pantry holds at least the required amount of this ingredient. */
    public static boolean hasEnough(Map<String, Double> stock, RecipeIngredient ingredient) {
        double required = UnitConverter.toBase(ingredient.getQuantity(), ingredient.getUnit());
        return available(stock, ingredient) + EPSILON >= required;
    }

    /** Checks one recipe against the pantry and lists what is short. */
    public static MatchResult match(RecipeWithIngredients recipe, Map<String, Double> stock) {
        List<String> missing = new ArrayList<>();
        for (RecipeIngredient ingredient : recipe.ingredients) {
            if (!hasEnough(stock, ingredient)) {
                double required = UnitConverter.toBase(ingredient.getQuantity(), ingredient.getUnit());
                double shortBy = required - available(stock, ingredient);
                missing.add(AppUtils.formatQuantity(shortBy) + " "
                        + UnitConverter.baseUnit(ingredient.getUnit()) + " " + ingredient.getName());
            }
        }
        return new MatchResult(recipe, missing);
    }

    /** Matches every recipe and returns the full list of results. */
    public static List<MatchResult> matchAll(List<RecipeWithIngredients> recipes,
                                             List<PantryItem> pantry, boolean ignoreExpired) {
        Map<String, Double> stock = buildStock(pantry, ignoreExpired);
        List<MatchResult> results = new ArrayList<>();
        for (RecipeWithIngredients recipe : recipes) {
            results.add(match(recipe, stock));
        }
        return results;
    }

    private static String key(String name, String unit) {
        return normalise(name) + "|" + UnitConverter.baseUnit(unit);
    }

    // ------------------------------------------------------------------

    /** The outcome of checking one recipe. */
    public static final class MatchResult {
        public final RecipeWithIngredients recipe;
        public final List<String> missing;   // e.g. "50 ml milk"; empty if cookable

        MatchResult(RecipeWithIngredients recipe, List<String> missing) {
            this.recipe = recipe;
            this.missing = missing;
        }

        /** Strict rule: every ingredient present in a sufficient quantity. */
        public boolean canCook() {
            return missing.isEmpty();
        }

        /** Bonus "Almost There": exactly one ingredient short. */
        public boolean isAlmostThere() {
            return missing.size() == 1;
        }
    }
}
