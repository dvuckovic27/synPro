package com.metalac.scanner.app.data.source.repositories;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingLiveData;

import com.metalac.scanner.app.utils.Utils;
import com.metalac.scanner.app.data.source.db.InventoryItemLocalDataSource;
import com.metalac.scanner.app.data.source.interfaces.InventoryItemDataSource;
import com.metalac.scanner.app.models.InventoryItem;
import com.metalac.scanner.app.models.ProductPreviewItem;
import com.metalac.scanner.app.models.QueryMasterItem;

public class InventoryItemRepository implements InventoryItemDataSource {

    private static InventoryItemRepository mInstance = null;
    private final InventoryItemLocalDataSource inventoryItemLocalDataSource;

    public static InventoryItemRepository getInstance(@NonNull InventoryItemLocalDataSource inventoryItemLocalDataSource) {
        if (mInstance == null) {
            mInstance = new InventoryItemRepository(inventoryItemLocalDataSource);
        }
        return mInstance;
    }

    public InventoryItemRepository(@NonNull InventoryItemLocalDataSource inventoryItemLocalDataSource) {
        this.inventoryItemLocalDataSource = inventoryItemLocalDataSource;
    }

    @Override
    public void addInventoryItem(@NonNull InventoryItem inventoryItem, @NonNull IAddInventoryItemCallback inventoryItemCallback) {
        inventoryItemLocalDataSource.addInventoryItem(inventoryItem, inventoryItemCallback);
    }

    @Override
    public void getInventoryItemList(int inventoryListId, @NonNull ILoadInventoryItemsCallback iLoadInventoryItemsCallback) {
        inventoryItemLocalDataSource.getInventoryItemList(inventoryListId, iLoadInventoryItemsCallback);
    }

    @Override
    public void getInventoryItemById(long id, @NonNull ILoadInventoryItemCallback iLoadInventoryItemByIdCallback) {
        inventoryItemLocalDataSource.getInventoryItemById(id, iLoadInventoryItemByIdCallback);
    }

    @Override
    public void voidInventoryItem(long id, @NonNull IVoidItemCallback iVoidItemCallback) {
        inventoryItemLocalDataSource.voidInventoryItem(id, iVoidItemCallback);
    }

    public LiveData<PagingData<ProductPreviewItem>> getInventoryData(int inventoryListId, @NonNull QueryMasterItem queryMasterItem) {
        Pager<Integer, ProductPreviewItem> pager = new Pager<>(
                new PagingConfig(Utils.PAGE_SIZE),
                () -> inventoryItemLocalDataSource.getInventoryData(inventoryListId, queryMasterItem)
        );
        return PagingLiveData.getLiveData(pager);
    }

    @Override
    public void updateInventoryItem(@NonNull InventoryItem inventoryItem, @NonNull IOnInventoryItemUpdatedCallback iOnInventoryItemUpdatedCallback) {
        inventoryItemLocalDataSource.updateInventoryItem(inventoryItem, iOnInventoryItemUpdatedCallback);
    }

    @Override
    public void deleteInventoryData(@NonNull IOnInventoryDataDeletedCallback callback) {
        inventoryItemLocalDataSource.deleteInventoryData(callback);
    }

    @Override
    public void exportData(@NonNull IOnDataLoadedCallback callback) {
        inventoryItemLocalDataSource.exportData(callback);
    }

    @Override
    public void checkIfAnyInventoryItemExists(@NonNull ICheckInventoryItemExistsCallback callback) {
        inventoryItemLocalDataSource.checkIfAnyInventoryItemExists(callback);
    }

}
