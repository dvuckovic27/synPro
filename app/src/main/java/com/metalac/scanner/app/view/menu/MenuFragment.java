package com.metalac.scanner.app.view.menu;

import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.MenuProvider;
import androidx.viewbinding.ViewBinding;

import com.metalac.scanner.app.helpers.DialogConfig;
import com.metalac.scanner.app.helpers.DialogHelper;
import com.metalac.scanner.app.Injection;
import com.metalac.scanner.app.data.source.PrefManager;
import com.metalac.scanner.app.R;
import com.metalac.scanner.app.databinding.FragmentMenuBinding;
import com.metalac.scanner.app.view.BaseFragment;
import com.metalac.scanner.app.view.BasePresenter;
import com.metalac.scanner.app.view.BaseView;
import com.metalac.scanner.app.view.MainActivity;
import com.metalac.scanner.app.view.ScannerReaderError;
import com.metalac.scanner.app.view.menu.contract.MenuContract;
import com.metalac.scanner.app.view.menu.presenter.MenuPresenter;

public class MenuFragment extends BaseFragment implements MenuContract.View {
    private FragmentMenuBinding mBinding;
    private MenuPresenter mPresenter;

    @Override
    protected void onCreateView(ViewBinding viewBinding, BasePresenter basePresenter) {
        mBinding = (FragmentMenuBinding) viewBinding;
        mPresenter = (MenuPresenter) basePresenter;
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            activity.setupToolbarIcon(R.drawable.ic_settings);
        }
        setupToolbar(activity);
        setupConfigInfo();
        setupButtons();
    }

    @Override
    public void onResume() {
        super.onResume();
        setupConfigInfo();
    }

    @Override
    protected ViewBinding onBindLayout() {
        return FragmentMenuBinding.inflate(LayoutInflater.from(getContext()));
    }

    @Override
    protected BaseView onBindContract() {
        return this;
    }

    @Override
    protected BasePresenter onBindPresenter() {
        return new MenuPresenter(Injection.provideInventoryItemRepository(requireContext()));
    }

    @Override
    protected MenuProvider provideMenuProvider() {
        return new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
                inflater.inflate(R.menu.toolbar_menu, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.change_store_code) {
                    if (mPresenter != null) {
                        mPresenter.onChangeStoreCodeRequested();
                    }
                    return true;
                }
                return false;
            }
        };
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
        mPresenter = null;
    }

    @Override
    public void showStoreCodeChangeDialog(boolean hasInventoryItems) {
        AlertDialog changeStoreCodeDialog = createChangeStoreCodeDialog(hasInventoryItems);

        if (changeStoreCodeDialog != null) {
            changeStoreCodeDialog.show();
        }
    }


    @Override
    public void showErrorDialog(@NonNull ScannerReaderError scannerReaderError) {
        DialogHelper.showErrorDialog(new DialogConfig(getContext(), getLayoutInflater(), scannerReaderError));
    }

    @Override
    public void showProgress() {
        //Not in use
    }

    @Override
    public void hideProgress() {
        //Not in use
    }

    private void setupToolbar(MainActivity activity) {
        if (activity != null) {
            activity.setupToolbar(R.id.MenuFragment, getString(R.string.menu));
        }
    }

    private void setupConfigInfo() {
        mBinding.iConfigInfo.tilDeviceName.setHintAnimationEnabled(false);
        mBinding.iConfigInfo.tilStoreCode.setHintAnimationEnabled(false);

        mBinding.iConfigInfo.etDeviceName.setText(PrefManager.getDeviceName());
        mBinding.iConfigInfo.etStoreCode.setText(PrefManager.getDeviceStoreCode());

        mBinding.iConfigInfo.etDeviceName.setEnabled(false);
        mBinding.iConfigInfo.etStoreCode.setEnabled(false);

        mBinding.iConfigInfo.tilLastSync.setVisibility(View.VISIBLE);
        mBinding.iConfigInfo.etLastSync.setText(PrefManager.hasMasterData() ? PrefManager.getLastMasterDataSyncDate() : getString(R.string.synchronization_required));
    }

    /**
     * Creates and returns a store code change confirmation or warning dialog,
     * depending on whether inventory items already exist in the system.
     * <p>
     * If inventory items exist, the dialog informs the user that they must clear
     * all inventory data before changing the store code.
     * <p>
     * If no items exist, the dialog asks the user to confirm their intention to
     * change the store code and navigates to the configuration screen if confirmed.
     *
     * @param hasInventoryItems true if inventory items exist and store code change is blocked,
     *                          false if store code can be changed directly.
     * @return a configured {@link AlertDialog} ready to be shown.
     */
    private AlertDialog createChangeStoreCodeDialog(boolean hasInventoryItems) {
        DialogConfig dialogConfig = new DialogConfig(getContext(), getLayoutInflater());
        if (hasInventoryItems) {
            dialogConfig
                    .setTitle(getString(R.string.change_store_code_blocked_dialog_title))
                    .setSubtitle(getString(R.string.change_store_code_blocked_dialog_subtitle))
                    .setPositiveButton(getString(R.string.ok));
        } else {
            dialogConfig
                    .setTitle(getString(R.string.change_store_code_confirmation_dialog_title))
                    .setSubtitle(getString(R.string.change_store_code_confirmation_dialog_subtitle))
                    .setPositiveButton(getString(R.string.change_store_code_confirm_button))
                    .setNegativeButton(getString(R.string.cancel))
                    .setClickListener((dialogInterface, i) -> {
                        if (i == DialogInterface.BUTTON_POSITIVE) {
                            navigate(getView(), R.id.MenuFragment, R.id.action_MenuFragment_to_ConfigurationFragment, null);
                        }
                    });
        }

        return DialogHelper.createAttentionDialog(dialogConfig);
    }


    private void setupButtons() {
        mBinding.btInventoryLists.setEnabled(PrefManager.hasMasterData());

        mBinding.btInventoryLists.setOnClickListener(v ->
                navigate(mBinding.getRoot(), R.id.MenuFragment, R.id.action_MenuFragment_to_InventoryListsFragment, null));
        mBinding.btSync.setOnClickListener(v ->
                navigate(mBinding.getRoot(), R.id.MenuFragment, R.id.action_MenuFragment_to_SynchronizationFragment, null));
    }
}
