package com.metalac.scanner.app.view.inventorylist.contract;

import androidx.annotation.NonNull;

import com.metalac.scanner.app.models.InventoryList;
import com.metalac.scanner.app.view.BasePresenter;
import com.metalac.scanner.app.view.BaseView;
import com.metalac.scanner.app.view.ScannerReaderError;

import java.util.List;

public interface InventoryListContract {

    interface View extends BaseView {
        void showInventoryLists(List<Object> inventoryLists, boolean scrollToLast);

        void showNoData();

        void showErrorDialog(@NonNull ScannerReaderError scannerReaderError);

        void gotoInventory();
    }

    interface Presenter extends BasePresenter {
        void loadInventoryLists();

        void addInventoryList(String listName);

        void setCurrentList(InventoryList inventoryList);
    }
}
