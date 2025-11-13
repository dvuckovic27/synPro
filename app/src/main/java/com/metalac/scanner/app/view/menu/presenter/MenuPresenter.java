package com.metalac.scanner.app.view.menu.presenter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.metalac.scanner.app.data.source.interfaces.InventoryItemDataSource;
import com.metalac.scanner.app.data.source.repositories.InventoryItemRepository;
import com.metalac.scanner.app.view.BaseView;
import com.metalac.scanner.app.view.ScannerReaderError;
import com.metalac.scanner.app.view.menu.contract.MenuContract;

public class MenuPresenter implements MenuContract.Presenter {
    @Nullable
    private MenuContract.View mView;
    @Nullable
    private final InventoryItemRepository mInventoryItemRepository;

    public MenuPresenter(@Nullable InventoryItemRepository mInventoryItemRepository) {
        this.mInventoryItemRepository = mInventoryItemRepository;
    }

    @Override
    public void onChangeStoreCodeRequested() {
        if (mInventoryItemRepository != null) {
            mInventoryItemRepository.checkIfAnyInventoryItemExists(new InventoryItemDataSource.ICheckInventoryItemExistsCallback() {
                @Override
                public void onResult(boolean exists) {
                    if (mView != null) {
                        mView.showStoreCodeChangeDialog(exists);
                    }
                }

                @Override
                public void onError(@NonNull ScannerReaderError scannerReaderError) {
                    if (mView != null) {
                        mView.showErrorDialog(scannerReaderError);
                    }
                }
            });
        }
    }

    @Override
    public void onAttach(BaseView view) {
        this.mView = (MenuContract.View) view;
    }

    @Override
    public void onDetach() {
        this.mView = null;
    }
}
