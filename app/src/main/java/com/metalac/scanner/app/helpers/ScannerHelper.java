package com.metalac.scanner.app.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.metalac.scanner.app.models.ScanResult;

public interface ScannerHelper {
    void configureScanner(Context context);

    IntentFilter getScanIntentFilter();

    ScanResult parseScanIntent(Intent intent);
}
