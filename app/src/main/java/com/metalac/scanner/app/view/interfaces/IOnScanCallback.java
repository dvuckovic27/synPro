package com.metalac.scanner.app.view.interfaces;

import androidx.annotation.NonNull;

public interface IOnScanCallback {
    void onBarcodeScanResult(@NonNull String barcode);

    void onWeightBarcodeScanResult(@NonNull String barcode);
}
