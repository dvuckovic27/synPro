package com.metalac.scanner.app.view.inventory.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.LiveData;
import androidx.paging.LoadState;
import androidx.paging.PagingData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewbinding.ViewBinding;

import com.metalac.scanner.app.helpers.DialogConfig;
import com.metalac.scanner.app.helpers.DialogHelper;
import com.metalac.scanner.app.Injection;
import com.metalac.scanner.app.R;
import com.metalac.scanner.app.helpers.SimpleTextWatcher;
import com.metalac.scanner.app.utils.Utils;
import com.metalac.scanner.app.databinding.FragmentSearchedInventoryDataBinding;
import com.metalac.scanner.app.models.ProductPreviewItem;
import com.metalac.scanner.app.models.QueryMasterItem;
import com.metalac.scanner.app.view.BaseFragment;
import com.metalac.scanner.app.view.BasePresenter;
import com.metalac.scanner.app.view.BaseView;
import com.metalac.scanner.app.view.MainActivity;
import com.metalac.scanner.app.view.ScannerReaderError;
import com.metalac.scanner.app.view.inventory.adapter.SearchedInventoryDataAdapter;
import com.metalac.scanner.app.view.inventory.contracts.SearchedInventoryDataContract;
import com.metalac.scanner.app.view.inventory.presenters.SearchedInventoryDataPresenter;

import kotlin.Unit;

public class SearchedInventoryDataFragment extends BaseFragment implements SearchedInventoryDataContract.View {
    private static final int TOP_POSITION = 0;
    //Constant value representing the delay (in milliseconds) for filtering by name.
    private static final int FILTER_BY_NAME_DELAY = 300;
    private FragmentSearchedInventoryDataBinding mBinding;
    private SearchedInventoryDataAdapter mSearchedInventoryDataAdapter;
    private SearchedInventoryDataPresenter mPresenter;
    private boolean mIsFilterApplied;
    private AlertDialog mItemOptionsDialog;

    @Override
    protected void onCreateView(ViewBinding viewBinding, BasePresenter basePresenter) {
        QueryMasterItem queryMasterItem;
        mBinding = (FragmentSearchedInventoryDataBinding) viewBinding;
        mPresenter = (SearchedInventoryDataPresenter) basePresenter;
        setupToolbar();

        Bundle bundle = getArguments();

        SearchedInventoryDataFragmentArgs args = SearchedInventoryDataFragmentArgs.fromBundle(bundle);
        queryMasterItem = args.getQueryMasterItem();

        setupSearch();

        if (queryMasterItem != null) {
            mPresenter.loadItems(queryMasterItem);
        } else {
            showNoResults(true);
        }
    }

    @Override
    protected ViewBinding onBindLayout() {
        return FragmentSearchedInventoryDataBinding.inflate(LayoutInflater.from(getContext()));
    }

    @Override
    protected BaseView onBindContract() {
        return this;
    }

    @Override
    protected BasePresenter onBindPresenter() {
        return new SearchedInventoryDataPresenter(Injection.provideInventoryItemRepository(requireContext()),
                Injection.provideInventoryListRepository(requireContext()));
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

        mSearchedInventoryDataAdapter = null;
        mItemOptionsDialog = null;
        dismissItemOptionsDialog();
    }

    @Override
    public void showInventoryData(LiveData<PagingData<ProductPreviewItem>> productPreviewItems) {
        productPreviewItems.observe(getViewLifecycleOwner(), pagingData ->
                mSearchedInventoryDataAdapter.submitData(getViewLifecycleOwner().getLifecycle(), pagingData));

    }

    @Override
    public void showErrorDialog(ScannerReaderError scannerReaderError) {
        DialogHelper.showErrorDialog(new DialogConfig(getContext(), getLayoutInflater(), scannerReaderError));
    }

