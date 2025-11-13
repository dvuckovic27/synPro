package com.metalac.scanner.app.view.inventory.contracts;

import androidx.annotation.NonNull;

import com.metalac.scanner.app.models.InventoryItem;
import com.metalac.scanner.app.models.MasterItem;
import com.metalac.scanner.app.models.ProductPreviewItem;
import com.metalac.scanner.app.view.BasePresenter;
import com.metalac.scanner.app.view.BaseView;
import com.metalac.scanner.app.view.ScannerReaderError;

import java.util.List;

public interface InventoryContract {
    interface View extends BaseView {
        void onMasterItemLoaded(@NonNull MasterItem masterItem, boolean restartQuantity);

        void onMasterItemLoadedByWeightBarcode(@NonNull MasterItem masterItem, double weight);

        void onMasterItemLoadingFailed(@NonNull ScannerReaderError scannerReaderError);

        void showDialogIfUomIsZeroOrLess();

        void showQuantityWarningDialog(@NonNull MasterItem masterItem, double quantity);

        void addItem(@NonNull ProductPreviewItem productPreviewItem);

        void showTryAgainDialog();

        void showErrorDialog(@NonNull ScannerReaderError scannerReaderError);

        void populateInventoryAdapter(@NonNull List<ProductPreviewItem> productPreviewItems);

        void showAdditionalData(@NonNull InventoryItem inventoryItem);

        void displayCurrentListData(String listName);

        void resetAlternativeSearch(boolean collapseView);
    }

    interface Presenter extends BasePresenter {
        void getSearchedData();

        void loadMasterItem(String barcode);

        void handleWeightBarcode(@NonNull String barcode);

        void validateQuantity(double quantity);

        void getInventoryItemList();

        void addInventoryItem(@NonNull InventoryItem inventoryItem);

        void loadAdditionData(long inventoryItemId);

        void setItemId(String itemIdent);

        String getItemIdent();

        void loadCurrentList();

        void getItemByIdent(@NonNull String ident);

        void getItemByAltId(@NonNull String altId);
    }
}
