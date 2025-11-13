package com.metalac.scanner.app.view.inventory.interfaces;

import androidx.annotation.NonNull;

import com.metalac.scanner.app.view.ScannerReaderError;

public interface ILoadDamageDescriptionCallback {
    void onDamageDescriptionLoaded(@NonNull String description);

    void onDamageDescriptionLoadFailed(@NonNull ScannerReaderError scannerReaderError);
}
