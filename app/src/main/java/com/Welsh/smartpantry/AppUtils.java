package com.welsh.smartpantry;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Small formatting and date helpers shared by several screens.
 * Uses Calendar and SimpleDateFormat (not java.time) because the app
 * supports Android 7.0 (API 24), where java.time is not available.
 */
public final class AppUtils {

    public static final String DATE_PATTERN = "yyyy-MM-dd";

    private AppUtils() { }

    /** 3.0 becomes "3", 1.5 becomes "1.5", always with a dot as decimal sign. */
    public static String formatQuantity(double quantity) {
        DecimalFormat format = new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.US));
        return format.format(quantity);
    }

    /** Builds yyyy-MM-dd from a DatePicker result (month is 0 based there). */
    public static String formatDate(int year, int monthZeroBased, int day) {
        return String.format(Locale.US, "%04d-%02d-%02d", year, monthZeroBased + 1, day);
    }

    /** Parses yyyy-MM-dd, returning null if the text is not a valid date. */
    public static Date parseDate(String text) {
        SimpleDateFormat format = new SimpleDateFormat(DATE_PATTERN, Locale.US);
        format.setLenient(false);
        try {
            return format.parse(text);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Whole days from today until the given date: 0 means today,
     * negative means it has already expired. Returns Long.MAX_VALUE
     * if the date cannot be read, so it is never flagged by mistake.
     */
    public static long daysUntil(String date) {
        Date target = parseDate(date);
        if (target == null) {
            return Long.MAX_VALUE;
        }
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        long millisPerDay = 24L * 60 * 60 * 1000;
        return Math.round((target.getTime() - today.getTimeInMillis()) / (double) millisPerDay);
    }
}
