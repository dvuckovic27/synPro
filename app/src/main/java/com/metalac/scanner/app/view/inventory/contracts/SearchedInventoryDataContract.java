package com.metalac.scanner.app.view.inventory.contracts;

import androidx.lifecycle.LiveData;
import androidx.paging.PagingData;

import com.metalac.scanner.app.models.ProductPreviewItem;
import com.metalac.scanner.app.models.QueryMasterItem;
import com.metalac.scanner.app.view.BasePresenter;
import com.metalac.scanner.app.view.BaseView;
import com.metalac.scanner.app.view.ScannerReaderError;

public interface SearchedInventoryDataContract {
    interface View extends BaseView {
        void showInventoryData(LiveData<PagingData<ProductPreviewItem>> productPreviewItems);

        void showErrorDialog(ScannerReaderError scannerReaderError);
    }

    interface Presenter extends BasePresenter {
        void loadItems(QueryMasterItem queryMasterItem);

        void searchByName(String productName);

        void voidItem(long inventoryItemId);
    }
}
