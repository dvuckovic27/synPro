package com.metalac.scanner.app.data.source.interfaces;

import androidx.annotation.NonNull;

import com.metalac.scanner.app.models.InventoryList;
import com.metalac.scanner.app.models.InventoryListWithCount;
import com.metalac.scanner.app.view.ScannerReaderError;

import java.util.List;

public interface InventoryListDataSource {

    void addInventoryList(String inventoryListName, IAddInventoryListCallback callback);

    void getAllInventoryLists(ILoadInventoryListsCallback callback);

    void getListById(int inventoryListId, ILoadInventoryListCallback callback);

    void getCurrentList(ILoadInventoryListCallback callback);

    void updateInventoryList(InventoryList inventoryList, IUpdateInventoryListCallback callback);

    void deleteInventoryListData(@NonNull IOnInventoryListDataDeletedCallback callback);

    void checkIfAnyInventoryListExists(@NonNull ICheckInventoryListExistsCallback callback);


    interface IAddInventoryListCallback {
        void onListAdded(@NonNull InventoryList inventoryList);

        void onFailToAddList(@NonNull ScannerReaderError scannerReaderError);
    }

    interface ILoadInventoryListsCallback {
        void onInventoryListsLoaded(@NonNull List<InventoryListWithCount> inventoryLists);

        void onFailToLoadInventoryLists(@NonNull ScannerReaderError scannerReaderError);
    }

    interface ILoadInventoryListCallback {
        void onInventoryListLoaded(@NonNull InventoryList inventoryList);

        void onFailToLoadInventoryList(@NonNull ScannerReaderError scannerReaderError);
    }

    interface IUpdateInventoryListCallback {
        void onInventoryListUpdated();

        void onFailToUpdateInventoryList(@NonNull ScannerReaderError scannerReaderError);
    }

    interface IOnInventoryListDataDeletedCallback {
        void onInventoryListDataDeleted();

        void onDeleteInventoryListDataFailed(@NonNull ScannerReaderError scannerReaderError);
    }

    interface ICheckInventoryListExistsCallback {
        void onResult(boolean exists);

        void onError(@NonNull ScannerReaderError scannerReaderError);
    }
}
