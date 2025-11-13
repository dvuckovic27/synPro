package com.metalac.scanner.app.helpers;

import androidx.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class for formatting and parsing dates using a fixed pattern.
 */
public class DateHelper {

    private static final String FORMAT_PATTERN = "dd.MM.yyyy - HH:mm";
    private static final String EXP_DATE_FORMAT_PATTERN = "dd.MM.yyyy";

    /**
     * Formats the given {@link Date} object into a string using the default format pattern.
     *
     * @param date the {@link Date} to be formatted
     * @return a formatted date string using {@code FORMAT_PATTERN},
     * or an empty string if the date is {@code null}
     */
    public static String formatDateToString(Date date) {
        return formatDateToString(date, FORMAT_PATTERN);
    }

    /**
     * Formats the given {@link Date} object into a string using the specified format pattern.
     *
     * @param date   the {@link Date} to be formatted
     * @param format the desired date format pattern (e.g., "dd.MM.yyyy")
     * @return a formatted date string using the provided pattern,
     * or an empty string if the date is {@code null}
     */
    public static String formatDateToString(Date date, String format) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.getDefault());
        return formatter.format(date);
    }

    /**
     * Formats the given {@link Date} object into a string using the expiration date format pattern.
     *
     * @param date the {@link Date} to be formatted
     * @return a formatted expiration date string using {@code EXP_DATE_FORMAT_PATTERN},
     * or an empty string if the date is {@code null}
     */
    public static String formatExpDateToString(Date date) {
        return formatDateToString(date, EXP_DATE_FORMAT_PATTERN);
    }

    /**
     * Parses a string representing a date in the expiration date format ("dd.MM.yyyy") into a {@link Date} object.
     *
     * @param currentText the string to parse, expected format is "dd.MM.yyyy" (e.g., "15.07.2025")
     * @return the parsed {@link Date} object, or {@code null} if the input is empty or cannot be parsed
     */
    @Nullable
    public static Date parseExpDateFromString(String currentText) {
        if (currentText == null || currentText.trim().isEmpty()) {
            return null;
        }

        SimpleDateFormat formatter = new SimpleDateFormat(EXP_DATE_FORMAT_PATTERN, Locale.getDefault());
        formatter.setLenient(false);

        try {
            return formatter.parse(currentText.trim());
        } catch (ParseException e) {
            return null;
        }
    }
}
