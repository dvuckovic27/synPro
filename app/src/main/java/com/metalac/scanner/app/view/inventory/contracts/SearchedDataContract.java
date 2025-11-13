package com.metalac.scanner.app.view.inventory.contracts;

import androidx.lifecycle.LiveData;
import androidx.paging.PagingData;

import com.metalac.scanner.app.models.MasterItem;
import com.metalac.scanner.app.models.QueryMasterItem;
import com.metalac.scanner.app.view.BasePresenter;
import com.metalac.scanner.app.view.BaseView;

public interface SearchedDataContract {
    interface View extends BaseView {
        void showMasterData(LiveData<PagingData<MasterItem>> masterItems);

        void showTryAgainDialog();
    }

    interface Presenter extends BasePresenter {
        void loadItems(QueryMasterItem queryMasterItem);

        void searchByName(String productName);
    }
}