    protected MenuProvider provideMenuProvider() {
        return new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
                inflater.inflate(R.menu.toolbar_searched_inventory_data, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.inventory) {
                    navigate(mBinding.getRoot(), R.id.SearchedInventoryDataFragment,
                            R.id.action_SearchedInventoryDataFragment_to_InventoryFragment, null);
                    return true;
                }
                return false;
            }
        };
    }

    /**
     * Sets up the toolbar for {@link SearchedInventoryDataFragment}.
     * <p>
     * Configures the toolbar title and registers a menu callback that
     * redirects the user to {@link InventoryFragment}.
     */
    private void setupToolbar() {
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            activity.setupToolbar(R.id.SearchedInventoryDataFragment, getString(R.string.data));
        }
    }

    private void setupSearch() {
        if (mBinding == null) {
            return;
        }

        setupRecyclerView();
        setupSearchInputListener();
        setupLoadStateListener();
    }

    private void setupRecyclerView() {
        mSearchedInventoryDataAdapter = new SearchedInventoryDataAdapter(this::onItemClicked);
        mBinding.rvSearchedData.setLayoutManager(new LinearLayoutManager(getContext()));
        mBinding.rvSearchedData.setAdapter(mSearchedInventoryDataAdapter);
    }

    private void setupSearchInputListener() {
        mBinding.etSearchByName.addTextChangedListener(new SimpleTextWatcher() {
            private final Handler handler = new Handler(Looper.getMainLooper());
            private Runnable searchRunnable;

            @Override
            public void afterTextChanged(Editable s) {
                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }

                searchRunnable = () -> {
                    if (mPresenter != null) {
                        mPresenter.searchByName(s.toString());
                        mIsFilterApplied = true;
                    }
                };

                handler.postDelayed(searchRunnable, FILTER_BY_NAME_DELAY);
            }
        });
    }

    private void setupLoadStateListener() {
        mSearchedInventoryDataAdapter.addLoadStateListener(loadStates -> {
            LoadState refresh = loadStates.getRefresh();

            if (refresh instanceof LoadState.Loading) {
                showProgress();
            } else {
                hideProgress();
                handleLoadResult(refresh);
            }

            return Unit.INSTANCE;
        });
    }

    private void handleLoadResult(LoadState refreshState) {
        boolean isEmpty = mSearchedInventoryDataAdapter.getItemCount() == 0;

        showNoResults(isEmpty);

        if (refreshState instanceof LoadState.Error) {
            Throwable error = ((LoadState.Error) refreshState).getError();
            DialogHelper.showErrorDialog(new DialogConfig(getContext(), getLayoutInflater(), new ScannerReaderError(error.getMessage())));
        }

        if (mIsFilterApplied) {
            mBinding.rvSearchedData.scrollToPosition(TOP_POSITION);
            mIsFilterApplied = false;
        }
    }

    private void showNoResults(boolean show) {
        mBinding.tvNoDataFound.setVisibility(show ? View.VISIBLE : View.GONE);
        mBinding.rvSearchedData.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void onItemClicked(ProductPreviewItem productPreviewItem) {
        dismissItemOptionsDialog();

        mItemOptionsDialog = DialogHelper.createItemOptionsDialog(
                new DialogConfig(getContext(), getLayoutInflater())
                        .setClickListener((dialog, which) -> onItemOptionsSelected(which, productPreviewItem))
                        .setSubtitle(getString(R.string.dialog_item_data,
                                productPreviewItem.getProductName(),
                                String.valueOf(productPreviewItem.getIdent()),
                                productPreviewItem.getBarcode()))
        );

        mItemOptionsDialog.show();
    }

    private void onItemOptionsSelected(int which, ProductPreviewItem productPreviewItem) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            showVoidConfirmationDialog(productPreviewItem);
        } else {
            launchItemUpdateFlow(productPreviewItem.getInventoryId(),
                    productPreviewItem.getIdent(),
                    productPreviewItem.getProductName());
        }
    }

    private void showVoidConfirmationDialog(ProductPreviewItem productPreviewItem) {
        DialogHelper.showVoidItemDialog(new DialogConfig(getContext(), getLayoutInflater())
                .setSubtitle(getString(R.string.dialog_item_data,
                        productPreviewItem.getProductName(),
                        String.valueOf(productPreviewItem.getIdent()),
                        productPreviewItem.getBarcode()))
                .setClickListener((dialog, which) -> {
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        voidItem(productPreviewItem.getInventoryId());
                    }
                }));
    }

    private void launchItemUpdateFlow(long inventoryItemId, String itemIdent, String itemName) {
        if (mBinding == null) return;

        Bundle bundle = new Bundle();
        bundle.putString(Utils.MASTER_ID, itemIdent);
        bundle.putLong(Utils.INVENTORY_ITEM_ID, inventoryItemId);
        bundle.putBoolean(Utils.IS_UPDATE_ACTION, true);
        bundle.putString(Utils.PRODUCT_NAME, itemName);

        navigate(mBinding.getRoot(), R.id.SearchedInventoryDataFragment,
                R.id.action_SearchedInventoryDataFragment_to_ExtraInfoFragment, bundle);
    }

    private void dismissItemOptionsDialog() {
        if (mItemOptionsDialog != null) {
            mItemOptionsDialog.dismiss();
            mItemOptionsDialog = null;
        }
    }

    private void voidItem(long inventoryItemId) {
        if (mPresenter != null) {
            mPresenter.voidItem(inventoryItemId);
        }
    }
}
