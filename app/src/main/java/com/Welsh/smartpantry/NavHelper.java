package com.Welsh.smartpantry;

import android.app.Activity;
import android.content.Intent;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Wires the bottom navigation bar that appears on the three main screens
 * (Pantry, Recipes, Settings). Each tab starts its Activity with an
 * explicit Intent. FLAG_ACTIVITY_REORDER_TO_FRONT brings an already open
 * screen back to the front instead of creating a second copy of it.
 */
public final class NavHelper {

    private NavHelper() { }

    public static void setup(Activity activity, BottomNavigationView nav, int currentItemId) {
        nav.setSelectedItemId(currentItemId);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == currentItemId) {
                return true;            // already on this screen
            }
            Class<?> target;
            if (id == R.id.nav_pantry) {
                target = MainActivity.class;
            } else if (id == R.id.nav_recipes) {
                target = SuggestedRecipesActivity.class;
            } else {
                target = SettingsActivity.class;
            }
            Intent intent = new Intent(activity, target);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            activity.startActivity(intent);
            activity.overridePendingTransition(0, 0);   // switch tabs without animation
            return true;
        });
    }
}
