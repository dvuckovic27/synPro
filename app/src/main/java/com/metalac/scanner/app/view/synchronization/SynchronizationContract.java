package com.metalac.scanner.app.view.synchronization;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.metalac.scanner.app.helpers.DialogHelper;
import com.metalac.scanner.app.view.BasePresenter;
import com.metalac.scanner.app.view.BaseView;
import com.metalac.scanner.app.view.ScannerReaderError;

public interface SynchronizationContract {
    interface View extends BaseView {
        void onSuccessfulSync(String lastSyncDate);

        void onFailedSync(ScannerReaderError scannerReaderError);

        void enableExport(boolean enable);

        void enableDelete(boolean enable);

        void showErrorDialog(@NonNull ScannerReaderError scannerReaderError);

        void createProgressDialog(DialogHelper.DialogMode dialogMode);

        void createSuccessfulDialog(DialogHelper.DialogMode dialogMode);

        void displayLastExportDate(String lastExportDate);
    }

    interface Presenter extends BasePresenter {
        void loadMasterItems(Uri uri);

        void exportData();

        void deleteInventoryData();

        void checkInventoryData();

        void checkInventoryListData();
    }
}
