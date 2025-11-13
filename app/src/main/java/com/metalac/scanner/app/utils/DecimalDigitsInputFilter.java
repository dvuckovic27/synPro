package com.metalac.scanner.app.utils;

import android.text.InputFilter;
import android.text.Spanned;

public class DecimalDigitsInputFilter implements InputFilter {

    private final int maxDecimalCount;

    public DecimalDigitsInputFilter(int maxDecimalCount) {
        this.maxDecimalCount = maxDecimalCount;
    }

    /**
     * Limits the number of decimal places allowed in the input based on maxDecimalCount.
     */
    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        String enteredText = dest.subSequence(0, dstart)
                + source.toString()
                + dest.subSequence(dend, dest.length());

        if (enteredText.equals(".")) {
            return "0.";
        }

        if (enteredText.contains(".")) {
            int index = enteredText.indexOf(".");
            int decimalCount = enteredText.length() - index - 1;

            if (decimalCount > maxDecimalCount) {
                return "";
            }
        }

        return null;
    }
}
