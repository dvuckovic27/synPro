package com.metalac.scanner.app.data.source.interfaces;

import androidx.annotation.NonNull;

import com.metalac.scanner.app.models.InventoryItem;
import com.metalac.scanner.app.models.ProductPreviewItem;
import com.metalac.scanner.app.view.ScannerReaderError;

import java.util.List;

public interface InventoryItemDataSource {

    void addInventoryItem(InventoryItem inventoryItem, @NonNull IAddInventoryItemCallback inventoryItemCallback);

    void getInventoryItemList(int inventoryListId, @NonNull ILoadInventoryItemsCallback iLoadInventoryItemsCallback);

    void getInventoryItemById(long id, @NonNull ILoadInventoryItemCallback callback);

    void voidInventoryItem(long id, @NonNull IVoidItemCallback iVoidItemCallback);

    void updateInventoryItem(@NonNull InventoryItem inventoryItem, @NonNull IOnInventoryItemUpdatedCallback callback);

    void deleteInventoryData(@NonNull IOnInventoryDataDeletedCallback callback);

    void exportData(@NonNull IOnDataLoadedCallback callback);

    void checkIfAnyInventoryItemExists(@NonNull ICheckInventoryItemExistsCallback callback);

    interface IAddInventoryItemCallback {
        void onSuccess(@NonNull ProductPreviewItem productPreviewItem);

        void onFailure(@NonNull ScannerReaderError scannerReaderError);
    }

    interface ILoadInventoryItemsCallback {

        void onSuccess(@NonNull List<ProductPreviewItem> productPreviewItems);

        void onFailure(@NonNull ScannerReaderError scannerReaderError);
    }

    interface ILoadInventoryItemCallback {
        void onInventoryItemLoaded(@NonNull InventoryItem inventoryItem);

        void onInventoryItemLoadFailed(@NonNull ScannerReaderError scannerReaderError);
    }

    interface IOnInventoryItemUpdatedCallback {

        void onSuccess();

        void onFailure(@NonNull ScannerReaderError scannerReaderError);
    }

    interface IOnInventoryDataDeletedCallback {
        void onInventoryDataDeleted();

        void onDeleteInventoryDataFailed(@NonNull ScannerReaderError scannerReaderError);
    }

    interface IOnDataLoadedCallback {
        void onItemsLoaded();

        void onFailToLoadItems(@NonNull ScannerReaderError scannerReaderError);
    }

    interface ICheckInventoryItemExistsCallback {
        void onResult(boolean exists);

        void onError(@NonNull ScannerReaderError scannerReaderError);
    }

    interface IVoidItemCallback {
        void onItemVoided();

        void onFailToVoidItem(@NonNull ScannerReaderError scannerReaderError);
    }
}
