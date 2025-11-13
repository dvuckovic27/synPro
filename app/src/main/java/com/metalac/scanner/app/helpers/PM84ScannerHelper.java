package com.metalac.scanner.app.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.metalac.scanner.app.models.ScanResult;

public class PM84ScannerHelper implements ScannerHelper {
    @Override
    public void configureScanner(Context context) {
        Intent intent = new Intent("com.pointmobile.scanner.ACTION_SETTING_CHANGE");
        Bundle bundle = new Bundle();

        bundle.putString("scanner_enable", "true");
        bundle.putString("output_enable", "true");
        bundle.putString("output_method", "intent");
        bundle.putString("intent_enable", "true");

        bundle.putString("intent_action", ScannerHelperFactory.INTENT_ACTION);
        bundle.putString("intent_category", Intent.CATEGORY_DEFAULT);

        bundle.putString("intent_delivery_time", "0");

        bundle.putString("decode_ean13", "true");
        bundle.putString("decode_ean8", "true");

        intent.putExtras(bundle);
        context.sendBroadcast(intent);
    }

    @Override
    public IntentFilter getScanIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ScannerHelperFactory.INTENT_ACTION);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        return filter;
    }

    @Override
    public ScanResult parseScanIntent(Intent intent) {
        if (intent == null || !ScannerHelperFactory.INTENT_ACTION.equals(intent.getAction())) {
            return null;
        }

        String data = intent.getStringExtra("EXTRA_EVENT_DECODE_STRING_VALUE");
        String type = intent.getStringExtra("EXTRA_EVENT_SYMBOL_NAME");

        return new ScanResult(data, type);
    }
}
