package com.Welsh.smartpantry.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * One ingredient the user currently has at home.
 * Room maps this class to the "pantry_items" table, one object per row.
 */
@Entity(tableName = "pantry_items")
public class PantryItem {

    // autoGenerate lets SQLite assign the next id when a new item is inserted
    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private String name;

    private double quantity;

    // One of the units in UnitConverter, e.g. "g", "kg", "ml", "l", "pcs"
    @NonNull
    private String unit;

    // Optional, stored as text in the format yyyy-MM-dd, null when left blank
    @Nullable
    @ColumnInfo(name = "expiry_date")
    private String expiryDate;

    public PantryItem(@NonNull String name, double quantity,
                      @NonNull String unit, @Nullable String expiryDate) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    @NonNull
    public String getName() { return name; }
    public void setName(@NonNull String name) { this.name = name; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    @NonNull
    public String getUnit() { return unit; }
    public void setUnit(@NonNull String unit) { this.unit = unit; }

    @Nullable
    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(@Nullable String expiryDate) { this.expiryDate = expiryDate; }
}
