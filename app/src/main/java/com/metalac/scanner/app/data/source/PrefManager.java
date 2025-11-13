package com.metalac.scanner.app.data.source;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.metalac.scanner.app.ScannerReaderApplication;

/**
 * {@code PrefManager} is a centralized utility class responsible for managing
 * application preferences using Android's {@link android.content.SharedPreferences}.
 * <p>
 * It provides a static API to store and retrieve various key-value pairs
 * that define the app's persistent configuration and state.
 * <p>
 * Typical usage includes saving and accessing data such as:
 * <ul>
 *     <li>Device-specific settings</li>
 *     <li>User input or selections</li>
 *     <li>Flags indicating setup or synchronization status</li>
 *     <li>Other small pieces of persistent data required across app sessions</li>
 * </ul>
 *
 * <p>
 * All data is stored privately within the application's context.
 * This class is designed to be safe for use throughout the application lifecycle.
 * </p>
 */

public class PrefManager {
    private static final String PREF_NAME = "scanner_preferences";
    private static final String DEVICE_NAME = "device_name";
    private static final String DEVICE_STORE_CODE = "device_store_code";
    private static final String HAS_MASTER_DATA = "has_master_data";
    private static final String LAST_MASTER_DATA_SYNC = "last_master_data_sync";
    private static final String LAST_DATA_EXPORT = "last_data_export";

    private static SharedPreferences getSharedPreferences() {
        Context context = ScannerReaderApplication.getAppContext();
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void setDeviceName(String deviceName) {
        getSharedPreferences()
                .edit()
                .putString(DEVICE_NAME, deviceName)
                .apply();
    }

    @NonNull
    public static String getDeviceName() {
        return getSharedPreferences().getString(DEVICE_NAME, "");
    }

    public static void setDeviceStoreCode(String storeCode) {
        getSharedPreferences()
                .edit()
                .putString(DEVICE_STORE_CODE, storeCode)
                .apply();
    }

    @NonNull
    public static String getDeviceStoreCode() {
        return getSharedPreferences().getString(DEVICE_STORE_CODE, "");
    }

    public static void setHasMasterData(boolean hasMasterData) {
        getSharedPreferences()
                .edit()
                .putBoolean(HAS_MASTER_DATA, hasMasterData)
                .apply();
    }

    public static boolean hasMasterData() {
        return getSharedPreferences().getBoolean(HAS_MASTER_DATA, false);
    }

    public static boolean isStoreCodeSet() {
        return !getDeviceStoreCode().isEmpty();
    }

    public static boolean isDeviceNameSet() {
        return !getDeviceName().isEmpty();
    }

    public static void setLastMasterDataSyncDate(@NonNull String masterDataSyncDate) {
        getSharedPreferences()
                .edit()
                .putString(LAST_MASTER_DATA_SYNC, masterDataSyncDate)
                .apply();
    }

    @NonNull
    public static String getLastMasterDataSyncDate() {
        return getSharedPreferences().getString(LAST_MASTER_DATA_SYNC, "");
    }

    public static void setLastDataExportDate(@NonNull String dataExportDate) {
        getSharedPreferences()
                .edit()
                .putString(LAST_DATA_EXPORT, dataExportDate)
                .apply();
    }

    @NonNull
    public static String getLastDataExportDate() {
        return getSharedPreferences().getString(LAST_DATA_EXPORT, "");
    }
}
