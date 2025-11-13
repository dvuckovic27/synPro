package com.metalac.scanner.app.executors;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Executor implementation that uses a single background thread for disk IO operations.
 * Ensures tasks are executed sequentially on a dedicated thread.
 */
public class DiskIOThreadExecutor implements Executor {

    private final Executor mDiskIO;

    public DiskIOThreadExecutor() {
        mDiskIO = Executors.newSingleThreadExecutor();
    }

    /**
     * Executes the given command at some time in the future on a dedicated disk IO thread.
     *
     * @param command The runnable task to execute; must not be null.
     */
    @Override
    public void execute(@NonNull Runnable command) {
        mDiskIO.execute(command);
    }
}
