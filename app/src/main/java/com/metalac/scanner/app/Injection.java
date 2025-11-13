package com.metalac.scanner.app;

import android.content.Context;

import androidx.annotation.NonNull;

import com.metalac.scanner.app.data.source.db.InventoryItemLocalDataSource;
import com.metalac.scanner.app.data.source.db.InventoryListLocalDataSource;
import com.metalac.scanner.app.data.source.db.MasterItemLocalDataSource;
import com.metalac.scanner.app.data.source.db.ScannerDatabase;
import com.metalac.scanner.app.data.source.repositories.InventoryItemRepository;
import com.metalac.scanner.app.data.source.repositories.InventoryListRepository;
import com.metalac.scanner.app.data.source.repositories.MasterItemRepository;
import com.metalac.scanner.app.executors.AppExecutors;

/**
 * Helper class for providing dependencies (simple manual dependency injection).
 */
public class Injection {

    @NonNull
    public static MasterItemRepository provideMasterItemRepository(@NonNull Context context) {
        return MasterItemRepository.getInstance(
                MasterItemLocalDataSource.getInstance(
                        new AppExecutors(),
                        ScannerDatabase.getInstance(context).masterItemDao()
                )
        );
    }

    @NonNull
    public static InventoryItemRepository provideInventoryItemRepository(@NonNull Context context) {
        return InventoryItemRepository.getInstance(
                InventoryItemLocalDataSource.getInstance(
                        new AppExecutors(),
                        ScannerDatabase.getInstance(context).inventoryItemDao()
                )
        );
    }

    @NonNull
    public static InventoryListRepository provideInventoryListRepository(@NonNull Context context) {
        return InventoryListRepository.getInstance(
                InventoryListLocalDataSource.getInstance(
                        new AppExecutors(),
                        ScannerDatabase.getInstance(context).inventoryListDao()
                )
        );
    }
}
