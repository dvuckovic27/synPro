package com.metalac.scanner.app.helpers;

import static com.metalac.scanner.app.helpers.ScannerHelperFactory.INTENT_ACTION;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.metalac.scanner.app.models.ScanResult;

public class ZebraScannerHelper implements ScannerHelper {

    private static final String PROFILE_NAME = "MetalacScannerProfile";
    private static final String TRUE = "true";
    private static final String FALSE = "false";
    private static final String PLUGIN_CONFIG = "PLUGIN_CONFIG";
    private static final String PLUGIN_NAME = "PLUGIN_NAME";
    private static final String RESET_CONFIG = "RESET_CONFIG";
    private static final String PARAM_LIST = "PARAM_LIST";
    private static final String ACTION_DATAWEDGE = "com.symbol.datawedge.api.ACTION";
    private static final String EXTRA_DATA_STRING = "com.symbol.datawedge.data_string";
    private static final String EXTRA_LABEL_TYPE = "com.symbol.datawedge.label_type";

    @Override
    public void configureScanner(Context context) {
        sendCreateProfileIntent(context);

        Bundle profileConfig = createBaseProfileConfig(context);

        // Barcode plugin configuration
        profileConfig.putBundle(PLUGIN_CONFIG, createBarcodeConfig());
        sendSetConfigIntent(context, profileConfig, false);

        // Intent plugin configuration
        profileConfig.putBundle(PLUGIN_CONFIG, createIntentPluginConfig());
        sendSetConfigIntent(context, profileConfig, true);

        // Keystroke plugin configuration
        profileConfig.putBundle(PLUGIN_CONFIG, createKeystrokePluginConfig());
        sendSetConfigIntent(context, profileConfig, true);
    }

    @Override
    public IntentFilter getScanIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.symbol.datawedge.api.RESULT_ACTION");
        intentFilter.addAction(INTENT_ACTION);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        return intentFilter;
    }

    @Override
    public ScanResult parseScanIntent(Intent intent) {
        if (intent == null || !ScannerHelperFactory.INTENT_ACTION.equals(intent.getAction())) {
            return null;
        }
        String data = intent.getStringExtra(EXTRA_DATA_STRING);
        String type = intent.getStringExtra(EXTRA_LABEL_TYPE);
        return new ScanResult(data, type);
    }

    private Bundle createBaseProfileConfig(Context context) {
        Bundle profileConfig = new Bundle();
        profileConfig.putString("PROFILE_NAME", PROFILE_NAME);
        profileConfig.putString("PROFILE_ENABLED", TRUE);
        profileConfig.putString("CONFIG_MODE", "CREATE_IF_NOT_EXIST");

        Bundle appConfig = new Bundle();
        appConfig.putString("PACKAGE_NAME", context.getPackageName());
        appConfig.putStringArray("ACTIVITY_LIST", new String[]{"*"});
        profileConfig.putParcelableArray("APP_LIST", new Bundle[]{appConfig});
        return profileConfig;
    }

    private Bundle createBarcodeConfig() {
        Bundle barcodeConfig = new Bundle();
        barcodeConfig.putString(PLUGIN_NAME, "BARCODE");
        barcodeConfig.putString(RESET_CONFIG, TRUE);

        Bundle params = new Bundle();
        params.putString("scanner_selection", "auto");
        params.putString("scanner_input_enabled", TRUE);
        params.putString("decoder_ean13", TRUE);
        params.putString("decoder_ean8", TRUE);
        params.putString("decoder_code128", FALSE);
        barcodeConfig.putBundle(PARAM_LIST, params);
        return barcodeConfig;
    }

    private Bundle createIntentPluginConfig() {
        Bundle intentConfig = new Bundle();
        intentConfig.putString(PLUGIN_NAME, "INTENT");
        intentConfig.putString(RESET_CONFIG, TRUE);

        Bundle params = new Bundle();
        params.putString("intent_output_enabled", TRUE);
        params.putString("intent_action", INTENT_ACTION);
        params.putString("intent_delivery", "2");
        intentConfig.putBundle(PARAM_LIST, params);
        return intentConfig;
    }

    private Bundle createKeystrokePluginConfig() {
        Bundle keystrokeConfig = new Bundle();
        keystrokeConfig.putString(PLUGIN_NAME, "KEYSTROKE");
        keystrokeConfig.putString(RESET_CONFIG, TRUE);

        Bundle params = new Bundle();
        params.putString("keystroke_output_enabled", FALSE);
        keystrokeConfig.putBundle(PARAM_LIST, params);
        return keystrokeConfig;
    }

    private void sendCreateProfileIntent(Context context) {
        Intent intent = new Intent(ACTION_DATAWEDGE);
        intent.putExtra("com.symbol.datawedge.api.CREATE_PROFILE", PROFILE_NAME);
        context.sendBroadcast(intent);
    }

    private void sendSetConfigIntent(Context context, Bundle config, boolean sendResult) {
        Intent intent = new Intent(ACTION_DATAWEDGE);
        intent.putExtra("com.symbol.datawedge.api.SET_CONFIG", config);
        if (sendResult) {
            intent.putExtra("SEND_RESULT", "LAST_RESULT");
        }
        context.sendBroadcast(intent);
    }
}