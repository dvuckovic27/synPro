package com.metalac.scanner.app.view.inventory.presenters;

import androidx.annotation.NonNull;

import com.metalac.scanner.app.data.source.interfaces.MasterItemDataSource;
import com.metalac.scanner.app.data.source.repositories.MasterItemRepository;
import com.metalac.scanner.app.view.BaseView;
import com.metalac.scanner.app.view.ScannerReaderError;
import com.metalac.scanner.app.view.inventory.contracts.SearchContract;
import com.metalac.scanner.app.view.inventory.fragments.SearchFragment;

import java.util.ArrayList;

public class SearchPresenter implements SearchContract.Presenter {

    private SearchContract.View mView;
    private final MasterItemRepository mMasterItemRepository;

    public SearchPresenter(@NonNull MasterItemRepository mMasterItemRepository) {
        this.mMasterItemRepository = mMasterItemRepository;
    }

    /**
     * Retrieves the list of available unit of measures from the repository.
     * - Calls the {@link MasterItemRepository#getUnitOfMeasureList} method to fetch the unit of measure data.
     * - Once the data is successfully loaded, it passes the list to the view via {@link MasterItemDataSource.ILoadUnitOfMeasureCallback#onUnitOfMeasureLoaded}.
     * - In case of an error, it notifies the view by calling {@link MasterItemDataSource.ILoadUnitOfMeasureCallback#onFailed}.
     */
    @Override
    public void getUnitOfMeasure() {
        mMasterItemRepository.getUnitOfMeasureList(new MasterItemDataSource.ILoadUnitOfMeasureCallback() {
            @Override
            public void onUnitOfMeasureLoaded(@NonNull ArrayList<String> unitOfMeasureList) {
                if (mView != null) {
                    mView.onUnitOfMeasureLoaded(unitOfMeasureList);
                }
            }

            @Override
            public void onFailed(@NonNull ScannerReaderError scannerReaderError) {
                if (mView != null) {
                    mView.onUnitOfMeasureFailed();
                }
            }
        });
    }

    @Override
    public void onAttach(BaseView view) {
        this.mView = (SearchFragment) view;
    }

    @Override
    public void onDetach() {
        this.mView = null;
    }
}
