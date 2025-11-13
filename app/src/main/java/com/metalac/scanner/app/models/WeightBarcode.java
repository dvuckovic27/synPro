package com.metalac.scanner.app.models;

import androidx.annotation.NonNull;

import com.metalac.scanner.app.R;
import com.metalac.scanner.app.ScannerReaderApplication;
import com.metalac.scanner.app.view.ScannerReaderError;

/**
 * Represents a parsed EAN-13 weight barcode in format:
 * 28 + 5-digit alternative code + 5-digit weight in grams
 * <p>
 * Example: 284011200870
 * - "28" (fixed)
 * - "40112" (alt code)
 * - "00870" (weight = 8.70kg)
 */
public class WeightBarcode {

    public static final String PREFIX = "28";
    private static final int PREFIX_LENGTH = PREFIX.length();
    private static final int ALT_CODE_LENGTH = 5;
    private static final int WEIGHT_START_INDEX = PREFIX_LENGTH + ALT_CODE_LENGTH;
    private static final int WEIGHT_END_INDEX = 12; // exclusive
    private static final double GRAMS_DIVISOR = 1000;

    private final String barcode;
    private final int altCode;
    private final double weightKg;

    /**
     * Constructs a {@code WeightBarcode} object by parsing an EAN-13 barcode string
     * representing a product with embedded weight information.
     * <p>
     * The barcode must follow the format:
     * <ul>
     *   <li>Prefix: 2 digits (e.g., "28") indicating it is a weight barcode</li>
     *   <li>Alternate Code (altCode): 5 digits used as a product identifier</li>
     *   <li>Weight: 5 digits representing the product's weight in grams</li>
     * </ul>
     * Example barcode: {@code "2812345010000"} → altCode = 12345, weight = 10000g (10.0kg)
     * <p>
     * The weight is converted to kilograms and stored as a decimal.
     *
     * @param barcode the full EAN-13 barcode string to be parsed.
     * @throws ScannerReaderError if:
     *                            <ul>
     *                              <li>the barcode is not numeric or not in the expected format</li>
     *                              <li>the alt code segment is not a valid integer</li>
     *                              <li>the weight segment is not a valid integer</li>
     *                            </ul>
     */
    public WeightBarcode(@NonNull String barcode) throws ScannerReaderError {
        this.barcode = barcode;

        try {
            this.altCode = Integer.parseInt(barcode.substring(PREFIX_LENGTH, PREFIX_LENGTH + ALT_CODE_LENGTH));
        } catch (NumberFormatException e) {
            throw new ScannerReaderError(
                    getString(R.string.invalid_alt_code_value)
            );
        }

        try {
            String weightPart = barcode.substring(WEIGHT_START_INDEX, WEIGHT_END_INDEX);
            int grams = Integer.parseInt(weightPart);
            this.weightKg = grams / GRAMS_DIVISOR;
        } catch (NumberFormatException e) {
            throw new ScannerReaderError(
                    getString(R.string.invalid_weight_value)
            );
        }
    }

    public String getBarcode() {
        return barcode;
    }

    public int getAltCode() {
        return altCode;
    }

    public double getWeightKg() {
        return weightKg;
    }

    private static String getString(int resId, Object... formatArgs) {
        return ScannerReaderApplication.getAppContext().getString(resId, formatArgs);
    }
}
