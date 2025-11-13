package com.metalac.scanner.app.view.synchronization;

import static android.view.View.GONE;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.viewbinding.ViewBinding;

import com.metalac.scanner.app.helpers.DialogConfig;
import com.metalac.scanner.app.helpers.DialogHelper;
import com.metalac.scanner.app.Injection;
import com.metalac.scanner.app.data.source.PrefManager;
import com.metalac.scanner.app.R;
import com.metalac.scanner.app.databinding.FragmentSynchronizationBinding;
import com.metalac.scanner.app.view.BaseFragment;
import com.metalac.scanner.app.view.BasePresenter;
import com.metalac.scanner.app.view.BaseView;
import com.metalac.scanner.app.view.MainActivity;
import com.metalac.scanner.app.view.ScannerReaderError;

public class SynchronizationFragment extends BaseFragment implements SynchronizationContract.View {

    private FragmentSynchronizationBinding mBinding;
    private SynchronizationPresenter mPresenter;
    private AlertDialog mProgressDialog;
    private AlertDialog mSuccessDialog;
    private AlertDialog mAttentionDialog;

    /**
     * Handles the result of the JSON file picker.
     * If a valid URI is selected, triggers loading via the presenter.
     */
    private final ActivityResultLauncher<Intent> jsonPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();

