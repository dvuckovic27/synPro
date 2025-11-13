package com.metalac.scanner.app.view.inventory.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.LiveData;
import androidx.navigation.Navigation;
import androidx.paging.LoadState;
import androidx.paging.PagingData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.metalac.scanner.app.helpers.DialogHelper;
import com.metalac.scanner.app.Injection;
import com.metalac.scanner.app.R;
import com.metalac.scanner.app.helpers.SimpleTextWatcher;
import com.metalac.scanner.app.utils.Utils;
import com.metalac.scanner.app.models.MasterItem;
import com.metalac.scanner.app.models.QueryMasterItem;
import com.metalac.scanner.app.databinding.FragmentSearchedDataBinding;
import com.metalac.scanner.app.view.BaseFragment;
import com.metalac.scanner.app.view.BasePresenter;
import com.metalac.scanner.app.view.BaseView;
import com.metalac.scanner.app.view.MainActivity;
import com.metalac.scanner.app.view.inventory.contracts.SearchedDataContract;
import com.metalac.scanner.app.view.inventory.presenters.SearchedDataPresenter;
import com.metalac.scanner.app.view.inventory.adapter.SearchedDataAdapter;

import kotlin.Unit;

public class SearchedDataFragment extends BaseFragment implements SearchedDataContract.View {
    //Constant value for the top position in a RecyclerView.
    private static final int TOP_POSITION = 0;
    //Constant value representing the delay (in milliseconds) for filtering by name.
    private static final int FILTER_BY_NAME_DELAY = 300;
    private FragmentSearchedDataBinding mBinding;
    private SearchedDataAdapter mSearchedDataAdapter;
    private SearchedDataPresenter mPresenter;
    private boolean isFilterApplied;

    @Override
    protected void onCreateView(ViewBinding viewBinding, BasePresenter basePresenter) {
        QueryMasterItem queryMasterItem;
        mBinding = (FragmentSearchedDataBinding) viewBinding;
        mPresenter = (SearchedDataPresenter) basePresenter;
        MainActivity activity = (MainActivity) getActivity();
        Bundle bundle = getArguments();

        SearchedDataFragmentArgs args = SearchedDataFragmentArgs.fromBundle(bundle);
        queryMasterItem = args.getQueryMasterItem();

        if (activity != null) {
            activity.setupToolbar(R.id.SearchFragment, getString(R.string.data));
        }

        setupAdapterAndRecyclerView();
        mPresenter.loadItems(queryMasterItem);
        setupSearch();
    }

    /**
     * Sets up the {@link RecyclerView} and its adapter to show searched data.
     * - Initializes the {@link SearchedDataAdapter} with a click listener to navigate to InventoryFragment.
     * - Sets a {@link LinearLayoutManager} for the RecyclerView.
     * - Shows or hides the "No Data Found" message based on whether data is available.
     * - Scrolls to the top if a filter action was recently applied.
     */
    private void setupAdapterAndRecyclerView() {
        if (mBinding == null) {
            return;
        }
        mSearchedDataAdapter = new SearchedDataAdapter(productPreviewItem -> {
            Bundle bundle = new Bundle();
            bundle.putString(Utils.ITEM_ID, productPreviewItem.getIdent());
            navigate(mBinding.getRoot(), R.id.SearchedDataFragment, R.id.action_SearchedDataFragment_to_InventoryFragment, bundle);
        });
        mBinding.rvSearchedData.setLayoutManager(new LinearLayoutManager(getContext()));
        mBinding.rvSearchedData.setAdapter(mSearchedDataAdapter);
        setupLoadStateListener();
    }

    /**
     * Sets up the search functionality for filtering data based on the name.
     * - Attaches a {@link TextWatcher} to the search input field to detect changes in the search query.
     * - Uses a {@link Handler} to delay the search query update to avoid excessive calls while the user types.
     * - Calls updateQueryMasterItem with the current input, followed by filterLoadedItems after a delay.
     */
    private void setupSearch() {
        mBinding.etSearchByName.addTextChangedListener(new SimpleTextWatcher() {
            private final Handler handler = new Handler(Looper.getMainLooper());
            private Runnable runnable;

            @Override
            public void afterTextChanged(Editable s) {
                if (runnable != null) {
                    handler.removeCallbacks(runnable);
                }
                if (mPresenter != null) {
                    runnable = () -> {
                        if (mPresenter != null) {
                            mPresenter.searchByName(s.toString());
                            isFilterApplied = true;
                        }
                    };
                    handler.postDelayed(runnable, FILTER_BY_NAME_DELAY);
                }
            }
        });
    }

    @Override
    protected ViewBinding onBindLayout() {
        return FragmentSearchedDataBinding.inflate(LayoutInflater.from(getContext()));
    }

    @Override
    protected BaseView onBindContract() {
        return this;
    }

    @Override
    protected BasePresenter onBindPresenter() {
        return new SearchedDataPresenter(Injection.provideMasterItemRepository(requireContext()));
    }

    /**
     * Displays paged data in the {@link RecyclerView}.
     * Observes the given {@link LiveData} of {@link PagingData} and submits it to
     * the {@link SearchedDataAdapter} for display.
     *
     * @param masterItems LiveData containing paged {@link MasterItem} data.
     */
    @Override
    public void showMasterData(LiveData<PagingData<MasterItem>> masterItems) {
        masterItems.observe(getViewLifecycleOwner(), pagingData ->
                mSearchedDataAdapter.submitData(getLifecycle(), pagingData));
    }

    /**
     * Shows a "Try Again" dialog to the user.
     * If the context is unavailable, it simply navigates back. Otherwise, it attempts
     * to create and display a retry dialog. If the dialog creation fails, it also navigates back.
     */
    @Override
    public void showTryAgainDialog() {
        Context context = getContext();
        if (context == null) {
            goBack();
        } else {
            AlertDialog alertDialog = DialogHelper.createTryAgainDialog(context, getLayoutInflater(),
                    getString(R.string.try_again_search_subtitle), (dialog, which) -> goBack());
            if (alertDialog != null) {
                alertDialog.show();
            } else {
                goBack();
            }
        }
    }

    private void goBack() {
        Navigation.findNavController(mBinding.getRoot()).navigateUp();
    }

    @Override
    public void showProgress() {
        if (mBinding != null) {
            mBinding.progress.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void hideProgress() {
        if (mBinding != null) {
            mBinding.progress.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
        mPresenter = null;
        mSearchedDataAdapter = null;
    }

    private void setupLoadStateListener() {
        mSearchedDataAdapter.addLoadStateListener(loadStates -> {
            LoadState refreshState = loadStates.getRefresh();
            if (refreshState instanceof LoadState.Loading) {
                showProgress();
            } else if (refreshState instanceof LoadState.NotLoading) {
                hideProgress();
                boolean isListEmpty = mSearchedDataAdapter.getItemCount() == 0;
                mBinding.tvNoDataFound.setVisibility(isListEmpty ? View.VISIBLE : View.GONE);
                mBinding.rvSearchedData.setVisibility(isListEmpty ? View.GONE : View.VISIBLE);
                if (isFilterApplied) {
                    mBinding.rvSearchedData.scrollToPosition(TOP_POSITION);
                    isFilterApplied = false;
                }
            } else if (refreshState instanceof LoadState.Error) {
                hideProgress();
                mBinding.tvNoDataFound.setVisibility(View.VISIBLE);
                mBinding.rvSearchedData.setVisibility(View.GONE);
            }
            return Unit.INSTANCE;
        });
    }
}
