package com.welsh.smartpantry.data;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.Arrays;
import java.util.List;

/**
 * Loads the built in recipe collection (20 recipes) into the database the
 * first time it is created. Called from AppDatabase's onCreate callback.
 *
 * Ingredient names are written in singular form and every quantity uses a
 * base unit (g, ml or pcs) so the matching engine can compare them directly.
 */
final class RecipeSeeder {

    private RecipeSeeder() { }

    /** Inserts every recipe row, then its ingredient rows linked by recipe_id. */
    static void seed(SupportSQLiteDatabase db) {
        for (SeedRecipe r : recipes()) {
            ContentValues recipeRow = new ContentValues();
            recipeRow.put("name", r.name);
            recipeRow.put("description", r.description);
            recipeRow.put("prep_minutes", r.prepMinutes);
            recipeRow.put("steps", r.steps);
            long recipeId = db.insert("recipes", SQLiteDatabase.CONFLICT_ABORT, recipeRow);

            for (SeedIngredient i : r.ingredients) {
                ContentValues ingredientRow = new ContentValues();
                ingredientRow.put("recipe_id", recipeId);
                ingredientRow.put("name", i.name);
                ingredientRow.put("quantity", i.quantity);
                ingredientRow.put("unit", i.unit);
                db.insert("recipe_ingredients", SQLiteDatabase.CONFLICT_ABORT, ingredientRow);
            }
        }
    }

    // ---------- the recipe collection ----------