                    if (uri != null && mPresenter != null) {
                        mPresenter.loadMasterItems(uri);
                    }
                }
            }
    );

    @Override
    protected void onCreateView(ViewBinding viewBinding, BasePresenter basePresenter) {
        mBinding = (FragmentSynchronizationBinding) viewBinding;
        mPresenter = (SynchronizationPresenter) basePresenter;

        MainActivity activity = (MainActivity) getActivity();

        if (activity != null) {
            activity.setupToolbar(R.id.SynchronizationFragment, getString(R.string.sync));
        }

        mBinding.tilLastSync.setHintAnimationEnabled(false);
        mBinding.tilLastExport.setHintAnimationEnabled(false);

        updateDateField(PrefManager.getLastMasterDataSyncDate(), mBinding.tilLastSync, mBinding.etLastSync);
        updateDateField(PrefManager.getLastDataExportDate(), mBinding.tilLastExport, mBinding.etLastExport);


        mBinding.btGetMaster.setOnClickListener(view -> openJsonFilePicker());

        mPresenter.checkInventoryData();
        mPresenter.checkInventoryListData();

        setupButtons();
    }

    /**
     * Updates the visibility and content of a date field within its container.
     * <p>
     * If the provided {@code date} is null or empty, the container is hidden. Otherwise,
     * the container is shown and the date is set on the {@code editText}.
     * Also updates the visibility of the parent date container depending on whether
     * both date fields are hidden.
     *
     * @param date      The date string to display.
     * @param container The container view holding the date field.
     * @param editText  The EditText to populate with the date.
     */
    private void updateDateField(String date, View container, EditText editText) {
        boolean hasDate = !TextUtils.isEmpty(date);

        container.setVisibility(hasDate ? View.VISIBLE : View.GONE);
        if (hasDate) {
            editText.setText(date);
        }

        updateDateContainerVisibility();
    }

    /**
     * Updates the visibility of the overall date container based on its children.
     * Hides the container if both child date fields are hidden.
     */
    private void updateDateContainerVisibility() {
        boolean bothHidden = mBinding.tilLastSync.getVisibility() == GONE &&
                mBinding.tilLastExport.getVisibility() == GONE;

        mBinding.llDateContainer.setVisibility(bothHidden ? GONE : View.VISIBLE);
    }


    /**
     * Sets up click listeners for inventory list buttons.
     * <p>
     * - "Send Inventory Lists" button triggers an export dialog.
     * - "Delete Inventories" button triggers a delete confirmation dialog.
     * </p>
     * Does nothing if the binding is null.
     */
    private void setupButtons() {
        if (mBinding != null) {
            mBinding.btSendInventoryLists.setOnClickListener(v ->
                    createAttentionDialog(DialogHelper.DialogMode.EXPORT));
            mBinding.btDeleteInventories.setOnClickListener(v ->
                    createAttentionDialog(DialogHelper.DialogMode.DELETE));
        }
    }

    @Override
    protected ViewBinding onBindLayout() {
        return FragmentSynchronizationBinding.inflate(LayoutInflater.from(getContext()));
    }

    @Override
    protected BaseView onBindContract() {
        return this;
    }

    @Override
    protected BasePresenter onBindPresenter() {
        return new SynchronizationPresenter(Injection.provideMasterItemRepository(requireContext()),
                Injection.provideInventoryItemRepository(requireContext()),
                Injection.provideInventoryListRepository(requireContext()));
    }

    @Override
    public void onSuccessfulSync(@NonNull String lastSyncDate) {
        updateDateField(lastSyncDate, mBinding.tilLastSync, mBinding.etLastSync);
        PrefManager.setLastMasterDataSyncDate(lastSyncDate);

        createSuccessfulDialog(DialogHelper.DialogMode.SYNC);
    }

    @Override
    public void onFailedSync(@NonNull ScannerReaderError scannerReaderError) {
        DialogConfig dialogConfig = new DialogConfig(getContext(), getLayoutInflater(), scannerReaderError);
        DialogHelper.showErrorDialog(dialogConfig);
    }

    @Override
    public void enableExport(boolean enable) {
        if (mBinding != null) {
            mBinding.btSendInventoryLists.setEnabled(enable);
        }
    }

    @Override
    public void enableDelete(boolean enable) {
        if (mBinding != null) {
            mBinding.btDeleteInventories.setEnabled(enable);
        }
    }

    @Override
    public void showErrorDialog(@NonNull ScannerReaderError scannerReaderError) {
        DialogHelper.showErrorDialog(new DialogConfig(getContext(), getLayoutInflater(), scannerReaderError));
    }

    /**
     * Creates and shows a success dialog based on the specified dialog mode.
     * <p>
     * If an existing success dialog is showing, it is dismissed before creating a new one.
     * The dialog title and subtitle are set according to the {@link DialogHelper.DialogMode}.
     * </p>
     *
     * @param dialogMode The mode determining which success message to display.
     */
    @Override
    public void createSuccessfulDialog(DialogHelper.DialogMode dialogMode) {
        if (mSuccessDialog != null) {
            mSuccessDialog.dismiss();
            mSuccessDialog = null;
        }

        String title = "";
        String subtitle = "";
        switch (dialogMode) {
            case EXPORT:
                title = getString(R.string.export_data_success_title);
                subtitle = getString(R.string.export_data_success_subtitle);
                break;
            case SYNC:
                title = getString(R.string.sync_success_title);
                subtitle = getString(R.string.sync_success_subtitle);
                break;
            case DELETE:
                title = getString(R.string.delete_data_success_title);
                subtitle = getString(R.string.delete_data_success_subtitle);
                break;
        }

        mSuccessDialog = DialogHelper.createSuccessDialog(
                new DialogConfig(getContext(), getLayoutInflater())
                        .setTitle(title)
                        .setSubtitle(subtitle)
                        .setPositiveButton(R.string.ok));
        showSuccessfulDialog();
    }

    @Override
    public void displayLastExportDate(String lastExportDate) {
        updateDateField(lastExportDate, mBinding.tilLastExport, mBinding.etLastExport);
    }

    private void showSuccessfulDialog() {
        if (mSuccessDialog != null) {
            mSuccessDialog.show();
        }
    }

    /**
     * Creates and shows an attention dialog based on the specified dialog mode.
     * <p>
     * If an existing attention dialog is showing, it is dismissed before creating a new one.
     * The dialog configuration is built according to the {@link DialogHelper.DialogMode}.
     * </p>
     *
     * @param dialogMode The mode determining the dialog’s content and behavior.
     */
    private void createAttentionDialog(DialogHelper.DialogMode dialogMode) {
        if (mAttentionDialog != null) {
            mAttentionDialog.dismiss();
            mAttentionDialog = null;
        }

        mAttentionDialog = DialogHelper.createAttentionDialog(buildAttentionDialogConfig(dialogMode));
        showAttentionDialog();
    }

    /**
     * Builds a {@link DialogConfig} for an attention dialog based on the given dialog mode.
     * <p>
     * Sets the dialog title, subtitle, positive button text, and click listener
     * according to the specified {@link DialogHelper.DialogMode}.
     * The dialog is configured with a cancel button and is non-cancelable by outside touch.
     * </p>
     *
     * @param dialogMode The mode determining the dialog's content and behavior.
     * @return A configured {@link DialogConfig} instance for the attention dialog.
     */
    private DialogConfig buildAttentionDialogConfig(DialogHelper.DialogMode dialogMode) {
        String title = "";
        String subtitle = "";
        String buttonPositive = "";
        DialogInterface.OnClickListener clickListener = null;

        if (dialogMode == DialogHelper.DialogMode.EXPORT) {
            title = getString(R.string.export_data_dialog_title);
            subtitle = getString(R.string.export_data_dialog_subtitle);
            buttonPositive = getString(R.string.export_data);
            clickListener = createExportClickListener();
        } else if (dialogMode == DialogHelper.DialogMode.DELETE) {
            title = getString(R.string.delete_data_dialog_title);
            subtitle = getString(R.string.delete_data_dialog_subtitle);
            buttonPositive = getString(R.string.clear);
            clickListener = createDeleteClickListener();
        }


        return new DialogConfig(getContext(), getLayoutInflater())
                .setTitle(title)
                .setSubtitle(subtitle)
                .setNegativeButton(R.string.cancel)
                .setPositiveButton(buttonPositive)
                .setClickListener(clickListener)
                .setCancelable(false);
    }

    /**
     * Creates a click listener for the export dialog's positive and negative buttons.
     * <p>
     * On positive button click, triggers the exportData() method of the presenter.
     * On any other click, dismisses the dialog.
     * </p>
     *
     * @return A {@link DialogInterface.OnClickListener} for the export dialog.
     */
    private DialogInterface.OnClickListener createExportClickListener() {
        return (dialog, which) -> {
            if (which == DialogInterface.BUTTON_POSITIVE && mPresenter != null) {
                mPresenter.exportData();
            } else {
                dialog.dismiss();
            }
        };
    }

    /**
     * Creates a click listener for the delete dialog's positive and negative buttons.
     * <p>
     * On positive button click, triggers the deleteInventoryData() method of the presenter.
     * On any other click, dismisses the dialog.
     * </p>
     *
     * @return A {@link DialogInterface.OnClickListener} for the delete dialog.
     */
    private DialogInterface.OnClickListener createDeleteClickListener() {
        return (dialog, which) -> {
            if (which == DialogInterface.BUTTON_POSITIVE && mPresenter != null) {
                mPresenter.deleteInventoryData();
            } else {
                dialog.dismiss();
            }
        };
    }

    private void showAttentionDialog() {
        if (mAttentionDialog != null) {
            mAttentionDialog.show();
        }
    }

    /**
     * Creates and shows a progress dialog based on the specified dialog mode.
     * <p>
     * If an existing dialog is present, it will be dismissed first.
     * The dialog title and subtitle are set according to the {@link DialogHelper.DialogMode}.
     * </p>
     *
     * @param dialogMode The mode which determines the dialog’s title and subtitle.
     */
    @Override
    public void createProgressDialog(DialogHelper.DialogMode dialogMode) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
        String title = "";
        String subtitle = "";
        switch (dialogMode) {
            case EXPORT:
                title = getString(R.string.exporting);
                subtitle = getString(R.string.exporting_inventory_data_subtitle);
                break;
            case SYNC:
                title = getString(R.string.loading);
                subtitle = getString(R.string.loading_master_data_subtitle);
                break;
            case DELETE:
                title = getString(R.string.deleting);
                subtitle = getString(R.string.deleting_data_subtitle);
                break;
        }
        mProgressDialog = DialogHelper.createProgressDialog(
                new DialogConfig(getContext(), getLayoutInflater())
                        .setTitle(title)
                        .setSubtitle(subtitle));
        showProgress();
    }

    @Override
    public void showProgress() {
        if (mProgressDialog != null) {
            mProgressDialog.show();
        }
    }

    @Override
    public void hideProgress() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    /**
     * Opens a file picker allowing the user to select a JSON file.
     */
    public void openJsonFilePicker() {
        String intentType = "application/json";

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(intentType);
        jsonPickerLauncher.launch(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
        mPresenter = null;
        mProgressDialog = null;
        mSuccessDialog = null;
        mAttentionDialog = null;
    }
}
