package com.Welsh.smartpantry;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.Welsh.smartpantry.data.AppDatabase;

/**
 * Settings screen. Each switch is saved straight to SharedPreferences
 * (AppPrefs), and the other screens read it in onResume.
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.title_settings);

        bindSwitch(R.id.switchHighlight, AppPrefs.KEY_HIGHLIGHT_EXPIRING,
                AppPrefs.highlightExpiring(this));
        bindSwitch(R.id.switchAlmost, AppPrefs.KEY_SHOW_ALMOST_THERE,
                AppPrefs.showAlmostThere(this));
        bindSwitch(R.id.switchIgnoreExpired, AppPrefs.KEY_IGNORE_EXPIRED,
                AppPrefs.ignoreExpired(this));

        // Show how many recipes are stored, read from the database
        TextView textAbout = findViewById(R.id.textAbout);
        AppDatabase db = AppDatabase.getInstance(this);
        AppDatabase.databaseExecutor.execute(() -> {
            int count = db.recipeDao().count();
            runOnUiThread(() -> textAbout.setText(getString(R.string.about_text, count)));
        });

        BottomNavigationView nav = findViewById(R.id.bottomNav);
        NavHelper.setup(this, nav, R.id.nav_settings);
    }

    private void bindSwitch(int viewId, String key, boolean current) {
        MaterialSwitch toggle = findViewById(viewId);
        toggle.setChecked(current);
        toggle.setOnCheckedChangeListener((button, checked) ->
                AppPrefs.setBoolean(this, key, checked));
    }
}
