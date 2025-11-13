package com.metalac.scanner.app.helpers;

import android.os.Build;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ScannerHelperFactory {
    public static final String INTENT_ACTION = "com.metalac.scanner.app.SCAN";

    private static final Map<String, Supplier<ScannerHelper>> MANUFACTURER_HELPERS = new HashMap<>();
    private static final Map<String, Supplier<ScannerHelper>> MODEL_HELPERS = new HashMap<>();
    private static final Supplier<ScannerHelper> DEFAULT_HELPER = ZebraScannerHelper::new;

    static {
        // Register manufacturer-based scanners (lowercase keys)
        MANUFACTURER_HELPERS.put("zebra", ZebraScannerHelper::new);
        MANUFACTURER_HELPERS.put("pointmobile", PM84ScannerHelper::new);

        // Register model-based scanners (uppercase keys)
        MODEL_HELPERS.put("PM84", PM84ScannerHelper::new);
    }

    public static ScannerHelper getScannerHelper() {
        String manufacturer = Build.MANUFACTURER.toLowerCase();
        String model = Build.MODEL.toUpperCase();

        Supplier<ScannerHelper> helper = MANUFACTURER_HELPERS.get(manufacturer);
        if (helper != null) {
            return helper.get();
        }

        helper = MODEL_HELPERS.get(model);
        if (helper != null) {
            return helper.get();
        }

        return DEFAULT_HELPER.get();
    }
}
