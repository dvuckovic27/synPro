package com.metalac.scanner.app.executors;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Executor manager that provides different thread pools for disk IO,
 * local background execution, and main thread operations.
 * <p>
 * Helps centralize and standardize thread usage across the app.
 */
public class AppExecutors {
    private static final int THREAD_COUNT = 3;

    private final Executor diskIO;

    private final Executor localExecution;

    private final Executor mainThread;

    AppExecutors(Executor diskIO, Executor localExecution, Executor mainThread) {
        this.diskIO = diskIO;
        this.localExecution = localExecution;
        this.mainThread = mainThread;
    }

    /**
     * Creates an instance with default executors:
     * - Disk IO: single-threaded
     * - Local execution: fixed thread pool
     * - Main thread: handler-based main thread executor
     */
    public AppExecutors() {
        this(new DiskIOThreadExecutor(), Executors.newFixedThreadPool(THREAD_COUNT),
                new MainThreadExecutor());
    }

    public Executor diskIO() {
        return diskIO;
    }

    public Executor localExecution() {
        return localExecution;
    }

    public Executor mainThread() {
        return mainThread;
    }

}
