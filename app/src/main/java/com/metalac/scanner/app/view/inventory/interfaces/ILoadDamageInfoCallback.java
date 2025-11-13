package com.metalac.scanner.app.view.inventory.interfaces;

import androidx.annotation.NonNull;

import com.metalac.scanner.app.models.DamageInfo;
import com.metalac.scanner.app.view.ScannerReaderError;

import java.util.ArrayList;

public interface ILoadDamageInfoCallback {
    void onDamageInfoLoaded(@NonNull ArrayList<DamageInfo> damageInfoList);

    void onDamageInfoLoadFailed(@NonNull ScannerReaderError scannerReaderError);
}