    static List<SeedRecipe> recipes() {
        return Arrays.asList(
            recipe("Scrambled Eggs", "Soft, creamy eggs in five minutes.", 5,
                "Whisk the eggs and milk together.\n"
                + "Melt the butter in a pan over low heat.\n"
                + "Add the eggs and stir slowly until just set.",
                ing("egg", 3, "pcs"), ing("milk", 50, "ml"), ing("butter", 10, "g")),

            recipe("Boiled Eggs on Toast", "A simple, filling breakfast.", 12,
                "Boil the eggs for 8 minutes, then cool in cold water.\n"
                + "Toast the bread and spread with butter.\n"
                + "Peel and slice the eggs onto the toast.",
                ing("egg", 2, "pcs"), ing("bread", 2, "pcs"), ing("butter", 10, "g")),

            recipe("Cheese Omelette", "Fluffy omelette folded over melted cheese.", 8,
                "Beat the eggs.\n"
                + "Melt the butter in a pan and pour in the eggs.\n"
                + "When almost set, sprinkle over the cheese and fold in half.",
                ing("egg", 3, "pcs"), ing("cheese", 50, "g"), ing("butter", 10, "g")),

            recipe("Tomato and Onion Omelette", "A savoury omelette with fresh vegetables.", 12,
                "Dice the tomato and onion.\n"
                + "Fry the onion in the oil until soft, then add the tomato.\n"
                + "Pour in the beaten eggs and cook until set.",
                ing("egg", 3, "pcs"), ing("tomato", 1, "pcs"), ing("onion", 1, "pcs"),
                ing("oil", 15, "ml")),

            recipe("French Toast", "Golden egg soaked bread, lightly sweet.", 15,
                "Whisk the eggs, milk and sugar in a bowl.\n"
                + "Dip each slice of bread into the mixture.\n"
                + "Fry in butter until golden on both sides.",
                ing("bread", 4, "pcs"), ing("egg", 2, "pcs"), ing("milk", 100, "ml"),
                ing("butter", 20, "g"), ing("sugar", 10, "g")),

            recipe("Pancakes", "Classic thin pancakes.", 25,
                "Mix the flour and sugar in a bowl.\n"
                + "Whisk in the eggs and milk until smooth.\n"
                + "Cook thin layers of batter in a buttered pan, turning once.",
                ing("flour", 200, "g"), ing("milk", 300, "ml"), ing("egg", 2, "pcs"),
                ing("sugar", 25, "g"), ing("butter", 20, "g")),

            recipe("Grilled Cheese Sandwich", "Crispy outside, melted inside.", 10,
                "Butter one side of each slice of bread.\n"
                + "Place the cheese between the slices, butter facing out.\n"
                + "Fry on medium heat until golden and the cheese melts.",
                ing("bread", 2, "pcs"), ing("cheese", 60, "g"), ing("butter", 20, "g")),

            recipe("Tomato Pasta", "A quick fresh tomato sauce over pasta.", 25,
                "Boil the pasta in salted water until cooked.\n"
                + "Fry the chopped onion and garlic in the oil.\n"
                + "Add the chopped tomatoes and simmer for 10 minutes.\n"
                + "Mix the sauce through the drained pasta.",
                ing("pasta", 250, "g"), ing("tomato", 4, "pcs"), ing("onion", 1, "pcs"),
                ing("garlic", 2, "pcs"), ing("oil", 30, "ml")),

            recipe("Garlic Butter Pasta", "Pasta tossed in garlic butter and cheese.", 20,
                "Boil the pasta until cooked.\n"
                + "Gently fry the crushed garlic in the butter.\n"
                + "Toss the drained pasta in the garlic butter and top with grated cheese.",
                ing("pasta", 250, "g"), ing("garlic", 4, "pcs"), ing("butter", 50, "g"),
                ing("cheese", 40, "g")),

            recipe("Spaghetti Bolognese", "Rich mince and tomato sauce.", 45,
                "Fry the onion and garlic until soft.\n"
                + "Add the mince and brown it well.\n"
                + "Add the chopped tomatoes and simmer for 25 minutes.\n"
                + "Serve over the cooked pasta.",
                ing("pasta", 250, "g"), ing("beef mince", 500, "g"), ing("tomato", 4, "pcs"),
                ing("onion", 1, "pcs"), ing("garlic", 2, "pcs")),

            recipe("Tuna Pasta Salad", "A cold salad for lunch boxes.", 20,
                "Cook the pasta, drain and let it cool.\n"
                + "Mix in the drained tuna, chopped onion and mayonnaise.\n"
                + "Chill before serving.",
                ing("pasta", 200, "g"), ing("canned tuna", 170, "g"), ing("mayonnaise", 60, "ml"),
                ing("onion", 1, "pcs")),

            recipe("Egg Fried Rice", "Uses up leftover rice and vegetables.", 20,
                "Cook the rice and let it cool if it is not already cooked.\n"
                + "Fry the diced onion and carrot in the oil.\n"
                + "Add the rice and stir fry for 3 minutes.\n"
                + "Push the rice aside, scramble the eggs, then mix everything together.",
                ing("rice", 300, "g"), ing("egg", 2, "pcs"), ing("onion", 1, "pcs"),
                ing("carrot", 1, "pcs"), ing("oil", 30, "ml")),

            recipe("Chicken and Rice", "A one pan family meal.", 40,
                "Cut the chicken into pieces and brown in the oil.\n"
                + "Add the chopped onion and cook until soft.\n"
                + "Add the rice and 500 ml of water, cover and simmer for 20 minutes.",
                ing("chicken breast", 2, "pcs"), ing("rice", 250, "g"), ing("onion", 1, "pcs"),
                ing("oil", 30, "ml")),

            recipe("Chakalaka", "A spicy South African vegetable relish.", 30,
                "Fry the chopped onion and green pepper in the oil.\n"
                + "Add the grated carrots and chopped tomatoes and cook for 10 minutes.\n"
                + "Stir in the baked beans and simmer for 5 minutes.",
                ing("onion", 1, "pcs"), ing("green pepper", 1, "pcs"), ing("carrot", 2, "pcs"),
                ing("tomato", 2, "pcs"), ing("baked bean", 410, "g"), ing("oil", 30, "ml")),

            recipe("Pap and Tomato Gravy", "Stiff pap with a simple tomato and onion gravy.", 35,
                "Bring 750 ml of water to the boil and slowly stir in the maize meal.\n"
                + "Cover and cook on low heat for 20 minutes, stirring now and then.\n"
                + "For the gravy, fry the onion in the oil, add the tomatoes and simmer.",
                ing("maize meal", 250, "g"), ing("tomato", 3, "pcs"), ing("onion", 1, "pcs"),
                ing("oil", 15, "ml")),

            recipe("Potato Wedges", "Crispy oven baked wedges.", 40,
                "Cut the potatoes into wedges.\n"
                + "Toss them in the oil.\n"
                + "Bake at 200 degrees for 35 minutes, turning halfway.",
                ing("potato", 4, "pcs"), ing("oil", 30, "ml")),

            recipe("Mashed Potatoes", "Smooth and buttery.", 30,
                "Peel and boil the potatoes until soft.\n"
                + "Drain, then mash with the butter.\n"
                + "Beat in the warm milk until smooth.",
                ing("potato", 5, "pcs"), ing("butter", 40, "g"), ing("milk", 100, "ml")),

            recipe("Vegetable Soup", "A warming soup from basic vegetables.", 40,
                "Chop all the vegetables.\n"
                + "Place them in a pot with 1 litre of water.\n"
                + "Simmer for 30 minutes, then blend or serve chunky.",
                ing("carrot", 2, "pcs"), ing("potato", 2, "pcs"), ing("onion", 1, "pcs"),
                ing("tomato", 2, "pcs")),

            recipe("Banana Smoothie", "A quick breakfast drink.", 5,
                "Peel the bananas.\n"
                + "Blend the bananas, milk and honey until smooth.",
                ing("banana", 2, "pcs"), ing("milk", 300, "ml"), ing("honey", 15, "ml")),

            recipe("Rice Pudding", "Slow cooked creamy dessert.", 45,
                "Place the rice, milk and sugar in a pot.\n"
                + "Simmer on low heat for 40 minutes, stirring often, until thick.",
                ing("rice", 150, "g"), ing("milk", 750, "ml"), ing("sugar", 60, "g"))
        );
    }

    // ---------- small helper types so the list above stays readable ----------

    static final class SeedRecipe {
        final String name, description, steps;
        final int prepMinutes;
        final SeedIngredient[] ingredients;

        SeedRecipe(String name, String description, int prepMinutes,
                   String steps, SeedIngredient[] ingredients) {
            this.name = name;
            this.description = description;
            this.prepMinutes = prepMinutes;
            this.steps = steps;
            this.ingredients = ingredients;
        }
    }

    static final class SeedIngredient {
        final String name, unit;
        final double quantity;

        SeedIngredient(String name, double quantity, String unit) {
            this.name = name;
            this.quantity = quantity;
            this.unit = unit;
        }
    }

    private static SeedRecipe recipe(String name, String description, int minutes,
                                     String steps, SeedIngredient... ingredients) {
        return new SeedRecipe(name, description, minutes, steps, ingredients);
    }

    private static SeedIngredient ing(String name, double quantity, String unit) {
        return new SeedIngredient(name, quantity, unit);
    }
}
