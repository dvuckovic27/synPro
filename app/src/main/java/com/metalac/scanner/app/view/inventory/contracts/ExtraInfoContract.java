package com.metalac.scanner.app.view.inventory.contracts;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.metalac.scanner.app.models.DamageInfo;
import com.metalac.scanner.app.models.InventoryItem;
import com.metalac.scanner.app.models.MasterItem;
import com.metalac.scanner.app.view.BasePresenter;
import com.metalac.scanner.app.view.BaseView;
import com.metalac.scanner.app.view.ScannerReaderError;
import com.metalac.scanner.app.view.inventory.interfaces.ISetResultArgsCallback;

import java.util.ArrayList;

public interface ExtraInfoContract {

    interface View extends BaseView {
        void goBack();

        void showDialogIfUomIsZeroOrLess();

        void showInitialData(InventoryItem inventoryItem);

        void showQuantityWarningDialog(@NonNull MasterItem masterItem, double quantity);

        void showErrorDialog(@NonNull ScannerReaderError scannerReaderError);

        void populateDamageInfo(@NonNull ArrayList<DamageInfo> damageInfo);

        void showNoDamageInfo();

        void configureQuantityInput(int decimalPlaces);
    }

    interface Presenter extends BasePresenter {
        void loadAllMasterData(long inventoryItemId, String masterIdent);

        void validateQuantity(double quantity, String expDate, String damageCode, String note);

        void addItem(@NonNull InventoryItem inventoryItem);

        void updateItem(@Nullable String expDate, @Nullable String damageCode, @Nullable String note);

        void setResultArgs(ISetResultArgsCallback setResultArgsCallback);

        void setExtraInfoItemAdded(boolean extraInfoItemAdded);

        void loadCurrentList();
    }
}
