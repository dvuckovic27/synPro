package com.metalac.scanner.app.data.source.db;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagingSource;

import com.google.gson.Gson;
import com.metalac.scanner.app.executors.AppExecutors;
import com.metalac.scanner.app.helpers.DateHelper;
import com.metalac.scanner.app.data.source.PrefManager;
import com.metalac.scanner.app.R;
import com.metalac.scanner.app.ScannerReaderApplication;
import com.metalac.scanner.app.utils.Utils;
import com.metalac.scanner.app.data.source.interfaces.MasterItemDataSource;
import com.metalac.scanner.app.models.DamageInfo;
import com.metalac.scanner.app.models.MasterItem;
import com.metalac.scanner.app.models.ProductsInfoModel;
import com.metalac.scanner.app.models.QueryMasterItem;
import com.metalac.scanner.app.data.source.db.dao.MasterItemDao;
import com.metalac.scanner.app.view.ScannerReaderError;
import com.metalac.scanner.app.view.inventory.interfaces.ILoadDamageDescriptionCallback;
import com.metalac.scanner.app.view.inventory.interfaces.ILoadDamageInfoCallback;
import com.google.android.gms.common.util.CollectionUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.Consumer;

/**
 * Local data source for managing {@link MasterItem} entities in the local database.
 * Handles reading from file, validation, and database operations asynchronously.
 */
public class MasterItemLocalDataSource implements MasterItemDataSource {

    private static volatile MasterItemLocalDataSource INSTANCE;

    private final MasterItemDao mMasterItemDao;
    private final AppExecutors mAppExecutors;

    private MasterItemLocalDataSource(@NonNull AppExecutors appExecutors, @NonNull MasterItemDao masterItemDao) {
        this.mAppExecutors = appExecutors;
        this.mMasterItemDao = masterItemDao;
    }

