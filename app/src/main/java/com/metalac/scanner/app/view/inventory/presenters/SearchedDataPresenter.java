package com.metalac.scanner.app.view.inventory.presenters;

import androidx.annotation.NonNull;

import com.metalac.scanner.app.models.QueryMasterItem;
import com.metalac.scanner.app.data.source.repositories.MasterItemRepository;
import com.metalac.scanner.app.view.BaseView;
import com.metalac.scanner.app.view.inventory.contracts.SearchedDataContract;

public class SearchedDataPresenter implements SearchedDataContract.Presenter {
    private SearchedDataContract.View mView;
    private final MasterItemRepository mMasterItemRepository;
    private QueryMasterItem mQueryMasterItem;

    public SearchedDataPresenter(@NonNull MasterItemRepository mMasterItemRepository) {
        this.mMasterItemRepository = mMasterItemRepository;
    }

    @Override
    public void onAttach(BaseView view) {
        this.mView = (SearchedDataContract.View) view;
    }

    @Override
    public void onDetach() {
        this.mView = null;
    }

    /**
     * Loads a filtered list of master items based on the provided {@link QueryMasterItem}.
     * <p>
     * If no previous query exists, the given query is saved. If the view is available,
     * it triggers data loading via {@link MasterItemRepository#getMasterData}, and the
     * results are passed to the view for display.
     *
     * @param queryMasterItem The filtering criteria used to load the master items.
     */
    @Override
    public void loadItems(QueryMasterItem queryMasterItem) {
        if (mQueryMasterItem == null) {
            mQueryMasterItem = queryMasterItem;
        }
        if (mView == null) return;
        mView.showMasterData(mMasterItemRepository.getMasterData(mQueryMasterItem));
    }

    /**
     * Filters master items by name using the current {@link QueryMasterItem}.
     * <p>
     * If no query exists, shows a retry dialog. Otherwise, updates the query with the new
     * name filter and reloads the data.
     *
     * @param nameFilter The text used to filter items by name.
     */
    @Override
    public void searchByName(String nameFilter) {
        if (mQueryMasterItem == null) {
            mView.showTryAgainDialog();
            return;
        }
        mQueryMasterItem = mQueryMasterItem.toBuilder()
                .setFilterText(nameFilter.trim())
                .build();
        loadItems(mQueryMasterItem);
    }
}
