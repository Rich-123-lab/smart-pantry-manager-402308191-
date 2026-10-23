package com.Welsh.smartpantry;

import java.util.Locale;

/**
 * Converts every supported unit to one of three base units so quantities
 * can be compared fairly: mass in grams (g), volume in millilitres (ml)
 * and countable items in pieces (pcs). Example: 1 kg becomes 1000 g, so a
 * pantry holding 1 kg of flour satisfies a recipe that needs 500 g.
 */
public final class UnitConverter {

    public static final String GRAMS = "g";
    public static final String MILLILITRES = "ml";
    public static final String PIECES = "pcs";

    private UnitConverter() { }

    /** The base unit a unit converts to: "g", "ml" or "pcs". */
    public static String baseUnit(String unit) {
        switch (clean(unit)) {
            case "g":
            case "kg":
                return GRAMS;
            case "ml":
            case "l":
            case "tsp":
            case "tbsp":
            case "cup":
                return MILLILITRES;
            default:
                return PIECES;
        }
    }

    /** The quantity expressed in the base unit, e.g. (2, "kg") gives 2000. */
    public static double toBase(double quantity, String unit) {
        switch (clean(unit)) {
            case "kg":
            case "l":
                return quantity * 1000;
            case "tsp":
                return quantity * 5;
            case "tbsp":
                return quantity * 15;
            case "cup":
                return quantity * 250;
            default:          // g, ml and pcs are already base units
                return quantity;
        }
    }

    private static String clean(String unit) {
        return unit == null ? "" : unit.trim().toLowerCase(Locale.US);
    }
}
