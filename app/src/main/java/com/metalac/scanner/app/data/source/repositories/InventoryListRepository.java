package com.metalac.scanner.app.data.source.repositories;

import androidx.annotation.NonNull;

import com.metalac.scanner.app.data.source.db.InventoryListLocalDataSource;
import com.metalac.scanner.app.data.source.interfaces.InventoryListDataSource;
import com.metalac.scanner.app.models.InventoryList;

public class InventoryListRepository implements InventoryListDataSource {

    private static InventoryListRepository mInstance = null;
    private final InventoryListLocalDataSource mInventoryListLocalDataSource;

    public static InventoryListRepository getInstance(@NonNull InventoryListLocalDataSource inventoryListLocalDataSource) {
        if (mInstance == null) {
            mInstance = new InventoryListRepository(inventoryListLocalDataSource);
        }
        return mInstance;
    }

    public InventoryListRepository(@NonNull InventoryListLocalDataSource inventoryListLocalDataSource) {
        this.mInventoryListLocalDataSource = inventoryListLocalDataSource;
    }

    @Override
    public void addInventoryList(String inventoryListName, IAddInventoryListCallback callback) {
        mInventoryListLocalDataSource.addInventoryList(inventoryListName, callback);
    }

    @Override
    public void getAllInventoryLists(ILoadInventoryListsCallback callback) {
        mInventoryListLocalDataSource.getAllInventoryLists(callback);
    }

    @Override
    public void getListById(int inventoryListId, ILoadInventoryListCallback callback) {
        mInventoryListLocalDataSource.getListById(inventoryListId, callback);
    }

    @Override
    public void getCurrentList(ILoadInventoryListCallback callback) {
        mInventoryListLocalDataSource.getCurrentList(callback);
    }

    @Override
    public void updateInventoryList(InventoryList inventoryList, IUpdateInventoryListCallback callback) {
        mInventoryListLocalDataSource.updateInventoryList(inventoryList, callback);
    }

    @Override
    public void deleteInventoryListData(@NonNull IOnInventoryListDataDeletedCallback callback) {
        mInventoryListLocalDataSource.deleteInventoryListData(callback);
    }

    @Override
    public void checkIfAnyInventoryListExists(@NonNull ICheckInventoryListExistsCallback callback) {
        mInventoryListLocalDataSource.checkIfAnyInventoryListExists(callback);
    }
}
