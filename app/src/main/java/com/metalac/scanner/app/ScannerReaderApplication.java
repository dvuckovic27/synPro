package com.metalac.scanner.app;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;

/**
 * Custom Application class to provide a global application context.
 * <p>
 * Stores the application context statically to allow easy access from anywhere in the app.
 */
public class ScannerReaderApplication extends Application {

    /**
     * Static reference to the application context.
     */
    private static Context mAppContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mAppContext = getApplicationContext();
    }

    /**
     * Returns the global application context.
     *
     * @return Application context.
     */
    @NonNull
    public static Context getAppContext() {
        return mAppContext;
    }
}
