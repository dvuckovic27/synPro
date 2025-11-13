package com.metalac.scanner.app.view.inventorylist.presenter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.metalac.scanner.app.data.source.interfaces.InventoryListDataSource;
import com.metalac.scanner.app.data.source.repositories.InventoryListRepository;
import com.metalac.scanner.app.models.InventoryList;
import com.metalac.scanner.app.models.InventoryListWithCount;
import com.metalac.scanner.app.view.BaseView;
import com.metalac.scanner.app.view.ScannerReaderError;
import com.metalac.scanner.app.view.inventorylist.contract.InventoryListContract;

import java.util.ArrayList;
import java.util.List;

public class InventoryListPresenter implements InventoryListContract.Presenter {
    @Nullable
    private InventoryListContract.View mView;
    private final InventoryListRepository mInventoryListRepository;
    private final ArrayList<InventoryListWithCount> mInventoryLists = new ArrayList<>();

    public InventoryListPresenter(@Nullable InventoryListRepository mInventoryListRepository) {
        this.mInventoryListRepository = mInventoryListRepository;
    }

    @Override
    public void loadInventoryLists() {
        if (mView != null) {
            mView.showProgress();
        }
        if (mInventoryListRepository != null) {
            mInventoryListRepository.getAllInventoryLists(new InventoryListDataSource.ILoadInventoryListsCallback() {
                @Override
                public void onInventoryListsLoaded(@NonNull List<InventoryListWithCount> inventoryLists) {
                    mInventoryLists.addAll(inventoryLists);
                    if (mView != null) {
                        mView.hideProgress();
                        mView.showInventoryLists(new ArrayList<>(mInventoryLists), false);
                    }
                }

                @Override
                public void onFailToLoadInventoryLists(@NonNull ScannerReaderError scannerReaderError) {
                    if (mView != null) {
                        mView.hideProgress();
                        mView.showNoData();
                    }
                }
            });
        }
    }

    @Override
    public void addInventoryList(String listName) {
        if (mInventoryListRepository != null) {
            mInventoryListRepository.addInventoryList(listName, new InventoryListDataSource.IAddInventoryListCallback() {
                @Override
                public void onListAdded(@NonNull InventoryList inventoryList) {
                    mInventoryLists.add(new InventoryListWithCount(inventoryList));
                    if (mView != null) {
                        mView.showInventoryLists(new ArrayList<>(mInventoryLists), true);
                    }
                }

                @Override
                public void onFailToAddList(@NonNull ScannerReaderError scannerReaderError) {
                    if (mView != null) {
                        mView.showErrorDialog(scannerReaderError);
                    }
                }
            });
        }
    }

    @Override
    public void setCurrentList(InventoryList inventoryList) {
        if (mInventoryListRepository != null) {
            mInventoryListRepository.updateInventoryList(inventoryList, new InventoryListDataSource.IUpdateInventoryListCallback() {
                @Override
                public void onInventoryListUpdated() {
                    if (mView != null) {
                        mView.gotoInventory();
                    }
                }

                @Override
                public void onFailToUpdateInventoryList(@NonNull ScannerReaderError scannerReaderError) {
                    if (mView != null) {
                        mView.showErrorDialog(scannerReaderError);
                    }
                }
            });
        }
    }

    @Override
    public void onAttach(BaseView view) {
        this.mView = (InventoryListContract.View) view;
    }

    @Override
    public void onDetach() {
        this.mView = null;
    }
}
