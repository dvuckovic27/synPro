package com.metalac.scanner.app.data.source.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.metalac.scanner.app.data.source.db.dao.InventoryItemDao;
import com.metalac.scanner.app.data.source.db.dao.InventoryListDao;
import com.metalac.scanner.app.models.DamageInfo;
import com.metalac.scanner.app.models.InventoryItem;
import com.metalac.scanner.app.models.InventoryList;
import com.metalac.scanner.app.models.MasterItem;
import com.metalac.scanner.app.data.source.db.dao.MasterItemDao;

/**
 * Room database class for the scanner application.
 * Holds the database instance and provides access to DAO interfaces.
 */
@Database(entities = {MasterItem.class, InventoryItem.class, DamageInfo.class, InventoryList.class}, version = 1)
public abstract class ScannerDatabase extends RoomDatabase {

    // Singleton instance of the database
    private static volatile ScannerDatabase INSTANCE;

    public abstract MasterItemDao masterItemDao();

    public abstract InventoryItemDao inventoryItemDao();

    public abstract InventoryListDao inventoryListDao();

    /**
     * Returns the singleton instance of {@link ScannerDatabase}, creating it if necessary.
     * Uses a destructive migration fallback strategy, which wipes and rebuilds the database
     * if the schema changes.
     *
     * @param context Application context
     * @return Singleton instance of {@link ScannerDatabase}
     */
    public static ScannerDatabase getInstance(Context context) {
        String databaseName = "scanner_db";

        if (INSTANCE == null) {
            synchronized (ScannerDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    ScannerDatabase.class, databaseName)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}