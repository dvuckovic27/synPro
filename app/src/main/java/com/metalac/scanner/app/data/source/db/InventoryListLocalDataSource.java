package com.metalac.scanner.app.data.source.db;

import android.content.Context;

import androidx.annotation.NonNull;

import com.metalac.scanner.app.executors.AppExecutors;
import com.metalac.scanner.app.R;
import com.metalac.scanner.app.ScannerReaderApplication;
import com.metalac.scanner.app.data.source.db.dao.InventoryListDao;
import com.metalac.scanner.app.data.source.interfaces.InventoryListDataSource;
import com.metalac.scanner.app.models.InventoryList;
import com.metalac.scanner.app.models.InventoryListWithCount;
import com.metalac.scanner.app.view.ScannerReaderError;

import java.util.List;
import java.util.function.Supplier;

public class InventoryListLocalDataSource implements InventoryListDataSource {

    private static volatile InventoryListLocalDataSource INSTANCE;

    private final InventoryListDao mInventoryListDao;
    private final AppExecutors mAppExecutors;

    private InventoryListLocalDataSource(@NonNull AppExecutors appExecutors, @NonNull InventoryListDao inventoryItemDao) {
        this.mAppExecutors = appExecutors;
        this.mInventoryListDao = inventoryItemDao;
    }