    /**
     * Retrieves the singleton instance of the data source.
     *
     * @param appExecutors  Executor utility
     * @param masterItemDao DAO for {@link MasterItem}
     * @return Singleton instance
     */
    public static MasterItemLocalDataSource getInstance(@NonNull AppExecutors appExecutors, @NonNull MasterItemDao masterItemDao) {
        if (INSTANCE == null) {
            synchronized (MasterItemLocalDataSource.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MasterItemLocalDataSource(appExecutors, masterItemDao);
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Loads and synchronizes master item data from a given JSON file URI.
     * <p>
     * The method performs the following steps:
     * <ul>
     *     <li>Validates the file name format from the given URI.</li>
     *     <li>Checks if the store code from the file matches the device store code.</li>
     *     <li>Parses the file content into {@link ProductsInfoModel}.</li>
     *     <li>Validates parsed data for master items and damage info.</li>
     *     <li>
     *         If {@code storeCodeChanged} is {@code true}, all existing master items and damage info
     *         are cleared before inserting new data, to avoid retaining stale products from the previous store.
     *     </li>
     *     <li>Inserts or upserts new master items and damage info into the database.</li>
     *     <li>On success or failure, invokes the callback on the main thread.</li>
     * </ul>
     *  @param uri              The URI of the JSON file to load.
     *
     * @param callback Callback to notify about success or failure.
     */
    @Override
    public void loadAndSyncFromFile(@NonNull Uri uri, ISyncMasterItemsCallback callback) {
        mAppExecutors.diskIO().execute(() -> {
            Context context = ScannerReaderApplication.getAppContext();

            try {
                String fileName = getFileNameFromUri(context, uri);
                if (!isValidFileName(fileName)) {
                    postFailure(callback, context.getString(R.string.invalid_filename_format));
                    return;
                }

                String fileStoreCode = Utils.getFileStoreCode(fileName);
                String deviceStoreCode = PrefManager.getDeviceStoreCode();

                if (!deviceStoreCode.equals(fileStoreCode)) {
                    postFailure(callback,
                            context.getString(R.string.invalid_master_data_subtitle, deviceStoreCode, fileStoreCode));
                    return;
                }

                String jsonContent = readFileContent(context, uri);
                if (!isValidJsonContent(jsonContent, callback, context)) {
                    return;
                }

                ProductsInfoModel model = parseMasterData(jsonContent);
                if (!isValidParsedData(model, callback, context)) {
                    return;
                }

                // Sync damage info
                List<DamageInfo> damageInfo = model.getDamageInfo();
                if (!isValidDamageInfo(damageInfo, callback, context)) {
                    return;
                }

                // Insert or upsert new data
                mMasterItemDao.upsertDamageInfo(damageInfo);
                mMasterItemDao.upsertAll(model.getMasterItems());

                PrefManager.setHasMasterData(true);

                mAppExecutors.mainThread().execute(() ->
                        callback.onSuccess(DateHelper.formatDateToString(new Date()))
                );
            } catch (Exception e) {
                postFailure(callback, "");
            }
        });
    }

    /**
     * Validates JSON content read from the file.
     */
    private boolean isValidJsonContent(String jsonContent, MasterItemDataSource.ISyncMasterItemsCallback callback, Context context) {
        if (jsonContent == null) {
            postFailure(callback,
                    context.getString(R.string.invalid_master_format_title),
                    context.getString(R.string.invalid_master_format_subtitle));
            return false;
        }
        return true;
    }

    /**
     * Validates the parsed {@link ProductsInfoModel}.
     */
    private boolean isValidParsedData(ProductsInfoModel model, MasterItemDataSource.ISyncMasterItemsCallback callback, Context context) {
        if (model == null || model.getMasterItems() == null || model.getMasterItems().isEmpty()) {
            postFailure(callback,
                    context.getString(R.string.invalid_master_format_title),
                    context.getString(R.string.invalid_master_format_subtitle));
            return false;
        }
        return true;
    }

    /**
     * Validates the list of {@link DamageInfo}.
     */
    private boolean isValidDamageInfo(List<DamageInfo> damageInfo, MasterItemDataSource.ISyncMasterItemsCallback callback, Context context) {
        if (damageInfo == null || damageInfo.isEmpty()) {
            postFailure(callback,
                    context.getString(R.string.invalid_damage_data_title),
                    context.getString(R.string.invalid_damage_data_subtitle));
            return false;
        }
        return true;
    }


    /**
     * Finds a {@link MasterItem} by its barcode asynchronously and returns it through the callback.
     *
     * @param barcode               Barcode to search for.
     * @param loadByBarcodeCallback Callback to receive the result or error.
     */
    @Override
    public void loadItemByBarcode(String barcode, @NonNull MasterItemDataSource.ILoadMasterItemCallback loadByBarcodeCallback) {
        loadItemAsync(() -> mMasterItemDao.getByBarcode(barcode), loadByBarcodeCallback);
    }

    /**
     * Finds a {@link MasterItem} by its alternative code 1 asynchronously and returns it through the callback.
     *
     * @param altCode1              Alternative code to search for.
     * @param loadByBarcodeCallback Callback to receive the result or error.
     */
    @Override
    public void loadItemByAltCode1(int altCode1, @NonNull MasterItemDataSource.ILoadMasterItemCallback loadByBarcodeCallback) {
        loadItemAsync(() -> mMasterItemDao.getByAltCode1(altCode1), loadByBarcodeCallback);
    }

    /**
     * Loads a {@link MasterItem} asynchronously using the given supplier function.
     *
     * @param fetchItem Supplier function to fetch the MasterItem (e.g., DAO call).
     * @param callback  Callback to deliver the result or an error.
     */
    private void loadItemAsync(Supplier<MasterItem> fetchItem, MasterItemDataSource.ILoadMasterItemCallback callback) {
        mAppExecutors.diskIO().execute(() -> {
            Context context = ScannerReaderApplication.getAppContext();
            try {
                MasterItem masterItem = fetchItem.get();
                if (masterItem == null) {
                    postMasterItemLoadFailure(callback, context);
                } else {
                    mAppExecutors.mainThread().execute(() -> callback.onMasterItemLoaded(masterItem));
                }
            } catch (Exception e) {
                postMasterItemLoadFailure(callback, context);
            }
        });
    }

    /**
     * Posts a master item load failure to the callback on the main thread.
     *
     * @param callback Callback to notify.
     * @param context  Application context.
     */
    private void postMasterItemLoadFailure(MasterItemDataSource.ILoadMasterItemCallback callback, Context context) {
        mAppExecutors.mainThread().execute(() ->
                callback.onMasterItemLoadFailed(new ScannerReaderError(context.getString(R.string.no_master_item))));
    }


    /**
     * Posts a failure result to the callback with a single message.
     * The title is automatically set to a generic "error" string from resources.
     *
     * @param callback Callback to notify
     * @param message  Error message
     */
    private void postFailure(ISyncMasterItemsCallback callback, String message) {
        mAppExecutors.mainThread().execute(() -> callback.onFailure(new ScannerReaderError(message)));
    }

    /**
     * Posts a failure result to the callback with a title and subtitle.
     *
     * @param callback Callback to notify
     * @param title    Error title
     * @param subtitle Error subtitle
     */
    private void postFailure(ISyncMasterItemsCallback callback, String title, String subtitle) {
        mAppExecutors.mainThread().execute(() -> callback.onFailure(new ScannerReaderError(title, subtitle)));
    }

    /**
     * Retrieves the file name from a content URI.
     *
     * @param context Context used to access content resolver
     * @param uri     URI pointing to a file
     * @return File name or null if not found
     */
    @Nullable
    private String getFileNameFromUri(@NonNull Context context, @NonNull Uri uri) {
        String result = null;
        String content = "content";

        if (content.equals(uri.getScheme())) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                }
            }
        }

        if (result == null) {
            result = uri.getLastPathSegment();
        }

        return result;
    }

