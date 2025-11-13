package com.metalac.scanner.app.data.source.interfaces;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.metalac.scanner.app.models.MasterItem;
import com.metalac.scanner.app.view.ScannerReaderError;
import com.metalac.scanner.app.view.inventory.interfaces.ILoadDamageDescriptionCallback;
import com.metalac.scanner.app.view.inventory.interfaces.ILoadDamageInfoCallback;

import java.util.ArrayList;

public interface MasterItemDataSource {

    void loadAndSyncFromFile(@NonNull Uri uri, ISyncMasterItemsCallback callback);

    void loadItemByBarcode(String barcode, @NonNull ILoadMasterItemCallback loadByBarcodeCallback);

    void loadItemByAltCode1(int altCode1, @NonNull MasterItemDataSource.ILoadMasterItemCallback loadMasterItemCallback);

    void getUnitOfMeasureList(ILoadUnitOfMeasureCallback callback);

    void getDamageInfoList(ILoadDamageInfoCallback callback);

    void getDamageDescriptionByCode(String code, ILoadDamageDescriptionCallback callback);

    void getItemByIdent(@NonNull String ident, ILoadMasterItemCallback callback);

    void getItemByAltId(@NonNull String altId, ILoadMasterItemCallback callback);

    void changeStoreCode(@NonNull String storeCode, @NonNull StoreCodeChangeCallback storeCodeChangeCallback);

    interface ISyncMasterItemsCallback {
        void onSuccess(String formattedSyncDate);

        void onFailure(ScannerReaderError error);
    }

    interface ILoadMasterItemCallback {
        void onMasterItemLoaded(@NonNull MasterItem masterItem);

        void onMasterItemLoadFailed(@NonNull ScannerReaderError scannerReaderError);
    }

    interface ILoadUnitOfMeasureCallback {

        void onUnitOfMeasureLoaded(@NonNull ArrayList<String> unitOfMeasureList);

        void onFailed(@NonNull ScannerReaderError scannerReaderError);
    }

    interface StoreCodeChangeCallback {
        void onStoreCodeChanged();

        void onStoreCodeChangeFailed(ScannerReaderError scannerReaderError);
    }
}
