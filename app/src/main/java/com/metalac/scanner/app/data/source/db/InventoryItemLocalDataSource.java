package com.metalac.scanner.app.data.source.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.paging.PagingSource;

import com.google.android.gms.common.util.CollectionUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.metalac.scanner.app.executors.AppExecutors;
import com.metalac.scanner.app.R;
import com.metalac.scanner.app.ScannerReaderApplication;
import com.metalac.scanner.app.models.InventoryExportItem;
import com.metalac.scanner.app.utils.Utils;
import com.metalac.scanner.app.data.source.db.dao.InventoryItemDao;
import com.metalac.scanner.app.data.source.interfaces.InventoryItemDataSource;
import com.metalac.scanner.app.models.InventoryItem;
import com.metalac.scanner.app.models.InventoryItemWithDamageDesc;
import com.metalac.scanner.app.models.ProductPreviewItem;
import com.metalac.scanner.app.models.QueryMasterItem;
import com.metalac.scanner.app.view.ScannerReaderError;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class InventoryItemLocalDataSource implements InventoryItemDataSource {

    private static volatile InventoryItemLocalDataSource INSTANCE;

    private final InventoryItemDao mInventoryItemDao;
    private final AppExecutors mAppExecutors;

    private InventoryItemLocalDataSource(@NonNull AppExecutors appExecutors, @NonNull InventoryItemDao inventoryItemDao) {
        this.mAppExecutors = appExecutors;
        this.mInventoryItemDao = inventoryItemDao;
    }

    public static InventoryItemLocalDataSource getInstance(@NonNull AppExecutors appExecutors, @NonNull InventoryItemDao inventoryItemDao) {
        if (INSTANCE == null) {
            synchronized (InventoryItemLocalDataSource.class) {
                if (INSTANCE == null) {
                    INSTANCE = new InventoryItemLocalDataSource(appExecutors, inventoryItemDao);
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Adds a new {@link InventoryItem} to the database asynchronously.
     *
     * <p>This method performs the following steps:</p>
     * <ul>
     *   <li>Calculates the next index for the item in the inventory list</li>
     *   <li>Sets the product's sequence number</li>
     *   <li>Inserts the item into the database</li>
     *   <li>Retrieves the inserted item as a {@link ProductPreviewItem}</li>
     * </ul>
     *
     * <p>The result is returned via the provided callback on the main thread.</p>
     *
     * @param inventoryItem         The {@link InventoryItem} to add.
     * @param inventoryItemCallback The callback used to notify success or failure of the operation.
     *                              On success, returns a {@link ProductPreviewItem}; on failure, returns a {@link ScannerReaderError}.
     */
    @Override
    public void addInventoryItem(InventoryItem inventoryItem, @NonNull IAddInventoryItemCallback inventoryItemCallback) {
        Runnable runnable = () -> {
            int listItemCount = mInventoryItemDao.getMaxIndexInList(inventoryItem.getInventoryListId());
            inventoryItem.setIndexInList(listItemCount + 1);
            inventoryItem.setId(0);
            int inventoryItemId = (int) mInventoryItemDao.insertInventoryItem(inventoryItem);
            if (inventoryItemId > -1) {
                ProductPreviewItem productPreviewItem = mInventoryItemDao.getProductPreviewByInventoryItemId(inventoryItemId);
                mAppExecutors.mainThread().execute(() -> {
                    if (productPreviewItem == null) {
                        inventoryItemCallback.onFailure(new ScannerReaderError(ScannerReaderApplication.getAppContext().getString(R.string.preview_item_error)));
                    } else {
                        inventoryItemCallback.onSuccess(productPreviewItem);
                    }
                });
            } else {
                mAppExecutors.mainThread().execute(() -> inventoryItemCallback.onFailure(new ScannerReaderError(ScannerReaderApplication.getAppContext().getString(R.string.add_product_error_title))));
            }
        };
        mAppExecutors.diskIO().execute(runnable);
    }

    /**
     * Retrieves a list of {@link ProductPreviewItem} objects for a given inventory list number asynchronously.
     * <p>
     * The items are loaded on a background thread, and the result is delivered on the main thread via the
     * provided callback. If the list is empty or an error occurs, {@code onFailure()} is triggered with a
     * {@link ScannerReaderError}.
     * </p>
     *
     * @param inventoryListId             The ID of the inventory list for which items should be retrieved.
     * @param iLoadInventoryItemsCallback Callback to deliver the result or error.
     *                                    On success, returns a list of {@link ProductPreviewItem}s;
     *                                    on failure, returns a {@link ScannerReaderError}.
     */
    @Override
    public void getInventoryItemList(int inventoryListId, @NonNull ILoadInventoryItemsCallback iLoadInventoryItemsCallback) {
        Runnable runnable = () -> {
            try {
                List<ProductPreviewItem> productPreviewItems = mInventoryItemDao.getItemsForDisplay(inventoryListId);
                mAppExecutors.mainThread().execute(() -> {
                    if (productPreviewItems.isEmpty()) {
                        iLoadInventoryItemsCallback.onFailure(new ScannerReaderError(ScannerReaderApplication.getAppContext().getString(R.string.no_products_error)));
                    } else {
                        iLoadInventoryItemsCallback.onSuccess(productPreviewItems);
                    }
                });

            } catch (Exception ignore) {
                mAppExecutors.mainThread().execute(() -> iLoadInventoryItemsCallback.onFailure(new ScannerReaderError(ScannerReaderApplication.getAppContext().getString(R.string.no_products_error))));
            }
        };
        mAppExecutors.diskIO().execute(runnable);
    }

    /**
     * Retrieves an {@link InventoryItem} by its product sequence number asynchronously.
     * <p>
     * The item is loaded from the database using a background thread. Once retrieved,
     * the result is delivered on the main thread through the provided callback.
     * If the item is not found, the callback's {@code onInventoryItemLoadFailed()} method is invoked
     * with a {@link ScannerReaderError}.
     * </p>
     *
     * @param id       The unique sequence number of the product to retrieve.
     * @param callback The callback used to deliver the loaded item or report failure.
     *                 On success, {@code onInventoryItemLoaded()} is called with the item.
     *                 On failure, {@code onInventoryItemLoadFailed()} is called with an error.
     */
    @Override
    public void getInventoryItemById(long id, @NonNull ILoadInventoryItemCallback callback) {
        Runnable runnable = () -> {
            InventoryItemWithDamageDesc inventoryItemWithDamageDesc = mInventoryItemDao.getItemById(id);
            mAppExecutors.mainThread().execute(() -> {
                if (inventoryItemWithDamageDesc == null || inventoryItemWithDamageDesc.getItem() == null) {
                    callback.onInventoryItemLoadFailed(new ScannerReaderError(ScannerReaderApplication.getAppContext().getString(R.string.no_products_error)));
                } else {
                    callback.onInventoryItemLoaded(new InventoryItem(inventoryItemWithDamageDesc));
                }
            });
        };
        mAppExecutors.diskIO().execute(runnable);
    }

    /**
     * Attempts to void an inventory item with the given ID.
     * <p>
     * This method runs asynchronously on a background thread.
     * On success, {@link IVoidItemCallback#onItemVoided()} is called on the main thread.
     * On failure (e.g., item not found or already voided),
     * {@link IVoidItemCallback#onFailToVoidItem(ScannerReaderError)} is invoked on the main thread
     * with the corresponding error.
     *
     * @param id                The ID of the inventory item to void.
     * @param iVoidItemCallback Callback to receive success or failure notifications.
     */
    @Override
    public void voidInventoryItem(long id, @NonNull IVoidItemCallback iVoidItemCallback) {
        mAppExecutors.diskIO().execute(() -> {
            InventoryItem item = mInventoryItemDao.getInventoryItemById(id);

            if (item == null) {
                postVoidItemFailure(iVoidItemCallback, new ScannerReaderError(ScannerReaderError.ITEM_NOT_FOUND));
                return;
            }

            if (item.getStatus() != InventoryItem.Status.NON_VOIDED.getStatusVal()) {
                postVoidItemFailure(iVoidItemCallback, new ScannerReaderError(ScannerReaderError.ALREADY_VOIDED));
                return;
            }

            int newIndex = mInventoryItemDao.getMaxIndexInList(item.getInventoryListId()) + 1;

            try {
                mInventoryItemDao.voidItem(id, newIndex);
                mAppExecutors.mainThread().execute(iVoidItemCallback::onItemVoided);
            } catch (Exception e) {
                postVoidItemFailure(iVoidItemCallback, new ScannerReaderError(e.getMessage()));
            }
        });
    }

    /**
     * Posts an error to the main thread callback.
     */
    private void postVoidItemFailure(@NonNull IVoidItemCallback callback, @NonNull ScannerReaderError error) {
        mAppExecutors.mainThread().execute(() -> callback.onFailToVoidItem(error));
    }


    /**
     * Returns a {@link PagingSource} for loading {@link ProductPreviewItem} objects associated with a specific inventory list.
     * <p>
     * If no filters are applied (as determined by {@link QueryMasterItem#isNoFilterApplied()}),
     * all items in the inventory list are returned. Otherwise, a filtered query is executed using
     * the criteria provided in the {@link QueryMasterItem}.
     * </p>
     *
     * @param inventoryListId The ID of the inventory list to query.
     * @param queryMasterItem Object containing filter parameters for the query.
     * @return A {@link PagingSource} for paginated loading of {@link ProductPreviewItem} objects.
     */
    public PagingSource<Integer, ProductPreviewItem> getInventoryData(int inventoryListId, @NonNull QueryMasterItem queryMasterItem) {
        if (queryMasterItem.isNoFilterApplied()) {
            return mInventoryItemDao.getAllInventoryItemsPaged(inventoryListId);
        } else {
            return mInventoryItemDao.getFilteredInventoryItems(queryMasterItem.getIdent(),
                    queryMasterItem.getBarcode(), queryMasterItem.getAltCode1(),
                    queryMasterItem.getAltCode2(), queryMasterItem.getSalesProgram(),
                    queryMasterItem.getPurchaseProgram(), queryMasterItem.getUnitOfMeasure(),
                    queryMasterItem.getName(), queryMasterItem.getActive(), queryMasterItem.getAccounting(),
                    queryMasterItem.getPrice(), queryMasterItem.getFilterText(), inventoryListId);
        }
    }

    /**
     * Updates an existing {@link InventoryItem} in the database asynchronously.
     * <p>
     * The update operation is performed on a background thread, and the result is posted back
     * to the main thread using the provided callback. If the update is successful, {@code onSuccess()} is called.
     * If the update affects no rows or an exception occurs, {@code onFailure()} is triggered with a {@link ScannerReaderError}.
     * </p>
     *
     * @param inventoryItem The {@link InventoryItem} to update.
     * @param callback      The callback used to report success or failure of the update operation.
     */
    @Override
    public void updateInventoryItem(@NonNull InventoryItem inventoryItem, @NonNull IOnInventoryItemUpdatedCallback callback) {
        Runnable updateTask = () -> {
            try {
                int rowsUpdated = mInventoryItemDao.updateInventoryItem(inventoryItem);
                if (rowsUpdated > 0) {
                    mAppExecutors.mainThread().execute(callback::onSuccess);
                } else {
                    Context context = ScannerReaderApplication.getAppContext();
                    ScannerReaderError error = new ScannerReaderError(context.getString(R.string.update_failed_title), context.getString(R.string.update_failed_subtitle));
                    mAppExecutors.mainThread().execute(() -> callback.onFailure(error));
                }
            } catch (Exception e) {
                ScannerReaderError error = new ScannerReaderError(ScannerReaderApplication.getAppContext().getString(R.string.database_error_title), e.getMessage());
                mAppExecutors.mainThread().execute(() -> callback.onFailure(error));
            }
        };
        mAppExecutors.diskIO().execute(updateTask);
    }

    /**
     * Deletes all inventory data from the database and resets any related counters (e.g., auto-increment values),
     * executing the operation asynchronously on a background thread.
     * <p>
     * If the operation completes successfully, {@code onInventoryDataDeleted()} is called on the main thread.
     * If an exception occurs during deletion, {@code onDeleteInventoryDataFailed()} is called with a
     * {@link ScannerReaderError} containing the error details.
     * </p>
     *
     * @param callback Callback interface used to notify the result of the delete operation.
     */
    @Override
    public void deleteInventoryData(@NonNull IOnInventoryDataDeletedCallback callback) {
        Runnable runnable = () -> {
            try {
                mInventoryItemDao.deleteAndRestartAllInventoryData();
                mAppExecutors.mainThread().execute(callback::onInventoryDataDeleted);
            } catch (Exception e) {
                mAppExecutors.mainThread().execute(() ->
                        callback.onDeleteInventoryDataFailed(new ScannerReaderError(ScannerReaderApplication.getAppContext().getString(R.string.delete_data_fail_title), e.getMessage())));
            }
        };
        mAppExecutors.diskIO().execute(runnable);
    }

    /**
     * Exports all inventory data from the database asynchronously and serializes it to JSON.
     * <p>
     * If inventory data exists, it is converted to a JSON string using {@link Gson} and passed to
     * {@link #exportFile(IOnDataLoadedCallback, String)} for writing to disk. If no data is found or
     * an error occurs during the export process, {@code postDataLoadingFailure()} is invoked with an
     * appropriate {@link ScannerReaderError}.
     * </p>
     *
     * @param callback Callback used to notify success or failure of the data export operation.
     */
    @Override
    public void exportData(@NonNull IOnDataLoadedCallback callback) {
        Runnable runnable = () -> {
            try {
                List<InventoryExportItem> inventoryExportItems = mInventoryItemDao.getAllInventoryData();
                if (CollectionUtils.isEmpty(inventoryExportItems)) {
                    postDataLoadingFailure(callback, R.string.no_products_error, "");
                } else {
                    Gson gson = new GsonBuilder()
                            .excludeFieldsWithoutExposeAnnotation()
                            .create();

                    String exportJson = gson.toJson(inventoryExportItems);
                    exportFile(callback, exportJson);
                }
            } catch (Exception e) {
                postDataLoadingFailure(callback, R.string.database_error_title, e.getMessage());
            }
        };
        mAppExecutors.diskIO().execute(runnable);
    }

    /**
     * Checks if there is at least one inventory item in the database.
     * Executes asynchronously and reports result on the main thread.
     *
     * @param callback Callback to return the result or error.
     */
    @Override
    public void checkIfAnyInventoryItemExists(@NonNull ICheckInventoryItemExistsCallback callback) {
        Runnable runnable = () -> {
            try {
                boolean exists = mInventoryItemDao.checkIfAnyInventoryItemExists();
                mAppExecutors.mainThread().execute(() -> callback.onResult(exists));
            } catch (Exception e) {
                ScannerReaderError error = new ScannerReaderError(e.getMessage());
                mAppExecutors.mainThread().execute(() -> callback.onError(error));
            }
        };
        mAppExecutors.diskIO().execute(runnable);
    }

    /**
     * Exports JSON content to a file in the public Downloads/POPIS folder.
     * Ensures only one file exists in that folder.
     */
    private void exportFile(@NonNull IOnDataLoadedCallback callback, @NonNull String jsonContent) {
        try {
            Context context = ScannerReaderApplication.getAppContext();
            ContentResolver resolver = context.getContentResolver();

            final String folderName = "POPIS";

            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, Utils.getExportFileName());
            values.put(MediaStore.MediaColumns.MIME_TYPE, "application/json");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/" + folderName);

            Uri collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);

            String selection = MediaStore.MediaColumns.RELATIVE_PATH + "=?";
            String[] selectionArgs = {Environment.DIRECTORY_DOWNLOADS + "/" + folderName + "/"};
            resolver.delete(collection, selection, selectionArgs);

            Uri uri = resolver.insert(collection, values);
            if (uri == null) {
                throw new IOException(context.getString(R.string.export_file_create_failed));
            }

            try (OutputStream out = resolver.openOutputStream(uri)) {
                if (out == null) {
                    throw new IOException(context.getString(R.string.export_file_open_failed));
                }
                out.write(jsonContent.getBytes(StandardCharsets.UTF_8));
            }

            mAppExecutors.mainThread().execute(callback::onItemsLoaded);

        } catch (IOException e) {
            postDataLoadingFailure(callback, R.string.export_data_fail_title, e.getMessage());
        }
    }


    /**
     * Posts a failure callback on the main thread with a {@link ScannerReaderError} containing
     * a localized title and a description message.
     *
     * @param callback    The callback to notify about the failure.
     * @param title       Resource ID of the error title string.
     * @param description Detailed error message to include.
     */
    private void postDataLoadingFailure(
            InventoryItemDataSource.IOnDataLoadedCallback callback,
            @StringRes int title,
            String description
    ) {
        mAppExecutors.mainThread().execute(() -> callback.onFailToLoadItems(
                new ScannerReaderError(
                        ScannerReaderApplication.getAppContext().getString(title),
                        description
                )
        ));
    }
}