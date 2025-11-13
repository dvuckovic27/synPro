package com.metalac.scanner.app.view.inventory.presenters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.metalac.scanner.app.R;
import com.metalac.scanner.app.ScannerReaderApplication;
import com.metalac.scanner.app.data.source.interfaces.InventoryItemDataSource;
import com.metalac.scanner.app.data.source.interfaces.InventoryListDataSource;
import com.metalac.scanner.app.data.source.repositories.InventoryItemRepository;
import com.metalac.scanner.app.data.source.repositories.InventoryListRepository;
import com.metalac.scanner.app.models.InventoryList;
import com.metalac.scanner.app.models.QueryMasterItem;
import com.metalac.scanner.app.view.BaseView;
import com.metalac.scanner.app.view.ScannerReaderError;
import com.metalac.scanner.app.view.inventory.contracts.SearchedInventoryDataContract;

public class SearchedInventoryDataPresenter implements SearchedInventoryDataContract.Presenter {
    private InventoryList mInventoryList;
    private SearchedInventoryDataContract.View mView;
    @Nullable
    private final InventoryItemRepository mInventoryItemRepository;
    @Nullable
    private final InventoryListRepository mInventoryListRepository;
    private QueryMasterItem mQueryMasterItem;

    public SearchedInventoryDataPresenter(@Nullable InventoryItemRepository inventoryItemRepository,
                                          @Nullable InventoryListRepository inventoryListRepository) {
        this.mInventoryItemRepository = inventoryItemRepository;
        this.mInventoryListRepository = inventoryListRepository;
    }

    @Override
    public void onAttach(BaseView view) {
        this.mView = (SearchedInventoryDataContract.View) view;
    }

    @Override
    public void onDetach() {
        this.mView = null;
    }

    @Override
    public void loadItems(@NonNull QueryMasterItem queryMasterItem) {
        if (mQueryMasterItem == null) {
            mQueryMasterItem = queryMasterItem;
        }
        if (mInventoryListRepository != null) {
            mInventoryListRepository.getCurrentList(new InventoryListDataSource.ILoadInventoryListCallback() {
                @Override
                public void onInventoryListLoaded(@NonNull InventoryList inventoryList) {
                    mInventoryList = inventoryList;
                    if (mView != null && mInventoryItemRepository != null) {
                        mView.showInventoryData(mInventoryItemRepository.getInventoryData(mInventoryList.getId(), mQueryMasterItem));
                    }
                }

                @Override
                public void onFailToLoadInventoryList(@NonNull ScannerReaderError scannerReaderError) {
                    if (mView != null) {
                        mView.showErrorDialog(scannerReaderError);
                    }
                }
            });
        }

    }

    @Override
    public void searchByName(String nameFilter) {
        if (mQueryMasterItem == null) {
            if (mView != null) {
                mView.showErrorDialog(new ScannerReaderError(ScannerReaderApplication.getAppContext().getString(R.string.invalid_filter)));
            }
            return;
        }

        mQueryMasterItem = mQueryMasterItem.toBuilder()
                .setFilterText(nameFilter.trim())
                .build();
        loadItems(mQueryMasterItem);
    }

    @Override
    public void voidItem(long inventoryItemId) {
        if (mInventoryItemRepository != null) {
            mInventoryItemRepository.voidInventoryItem(inventoryItemId, new InventoryItemDataSource.IVoidItemCallback() {
                @Override
                public void onItemVoided() {
                    //not in use
                }

                @Override
                public void onFailToVoidItem(@NonNull ScannerReaderError scannerReaderError) {
                    if (mView != null) {
                        mView.showErrorDialog(scannerReaderError);
                    }
                }
            });
        }
    }
}