    public static InventoryListLocalDataSource getInstance(@NonNull AppExecutors appExecutors, @NonNull InventoryListDao inventoryItemDao) {
        if (INSTANCE == null) {
            synchronized (InventoryListLocalDataSource.class) {
                if (INSTANCE == null) {
                    INSTANCE = new InventoryListLocalDataSource(appExecutors, inventoryItemDao);
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Adds a new inventory list with the specified name asynchronously.
     * <p>
     * Inserts a new {@link InventoryList} into the database, then retrieves the inserted item.
     * If successful, calls {@code onListAdded()} on the main thread with the inserted item.
     * Otherwise, calls {@code onFailToAddList()} with a {@link ScannerReaderError}.
     * </p>
     *
     * @param inventoryListName The name of the new inventory list to add.
     * @param callback          Callback interface to notify success or failure of the add operation.
     */
    @Override
    public void addInventoryList(String inventoryListName, IAddInventoryListCallback callback) {
        Runnable runnable = () -> {
            int inventoryListId = (int) mInventoryListDao.insert(new InventoryList(inventoryListName));
            if (inventoryListId > -1) {
                InventoryList inventoryList = mInventoryListDao.getListById(inventoryListId);
                mAppExecutors.mainThread().execute(() -> {
                    if (inventoryList == null) {
                        callback.onFailToAddList(new ScannerReaderError(ScannerReaderApplication.getAppContext().getString(R.string.preview_item_error)));
                    } else {
                        callback.onListAdded(inventoryList);
                    }
                });
            } else {
                mAppExecutors.mainThread().execute(() -> callback.onFailToAddList(new ScannerReaderError(ScannerReaderApplication.getAppContext().getString(R.string.add_product_error_title))));
            }
        };
        mAppExecutors.diskIO().execute(runnable);
    }

    /**
     * Retrieves all inventory lists asynchronously from the database.
     * <p>
     * The list retrieval is performed on a background thread. If the result is non-empty,
     * {@code onInventoryListsLoaded()} is called with the list on the main thread.
     * If the list is empty or an exception occurs, {@code onFailToLoadInventoryLists()} is called
     * with a {@link ScannerReaderError}.
     * </p>
     *
     * @param callback Callback interface used to notify success or failure of loading inventory lists.
     */
    @Override
    public void getAllInventoryLists(ILoadInventoryListsCallback callback) {
        Runnable runnable = () -> {
            try {
                List<InventoryListWithCount> inventoryLists = mInventoryListDao.getAllInventoryListsWithItemCount();
                mAppExecutors.mainThread().execute(() -> {
                    if (inventoryLists.isEmpty()) {
                        callback.onFailToLoadInventoryLists(new ScannerReaderError(ScannerReaderApplication.getAppContext().getString(R.string.no_list_error)));
                    } else {
                        callback.onInventoryListsLoaded(inventoryLists);
                    }
                });

            } catch (Exception ignore) {
                mAppExecutors.mainThread().execute(() -> callback.onFailToLoadInventoryLists(new ScannerReaderError(ScannerReaderApplication.getAppContext().getString(R.string.database_error_title))));
            }
        };
        mAppExecutors.diskIO().execute(runnable);
    }

    /**
     * Loads an inventory list by its number asynchronously.
     *
     * @param inventoryListId The ID of the inventory list to load.
     * @param callback        Callback to receive the result or error.
     */
    @Override
    public void getListById(int inventoryListId, ILoadInventoryListCallback callback) {
        loadInventoryList(() -> mInventoryListDao.getListById(inventoryListId), callback);
    }

    /**
     * Loads the current inventory list asynchronously.
     *
     * @param callback Callback to receive the loaded inventory list or an error.
     */
    @Override
    public void getCurrentList(ILoadInventoryListCallback callback) {
        loadInventoryList(mInventoryListDao::getCurrentList, callback);
    }

    /**
     * Updates the selected state of inventory list items asynchronously.
     * <p>
     * It first clears the current selected list (if any), then sets the provided
     * {@link InventoryList} as selected. Calls back on success or failure on the main thread.
     * </p>
     *
     * @param inventoryList The inventory list item to set as selected.
     * @param callback      Callback to notify success or failure of the update operation.
     */
    @Override
    public void updateInventoryList(InventoryList inventoryList, IUpdateInventoryListCallback callback) {
        Runnable runnable = () -> {
            Context context = ScannerReaderApplication.getAppContext();
            ScannerReaderError error = new ScannerReaderError(
                    context.getString(R.string.update_failed_title),
                    context.getString(R.string.update_failed_subtitle)
            );

            try {
                InventoryList currentList = mInventoryListDao.getCurrentList();

                if (currentList != null) {
                    currentList.setSelected(0);
                    int rowsUpdated = mInventoryListDao.updateInventoryList(currentList);
                    if (rowsUpdated == 0) {
                        postFailure(callback, error);
                        return;
                    }
                }

                inventoryList.setSelected(1);
                int rowsUpdated = mInventoryListDao.updateInventoryList(inventoryList);
                if (rowsUpdated == 0) {
                    postFailure(callback, error);
                } else {
                    mAppExecutors.mainThread().execute(callback::onInventoryListUpdated);
                }

            } catch (Exception e) {
                ScannerReaderError dbError = new ScannerReaderError(
                        context.getString(R.string.database_error_title),
                        e.getMessage()
                );
                postFailure(callback, dbError);
            }
        };

        mAppExecutors.diskIO().execute(runnable);
    }

    /**
     * Deletes all inventory list data and resets related counters asynchronously.
     * <p>
     * Calls {@code onInventoryListDataDeleted()} on success, or {@code onDeleteInventoryListDataFailed()}
     * with error details on failure, both on the main thread.
     * </p>
     *
     * @param callback Callback to notify the result of the deletion operation.
     */
    @Override
    public void deleteInventoryListData(@NonNull IOnInventoryListDataDeletedCallback callback) {
        Runnable runnable = () -> {
            try {
                mInventoryListDao.deleteAndRestartAllInventoryListData();
                mInventoryListDao.resetInventoryListData();
                mAppExecutors.mainThread().execute(callback::onInventoryListDataDeleted);
            } catch (Exception e) {
                mAppExecutors.mainThread().execute(() ->
                        callback.onDeleteInventoryListDataFailed(new ScannerReaderError(ScannerReaderApplication.getAppContext().getString(R.string.delete_data_fail_title), e.getMessage())));
            }
        };
        mAppExecutors.diskIO().execute(runnable);
    }

    @Override
    public void checkIfAnyInventoryListExists(@NonNull ICheckInventoryListExistsCallback callback) {
        Runnable runnable = () -> {
            try {
                boolean exists = mInventoryListDao.checkIfAnyInventoryListExists();
                mAppExecutors.mainThread().execute(() -> callback.onResult(exists));
            } catch (Exception e) {
                ScannerReaderError error = new ScannerReaderError(e.getMessage());
                mAppExecutors.mainThread().execute(() -> callback.onError(error));
            }
        };
        mAppExecutors.diskIO().execute(runnable);
    }


    /**
     * Posts a failure callback on the main thread with the provided error.
     *
     * @param callback The callback to notify about the failure.
     * @param error    The {@link ScannerReaderError} describing the failure.
     */
    private void postFailure(IUpdateInventoryListCallback callback, ScannerReaderError error) {
        mAppExecutors.mainThread().execute(() -> callback.onFailToUpdateInventoryList(error));
    }

    /**
     * Loads an inventory list asynchronously using the provided supplier.
     * <p>
     * Executes the supplier on a background thread to fetch an {@link InventoryList}.
     * If successful, calls {@code onInventoryListLoaded()} on the main thread;
     * otherwise, calls {@code onFailToLoadInventoryList()} with an error.
     * </p>
     *
     * @param supplier A {@link Supplier} that provides the inventory list item.
     * @param callback Callback to notify success or failure of the load operation.
     */
    private void loadInventoryList(Supplier<InventoryList> supplier, ILoadInventoryListCallback callback) {
        Runnable runnable = () -> {
            InventoryList inventoryList = supplier.get();
            mAppExecutors.mainThread().execute(() -> {
                if (inventoryList != null) {
                    callback.onInventoryListLoaded(inventoryList);
                } else {
                    callback.onFailToLoadInventoryList(
                            new ScannerReaderError(
                                    ScannerReaderApplication.getAppContext().getString(R.string.no_list_error)
                            )
                    );
                }
            });
        };
        mAppExecutors.diskIO().execute(runnable);
    }
}
