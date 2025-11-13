package com.metalac.scanner.app.view.configuration.contracts;

import androidx.annotation.NonNull;

import com.metalac.scanner.app.view.BasePresenter;
import com.metalac.scanner.app.view.BaseView;
import com.metalac.scanner.app.view.ScannerReaderError;

public interface ConfigurationContract {

    interface View extends BaseView {
        void navigateToMenu();

        void showErrorDialog(@NonNull ScannerReaderError scannerReaderError);
    }

    interface Presenter extends BasePresenter {
        void changeStoreCode(String storeCode);
    }
}
