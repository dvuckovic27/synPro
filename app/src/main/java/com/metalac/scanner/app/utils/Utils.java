package com.metalac.scanner.app.utils;

import android.content.Context;
import android.text.InputFilter;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.metalac.scanner.app.R;
import com.metalac.scanner.app.data.source.PrefManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for constants and helper functions used in the scanner application.
 */
public class Utils {
    private static final String PRICE_FORMAT = "%.2f";
    private static final String CURRENCY_STRING = " RSD";
    private static final String EXPORT_PREF = "POP";
    private static final String EXPORT_DATE_FORMAT = "yyyyMMdd";
    public static final int DEVICE_NAME_NUM_OF_CHAR = 5;
    public static final int STORE_CODE_NUM_OF_CHAR = 9;
    public static final int STORE_CODE_PREFIX_CHAR_COUNT = 3;
    public static final int SHOW_KEYBOARD_DELAY = 150;
    private static final int STORE_CODE_LENGTH = 12;
    public static final int PAGE_SIZE = 30;

    public static final String ITEM_ID = "itemId";
    public static final String MASTER_ID = "masterId";
    public static final String QUANTITY = "quantity";
    public static final String PRODUCT_NAME = "productName";
    public static final String INVENTORY_ITEM_ID = "inventoryItemId";
    public static final String QUERY_MASTER_ITEM = "queryMasterItem";
    public static final String IS_UPDATE_ACTION = "isUpdateAction";

    public static final String EXTRA_INFO_RESULT_KEY = "extra_info_result";
    public static final String EXTRA_INFO_ITEM_ADDED = "item_added";

    /**
     * Extracts the store code from a file name string.
     * <p>
     * Assumes the store code starts at index {@code STORE_CODE_PREFIX_CHAR_COUNT} and is
     * {@code STORE_CODE_NUM_OF_CHAR} characters long.
     *
     * @param fileName The name of the file, should including encoded store info.
     * @return Extracted store code if valid, empty string if the file name is too short,
     * or {@code null} if the input is null.
     */
    @Nullable
    public static String getFileStoreCode(@Nullable String fileName) {
        if (fileName == null) {
            return null;
        }

        if (fileName.length() < STORE_CODE_LENGTH) {
            return "";
        }

        return fileName.substring(STORE_CODE_PREFIX_CHAR_COUNT, STORE_CODE_LENGTH);
    }

    public static String getFormatedPrice(Double price) {
        return String.format(Locale.ENGLISH, PRICE_FORMAT, price) + CURRENCY_STRING;
    }

    public static String getQuantityString(double quantity) {
        if (quantity % 1 == 0) {
            return String.valueOf((long) quantity); // cast to long to remove ".0"
        } else {
            return String.valueOf(quantity);
        }
    }

    /**
     * Returns the trimmed string value from the given {@link EditText}, or {@code null} if the text is empty or null.
     *
     * @param editText The {@link EditText} to extract the text from.
     * @return The trimmed string, or {@code null} if no text is present.
     */
    @Nullable
    public static String getStringOrNull(EditText editText) {
        return editText.getText() != null
                ? editText.getText().toString().trim()
                : null;
    }

    /**
     * Extracts the damage code from a string formatted as "<damage_name> (<damage_code>)".
     * <p>
     * For example, if the input is "Broken Window (BW321)", the method will return "BW321".
     * If no parentheses are found in the input string, an empty string is returned.
     *
     * @param damageInfoString the input string containing the damage name and code in parentheses
     * @return the extracted damage code without parentheses, or an empty string if not found
     */
    public static String extractDamageCode(String damageInfoString) {
        Pattern pattern = Pattern.compile("\\(([^)]+)\\)");
        Matcher matcher = pattern.matcher(damageInfoString);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    /**
     * Extracts the damage code from an {@link EditText} whose content is expected to be in the format "<damage_name> (<damage_code>)".
     * <p>
     * This method first retrieves the trimmed string from the {@link EditText} using {@link #getStringOrNull(EditText)},
     * and then extracts the code from within the parentheses.
     *
     * @param editText The {@link EditText} containing the damage information.
     * @return the extracted damage code, or an empty string if the format is incorrect or the field is empty.
     */
    public static String extractDamageCode(EditText editText) {
        return extractDamageCode(getStringOrNull(editText));
    }

    /**
     * Safely parses a {@code double} value from the text content of the given {@link EditText}.
     * <p>
     * If the text is empty or not a valid number, this method returns {@code 0.0} instead of throwing an exception.
     *
     * @param editText The {@link EditText} from which to extract and parse the numeric value.
     * @return The parsed {@code double} value, or {@code 0.0} if parsing fails.
     */
    public static double parseSafeDouble(EditText editText) {
        try {
            return Double.parseDouble(getTextOrEmpty(editText));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Retrieves the trimmed text from an {@link EditText} field.
     * If the field is null or empty, an empty string is returned.
     *
     * @param editText The {@link EditText} from which to retrieve the text.
     * @return The trimmed text of the {@link EditText}, or an empty string if the field is null or empty.
     */
    public static String getTextOrEmpty(EditText editText) {
        if (editText != null && editText.getText() != null) {
            return editText.getText().toString().trim();
        }
        return "";
    }

    /**
     * Checks if the given string is not null and not empty after trimming whitespace.
     *
     * @param string the string to check
     * @return {@code true} if the string is not null and not empty; {@code false} otherwise
     */
    public static boolean isNotEmptyOrNull(String string) {
        return string != null && !string.trim().isEmpty();
    }

    /**
     * Sets up an AutoCompleteTextView with a given list of items,
     * adds default item at the top, and enables dropdown behavior.
     *
     * @param context      the context
     * @param dropdownView the AutoCompleteTextView to configure
     * @param items        the list of strings to populate (excluding default)
     * @param defaultItem  the default string to show first
     */
    public static void setupDropdown(
            Context context,
            AutoCompleteTextView dropdownView,
            @NonNull List<String> items,
            String defaultItem
    ) {
        List<String> allItems = new ArrayList<>();
        allItems.add(defaultItem);
        allItems.addAll(items);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context,
                R.layout.dropdown_item_with_divider,
                R.id.dropdown_item_text,
                allItems
        );

        dropdownView.setAdapter(adapter);

        dropdownView.setOnClickListener(v -> dropdownView.showDropDown());
        dropdownView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) dropdownView.showDropDown();
        });
    }

    /**
     * Configures the given EditText to accept numeric input with optional decimal places.
     * <p>
     * If {@code decimalPlaces} is greater than 0, the input will allow decimal numbers
     * with the specified number of decimal digits, and a maximum input length of 10 characters.
     * Otherwise, only integer input is allowed with a maximum input length of 10 characters.
     *
     * @param editText      the EditText to configure
     * @param decimalPlaces the number of allowed decimal places; set to 0 for integer-only input
     */
    public static void configureInput(EditText editText, int decimalPlaces) {
        if (decimalPlaces > 0) {
            editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            editText.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(decimalPlaces), new InputFilter.LengthFilter(10)});
        } else {
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        }
    }

    /**
     * Generates a filename for exporting data based on the current date and device store code.
     *
     * @return A string representing the export file name in the format:
     * "<EXPORT_PREF><DeviceStoreCode>_<EXPORT_DATE_FORMAT>"
     */
    public static String getExportFileName() {
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(EXPORT_DATE_FORMAT);
        String currentTimeString = currentTime.format(formatter);
        return EXPORT_PREF + PrefManager.getDeviceStoreCode() + "_" + currentTimeString + ".json";
    }
}
