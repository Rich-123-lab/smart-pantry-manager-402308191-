package com.Welsh.smartpantry;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.Welsh.smartpantry.data.AppDatabase;
import com.Welsh.smartpantry.data.PantryItem;
import com.Welsh.smartpantry.data.PantryItemDao;

import java.util.Calendar;
import java.util.Date;

/**
 * One screen used for both adding and editing a pantry item.
 * If the Intent that started it carries EXTRA_ITEM_ID, it loads that item
 * and works in edit mode (with a Delete button); otherwise it adds a new item.
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    /** Intent extra key: the id of the PantryItem to edit. */
    public static final String EXTRA_ITEM_ID = "com.Welsh.smartpantry.EXTRA_ITEM_ID";
    private static final long NO_ID = -1L;

    private static final int MAX_NAME_LENGTH = 40;
    private static final double MAX_QUANTITY = 100000;

    private TextInputLayout layoutName;
    private TextInputLayout layoutQuantity;
    private TextInputEditText editName;
    private TextInputEditText editQuantity;
    private TextInputEditText editExpiry;
    private Spinner spinnerUnit;

    private PantryItemDao dao;
    private String[] units;

    private PantryItem existingItem;   // null while adding a new item
    private String selectedExpiry;     // yyyy-MM-dd, or null for no expiry

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_edit_ingredient);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);   // back arrow
        }

        layoutName = findViewById(R.id.layoutName);
        layoutQuantity = findViewById(R.id.layoutQuantity);
        editName = findViewById(R.id.editName);
        editQuantity = findViewById(R.id.editQuantity);
        editExpiry = findViewById(R.id.editExpiry);
        spinnerUnit = findViewById(R.id.spinnerUnit);
        MaterialButton buttonClearExpiry = findViewById(R.id.buttonClearExpiry);
        MaterialButton buttonSave = findViewById(R.id.buttonSave);
        MaterialButton buttonDelete = findViewById(R.id.buttonDelete);

        // Fill the unit dropdown from res/values/arrays.xml
        units = getResources().getStringArray(R.array.units);
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, units);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(unitAdapter);

        editExpiry.setOnClickListener(v -> showDatePicker());
        buttonClearExpiry.setOnClickListener(v -> {
            selectedExpiry = null;
            editExpiry.setText("");
        });
        buttonSave.setOnClickListener(v -> saveItem());
        buttonDelete.setOnClickListener(v -> confirmDelete());

        dao = AppDatabase.getInstance(this).pantryItemDao();

        // Read the data passed in by the Intent to decide add mode or edit mode
        long itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, NO_ID);
        if (itemId == NO_ID) {
            setTitle(R.string.title_add_ingredient);
        } else {
            setTitle(R.string.title_edit_ingredient);
            buttonDelete.setVisibility(View.VISIBLE);
            loadItem(itemId);
        }
    }

    /** Reads the item on a background thread, then fills the form on the UI thread. */
    private void loadItem(long id) {
        AppDatabase.databaseExecutor.execute(() -> {
            PantryItem item = dao.getById(id);
            runOnUiThread(() -> {
                if (item == null) {      // item was deleted elsewhere
                    finish();
                    return;
                }
                existingItem = item;
                editName.setText(item.getName());
                editQuantity.setText(AppUtils.formatQuantity(item.getQuantity()));
                spinnerUnit.setSelection(indexOfUnit(item.getUnit()));
                selectedExpiry = item.getExpiryDate();
                editExpiry.setText(selectedExpiry == null ? "" : selectedExpiry);
            });
        });
    }

    private int indexOfUnit(String unit) {
        for (int i = 0; i < units.length; i++) {
            if (units[i].equals(unit)) {
                return i;
            }
        }
        return 0;
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        if (selectedExpiry != null) {
            Date current = AppUtils.parseDate(selectedExpiry);
            if (current != null) {
                calendar.setTime(current);
            }
        }
        new DatePickerDialog(this, (view, year, month, day) -> {
            selectedExpiry = AppUtils.formatDate(year, month, day);
            editExpiry.setText(selectedExpiry);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    /** Validates the form; only writes to the database if every field is valid. */
    private void saveItem() {
        layoutName.setError(null);
        layoutQuantity.setError(null);
        boolean valid = true;

        // Name: required, must contain a letter, sensible length
        String name = textOf(editName).trim().replaceAll("\\s+", " ");
        if (name.isEmpty()) {
            layoutName.setError(getString(R.string.error_name_required));
            valid = false;
        } else if (!name.matches(".*[A-Za-z].*")) {
            layoutName.setError(getString(R.string.error_name_letters));
            valid = false;
        } else if (name.length() > MAX_NAME_LENGTH) {
            layoutName.setError(getString(R.string.error_name_too_long));
            valid = false;
        }

        // Quantity: required, a real number above zero, not absurdly large.
        // A comma is accepted as decimal sign because many SA keyboards use it.
        String quantityText = textOf(editQuantity).trim().replace(',', '.');
        double quantity = 0;
        if (quantityText.isEmpty()) {
            layoutQuantity.setError(getString(R.string.error_quantity_required));
            valid = false;
        } else {
            try {
                quantity = Double.parseDouble(quantityText);
                if (Double.isNaN(quantity) || quantity <= 0) {
                    layoutQuantity.setError(getString(R.string.error_quantity_invalid));
                    valid = false;
                } else if (quantity > MAX_QUANTITY) {
                    layoutQuantity.setError(getString(R.string.error_quantity_too_big));
                    valid = false;
                }
            } catch (NumberFormatException e) {
                layoutQuantity.setError(getString(R.string.error_quantity_invalid));
                valid = false;
            }
        }

        if (!valid) {
            return;
        }

        String unit = units[spinnerUnit.getSelectedItemPosition()];

        if (existingItem == null) {
            // CREATE
            PantryItem newItem = new PantryItem(name, quantity, unit, selectedExpiry);
            AppDatabase.databaseExecutor.execute(() -> {
                dao.insert(newItem);
                runOnUiThread(() -> finishWithToast(R.string.toast_saved));
            });
        } else {
            // UPDATE
            existingItem.setName(name);
            existingItem.setQuantity(quantity);
            existingItem.setUnit(unit);
            existingItem.setExpiryDate(selectedExpiry);
            AppDatabase.databaseExecutor.execute(() -> {
                dao.update(existingItem);
                runOnUiThread(() -> finishWithToast(R.string.toast_updated));
            });
        }
    }

    /** DELETE, after the user confirms in a dialog. */
    private void confirmDelete() {
        if (existingItem == null) {
            return;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(getString(R.string.dialog_delete_message, existingItem.getName()))
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete, (dialog, which) ->
                        AppDatabase.databaseExecutor.execute(() -> {
                            dao.delete(existingItem);
                            runOnUiThread(() -> finishWithToast(R.string.toast_deleted));
                        }))
                .show();
    }

    private void finishWithToast(int messageRes) {
        Toast.makeText(this, messageRes, Toast.LENGTH_SHORT).show();
        finish();   // back to the list, which refreshes itself through LiveData
    }

    private static String textOf(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
