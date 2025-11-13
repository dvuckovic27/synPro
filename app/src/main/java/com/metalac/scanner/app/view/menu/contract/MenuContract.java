package com.metalac.scanner.app.view.menu.contract;

import androidx.annotation.NonNull;

import com.metalac.scanner.app.view.BasePresenter;
import com.metalac.scanner.app.view.BaseView;
import com.metalac.scanner.app.view.ScannerReaderError;

public interface MenuContract {
    interface View extends BaseView {
        void showStoreCodeChangeDialog(boolean hasInventoryItems);

        void showErrorDialog(@NonNull ScannerReaderError scannerReaderError);
    }

    interface Presenter extends BasePresenter {
        void onChangeStoreCodeRequested();
    }
}
