package com.metalac.scanner.app.executors;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;

/**
 * Executor that posts tasks to the main (UI) thread using a {@link Handler}.
 */
public class MainThreadExecutor implements Executor {

    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    @Override
    public void execute(@NonNull Runnable command) {
        mainThreadHandler.post(command);
    }
}