    /**
     * Reads the full content of a file from the given URI.
     *
     * @param context Context used to open the input stream
     * @param uri     URI pointing to the file
     * @return File content as a string, or null if an error occurs
     */
    @Nullable
    private String readFileContent(Context context, Uri uri) {
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();

        } catch (Exception ignore) {
            return null;
        }
    }

    /**
     * Parses a JSON string into a list of {@link MasterItem} objects.
     *
     * @param jsonContent JSON string representing a list of master items
     * @return List of {@link MasterItem} or empty list if parsing fails
     */
    @Nullable
    private ProductsInfoModel parseMasterData(String jsonContent) {
        try {
            Gson gson = new Gson();
            return gson.fromJson(jsonContent, ProductsInfoModel.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Validates whether the given file name starts with "MAT" followed by exactly 9 digits
     * and ends with ".json". Characters between the 9 digits and ".json" are allowed but not required.
     * <p>
     * Examples of valid file names:
     * "MAT123456789.json", "MAT123456789_foo.json", "MAT123456789_20240115.json"
     *
     * @param fileName the file name to validate
     * @return true if the file name matches the format, false otherwise or if null
     */
    public static boolean isValidFileName(@Nullable String fileName) {
        if (fileName == null) {
            return false;
        }

        return fileName.matches("^MAT\\d{9}.*\\.json$");
    }

    public PagingSource<Integer, MasterItem> getMasterData(QueryMasterItem queryMasterItem) {
        if (queryMasterItem.isNoFilterApplied()) {
            return mMasterItemDao.getAllPaged();
        } else {
            return mMasterItemDao.getFilteredItems(queryMasterItem.getIdent(),
                    queryMasterItem.getBarcode(), queryMasterItem.getAltCode1(),
                    queryMasterItem.getAltCode2(), queryMasterItem.getSalesProgram(),
                    queryMasterItem.getPurchaseProgram(), queryMasterItem.getUnitOfMeasure(),
                    queryMasterItem.getName(), queryMasterItem.getActive(), queryMasterItem.getAccounting(),
                    queryMasterItem.getPrice(), queryMasterItem.getFilterText());
        }
    }

    /**
     * Fetches the list of available unit of measure values.
     *
     * @param callback The callback interface that will be used to notify the result of the operation.
     *                 It will either invoke {@link ILoadUnitOfMeasureCallback#onUnitOfMeasureLoaded}
     *                 with the list of units of measure, or {@link ILoadUnitOfMeasureCallback#onFailed}
     *                 if an error occurs or the list is empty.
     */
    @Override
    public void getUnitOfMeasureList(ILoadUnitOfMeasureCallback callback) {
        Runnable runnable = () -> {
            try {
                ArrayList<String> unitOfMeasureLis = new ArrayList<>(mMasterItemDao.getUniteOfMeasure());
                if (CollectionUtils.isEmpty(unitOfMeasureLis)) {
                    mAppExecutors.mainThread().execute(() -> callback.onFailed(new ScannerReaderError("")));
                } else {
                    mAppExecutors.mainThread().execute(() -> callback.onUnitOfMeasureLoaded(unitOfMeasureLis));
                }
            } catch (Exception ignore) {
                mAppExecutors.mainThread().execute(() -> callback.onFailed(new ScannerReaderError("")));
            }
        };
        mAppExecutors.diskIO().execute(runnable);
    }

    /**
     * Retrieves the list of {@link DamageInfo} items from the local database.
     * <p>
     * If the list is empty or an error occurs during the fetch, the failure callback is triggered.
     * Otherwise, the loaded list is returned via the success callback.
     * </p>
     *
     * @param callback the {@link ILoadDamageInfoCallback} to receive the result of the operation.
     */
    @Override
    public void getDamageInfoList(ILoadDamageInfoCallback callback) {
        executeBackgroundTask(
                () -> new ArrayList<>(mMasterItemDao.getDamageInfo()),
                result -> {
                    if (CollectionUtils.isEmpty(result)) {
                        callback.onDamageInfoLoadFailed(new ScannerReaderError(""));
                    } else {
                        callback.onDamageInfoLoaded(result);
                    }
                },
                () -> callback.onDamageInfoLoadFailed(new ScannerReaderError(""))
        );
    }

    /**
     * Retrieves the damage description string for a given damage code.
     * <p>
     * If no description is found or an error occurs, the failure callback is triggered.
     * Otherwise, the description is returned via the success callback.
     * </p>
     *
     * @param code     the damage code used to look up the description.
     * @param callback the {@link ILoadDamageDescriptionCallback} to receive the result of the operation.
     */
    @Override
    public void getDamageDescriptionByCode(String code, ILoadDamageDescriptionCallback callback) {
        executeBackgroundTask(
                () -> mMasterItemDao.getDamageNameByCode(code),
                result -> {
                    if (result == null) {
                        callback.onDamageDescriptionLoadFailed(new ScannerReaderError(""));
                    } else {
                        callback.onDamageDescriptionLoaded(result);
                    }
                },
                () -> callback.onDamageDescriptionLoadFailed(new ScannerReaderError(""))
        );
    }

    /**
     * Fetches a {@link MasterItem} from the database by its ident.
     * <p>
     * The query runs on a background thread,
     * and the result or error is posted to the provided callback.
     *
     * @param ident    The item's ident (non-null).
     * @param callback Callback to receive the result or error.
     */
    @Override
    public void getItemByIdent(@NonNull String ident, ILoadMasterItemCallback callback) {
        Runnable runnable = () -> {
            try {
                MasterItem masterItem = mMasterItemDao.getItemByIdent(ident);
                postResult(masterItem, callback);
            } catch (Exception e) {
                mAppExecutors.mainThread().execute(() ->
                        callback.onMasterItemLoadFailed(new ScannerReaderError(""))
                );
            }
        };

        mAppExecutors.diskIO().execute(runnable);
    }

    /**
     * Retrieves a {@link MasterItem} from the database using the provided alternate ID.
     * <p>
     * The search first checks {@code alt_code_1}, and if no match is found, checks {@code alt_code_2}.
     * The query is executed on a background thread, and the result or any error is delivered
     * via the provided callback on the main thread.
     *
     * @param altId    The alternate ID to search for.
     * @param callback Callback to receive the loaded {@link MasterItem} or an error.
     */
    @Override
    public void getItemByAltId(@NonNull String altId, ILoadMasterItemCallback callback) {
        Runnable runnable = () -> {
            try {
                MasterItem masterItem = mMasterItemDao.getItemByAltId1(altId);
                if (masterItem == null) {
                    masterItem = mMasterItemDao.getItemByAltId2(altId);
                }
                postResult(masterItem, callback);
            } catch (Exception e) {
                mAppExecutors.mainThread().execute(() ->
                        callback.onMasterItemLoadFailed(new ScannerReaderError(""))
                );
            }
        };

        mAppExecutors.diskIO().execute(runnable);
    }

    @Override
    public void changeStoreCode(@NonNull String storeCode, @NonNull StoreCodeChangeCallback storeCodeChangeCallback) {
        Runnable runnable = () -> {
            try {
                mMasterItemDao.clearAllMasterItems();
                mMasterItemDao.clearAllDamageInfo();

                mAppExecutors.mainThread().execute(storeCodeChangeCallback::onStoreCodeChanged);
            } catch (Exception e) {
                mAppExecutors.mainThread().execute(() -> storeCodeChangeCallback.onStoreCodeChangeFailed(new ScannerReaderError(e.getMessage())));
            }
        };

        mAppExecutors.diskIO().execute(runnable);
    }

    /**
     * Posts the result of the MasterItem query back on the main thread.
     * Calls success if a non-null item is found; otherwise calls failure with a generic error.
     *
     * @param finalItem The retrieved MasterItem, or null if none found.
     * @param callback  Callback interface to receive success or failure results.
     */
    private void postResult(MasterItem finalItem, ILoadMasterItemCallback callback) {
        mAppExecutors.mainThread().execute(() -> {
            Context context = ScannerReaderApplication.getAppContext();
            if (finalItem != null) {
                callback.onMasterItemLoaded(finalItem);
            } else {
                callback.onMasterItemLoadFailed(new ScannerReaderError(context.getString(R.string.alternative_search_fail)));
            }
        });
    }


    /**
     * Executes a background task using diskIO executor and posts result to main thread.
     *
     * @param task      The task to execute in the background.
     * @param onSuccess Called with result on success.
     * @param onFailure Called on exception or null/empty result condition.
     * @param <T>       The result type.
     */
    private <T> void executeBackgroundTask(Supplier<T> task, Consumer<T> onSuccess, Runnable onFailure) {
        Runnable runnable = () -> {
            try {
                T result = task.get();
                mAppExecutors.mainThread().execute(() -> onSuccess.accept(result));
            } catch (Exception ignored) {
                mAppExecutors.mainThread().execute(onFailure);
            }
        };
        mAppExecutors.diskIO().execute(runnable);
    }
}