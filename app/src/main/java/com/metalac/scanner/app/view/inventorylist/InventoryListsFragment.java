package com.metalac.scanner.app.view.inventorylist;

import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewbinding.ViewBinding;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.metalac.scanner.app.helpers.BottomDialogHelper;
import com.metalac.scanner.app.helpers.DialogConfig;
import com.metalac.scanner.app.helpers.DialogHelper;
import com.metalac.scanner.app.Injection;
import com.metalac.scanner.app.R;
import com.metalac.scanner.app.databinding.FragmentInventoryListsBinding;
import com.metalac.scanner.app.view.BaseFragment;
import com.metalac.scanner.app.view.BasePresenter;
import com.metalac.scanner.app.view.BaseView;
import com.metalac.scanner.app.view.MainActivity;
import com.metalac.scanner.app.view.ScannerReaderError;
import com.metalac.scanner.app.view.inventorylist.adapter.InventoryListAdapter;
import com.metalac.scanner.app.view.inventorylist.contract.InventoryListContract;
import com.metalac.scanner.app.view.inventorylist.presenter.InventoryListPresenter;

import java.util.List;

public class InventoryListsFragment extends BaseFragment implements InventoryListContract.View {
    private FragmentInventoryListsBinding mBinding;
    private InventoryListAdapter mInventoryListAdapter;
    private BottomSheetDialog mAddNewListDialog;

    @Override
    protected void onCreateView(ViewBinding viewBinding, BasePresenter basePresenter) {
        InventoryListPresenter presenter;
        mBinding = (FragmentInventoryListsBinding) viewBinding;
        presenter = (InventoryListPresenter) basePresenter;
        MainActivity activity = (MainActivity) getActivity();

        if (activity != null) {
            activity.setupToolbar(R.id.InventoryListsFragment, getString(R.string.inventory_lists));
        }

        setUpButtonClick(presenter);
        initAdapterAndRecyclerView(presenter);
        presenter.loadInventoryLists();
    }

    /**
     * Sets up the click listener for the "Add" floating action button.
     * <p>
     * When the button is clicked, a bottom sheet dialog is shown to enter a new inventory list name.
     * Once the user confirms, the provided {@link InventoryListPresenter} handles adding the list.
     *
     * @param presenter the presenter responsible for handling inventory list actions
     */
    private void setUpButtonClick(InventoryListPresenter presenter) {
        mBinding.fbAdd.setOnClickListener(v -> {
            if (mAddNewListDialog == null) {
                mAddNewListDialog = BottomDialogHelper.createAddListDialog(getContext(), listName -> {
                    if (presenter != null) {
                        presenter.addInventoryList(listName);
                    }
                });
                mAddNewListDialog.show();
            } else {
                mAddNewListDialog.show();
            }
        });
    }

    private void initAdapterAndRecyclerView(InventoryListPresenter presenter) {
        mInventoryListAdapter = new InventoryListAdapter(inventoryList -> {
            if (presenter != null) {
                presenter.setCurrentList(inventoryList);
            }
        });
        mBinding.rvInventoryList.setLayoutManager(new LinearLayoutManager(getContext()));
        mBinding.rvInventoryList.setAdapter(mInventoryListAdapter);
    }

    @Override
    protected ViewBinding onBindLayout() {
        return FragmentInventoryListsBinding.inflate(LayoutInflater.from(getContext()));
    }

    @Override
    protected BaseView onBindContract() {
        return this;
    }

    @Override
    protected BasePresenter onBindPresenter() {
        return new InventoryListPresenter(Injection.provideInventoryListRepository(requireContext()));
    }

    @Override
    public void showInventoryLists(List<Object> inventoryLists, boolean scrollToLast) {
        if (mBinding == null) {
            return;
        }
        mBinding.tvNoData.setVisibility(View.GONE);
        if (mInventoryListAdapter != null) {
            mInventoryListAdapter.setDataList(inventoryLists);
            if (scrollToLast) {
                mBinding.rvInventoryList.smoothScrollToPosition(mInventoryListAdapter.getItemCount());
            }
        }
    }

    @Override
    public void showNoData() {
        if (mBinding != null) {
            mBinding.tvNoData.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showErrorDialog(@NonNull ScannerReaderError scannerReaderError) {
        DialogHelper.showErrorDialog(new DialogConfig(getContext(), getLayoutInflater(), scannerReaderError));
    }

    @Override
    public void gotoInventory() {
        navigate(mBinding.getRoot(), R.id.InventoryListsFragment, R.id.action_InventoryListsFragment_to_InventoryFragment, null);
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
        mInventoryListAdapter = null;
        mAddNewListDialog = null;
    }
}
