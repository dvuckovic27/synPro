package com.metalac.scanner.app.view.configuration.presenters;

import androidx.annotation.Nullable;

import com.metalac.scanner.app.data.source.PrefManager;
import com.metalac.scanner.app.data.source.interfaces.MasterItemDataSource;
import com.metalac.scanner.app.data.source.repositories.MasterItemRepository;
import com.metalac.scanner.app.view.BaseView;
import com.metalac.scanner.app.view.ScannerReaderError;
import com.metalac.scanner.app.view.configuration.contracts.ConfigurationContract;

public class ConfigurationPresenter implements ConfigurationContract.Presenter {
    @Nullable
    private ConfigurationContract.View mView;
    @Nullable
    private final MasterItemRepository mMasterItemRepository;

    public ConfigurationPresenter(@Nullable MasterItemRepository masterItemRepository) {
        this.mMasterItemRepository = masterItemRepository;
    }

    @Override
    public void changeStoreCode(String storeCode) {
        if (mMasterItemRepository != null) {
            mMasterItemRepository.changeStoreCode(storeCode, new MasterItemDataSource.StoreCodeChangeCallback() {
                @Override
                public void onStoreCodeChanged() {
                    PrefManager.setDeviceStoreCode(storeCode);
                    PrefManager.setHasMasterData(false);
                    if (mView != null) {
                        mView.navigateToMenu();
                    }
                }

                @Override
                public void onStoreCodeChangeFailed(ScannerReaderError scannerReaderError) {
                    if (mView != null) {
                        mView.showErrorDialog(scannerReaderError);
                    }
                }
            });
        }
    }

    @Override
    public void onAttach(BaseView view) {
        this.mView = (ConfigurationContract.View) view;
    }

    @Override
    public void onDetach() {
        this.mView = null;
    }
}
