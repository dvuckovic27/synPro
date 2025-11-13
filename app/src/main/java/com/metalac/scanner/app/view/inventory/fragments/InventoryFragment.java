package com.metalac.scanner.app.view.inventory.fragments;

import static android.view.View.VISIBLE;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.MenuProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewbinding.ViewBinding;

import com.metalac.scanner.app.helpers.DialogConfig;
import com.metalac.scanner.app.helpers.DialogHelper;
import com.metalac.scanner.app.Injection;
import com.metalac.scanner.app.data.source.PrefManager;
import com.metalac.scanner.app.R;
import com.metalac.scanner.app.utils.Utils;
import com.metalac.scanner.app.utils.ViewAnimationUtils;
import com.metalac.scanner.app.models.InventoryItem;
import com.metalac.scanner.app.models.MasterItem;
import com.metalac.scanner.app.helpers.SimpleTextWatcher;
import com.metalac.scanner.app.databinding.FragmentInventoryBinding;
import com.metalac.scanner.app.models.ProductPreviewItem;
import com.metalac.scanner.app.view.BaseFragment;
import com.metalac.scanner.app.view.BasePresenter;
import com.metalac.scanner.app.view.BaseView;
import com.metalac.scanner.app.view.MainActivity;
import com.metalac.scanner.app.view.interfaces.IOnScanCallback;
import com.metalac.scanner.app.view.inventory.adapter.InventoryItemAdapter;
import com.metalac.scanner.app.view.inventory.contracts.InventoryContract;
import com.metalac.scanner.app.view.inventory.presenters.InventoryPresenter;
import com.metalac.scanner.app.view.ScannerReaderError;

import java.util.ArrayList;
import java.util.List;

public class InventoryFragment extends BaseFragment implements InventoryContract.View {
    private static final String INFO_PREFIX = " | ";
    private FragmentInventoryBinding mBinding;
    private InventoryPresenter mPresenter;
    private InventoryItemAdapter mInventoryItemAdapter;

    private AlertDialog mProgressDialog;
    private AlertDialog mQuantityWarningDialog;
    private AlertDialog mInvalidQuantityDialog;
    private AlertDialog mDataWillBeLostDialog;
    private AlertDialog mErrorDialog;
    private AlertDialog mAdditionalInfoDialog;

    private EditText[] mEditTexts;
    private String mQuantity;
    private boolean mEnableExtraInfo = false;

    @Override
    protected void onCreateView(ViewBinding viewBinding, BasePresenter basePresenter) {
        mBinding = (FragmentInventoryBinding) viewBinding;
        mPresenter = (InventoryPresenter) basePresenter;
        mPresenter.loadCurrentList();

        setupToolbarAndScanner();
        extractArguments();
        setupListeners();
        setAdapter();
        setTextWatchers();
        setUpAlternativeSearch();

        mPresenter.getSearchedData();
    }

    @Override
    protected ViewBinding onBindLayout() {
        return FragmentInventoryBinding.inflate(LayoutInflater.from(getContext()));
    }

    @Override
    protected BaseView onBindContract() {
        return this;
    }

    @Override
    protected BasePresenter onBindPresenter() {
        return new InventoryPresenter(
                Injection.provideMasterItemRepository(requireContext()),
                Injection.provideInventoryItemRepository(requireContext()),
                Injection.provideInventoryListRepository(requireContext()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        resetAlternativeSearch(true);
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.removeOnScanCallback();
        }
        mBinding = null;
        mEditTexts = null;
        mInventoryItemAdapter = null;
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
        mQuantityWarningDialog = null;
        mInvalidQuantityDialog = null;
        mDataWillBeLostDialog = null;
        mErrorDialog = null;
    }

    /**
     * Callback invoked when the {@link MasterItem} is successfully loaded by a regular barcode scan.
     * <p>
     * This method updates the UI with data from the given {@code MasterItem}, including
     * barcode, ident, product name, unit of measure, price, and ERP quantity. It also configures
     * the quantity input field according to the number of decimal places defined for the item.
     * <ul>
     *     <li>If {@code isNewItem} is {@code true}:
     *         <ul>
     *             <li>If {@code mQuantity} contains a value, that value is applied to the
     *             quantity field and then cleared.</li>
     *             <li>If {@code mQuantity} is empty, the quantity field is cleared.</li>
     *         </ul>
     *     </li>
     *     <li>If {@code isNewItem} is {@code false}, the existing quantity value is preserved.</li>
     * </ul>
     * The quantity field is then enabled, focus is requested on it, and the keyboard is shown.
     * Additionally, the "Extra Info" button and menu options are enabled.
     * <p>
     * If an error dialog is currently displayed, it is dismissed before updating the UI.
     *
     * @param item      the {@link MasterItem} containing the loaded product data; must not be {@code null}
     * @param isNewItem {@code true} if the item was newly scanned and the quantity field should be reset
     */
    @Override
    public void onMasterItemLoaded(@NonNull MasterItem item, boolean isNewItem) {
        if (mErrorDialog != null && mErrorDialog.isShowing()) {
            mErrorDialog.dismiss();
        }
        mBinding.etBarcode.setText(item.getBarcode());
        mBinding.etIdentToShow.setText(item.getIdent());
        mBinding.etProductName.setText(item.getName());
        mBinding.etUnitOfMeasure.setText(item.getUnitOfMeasure());
        mBinding.etPrice.setText(item.getPriceString());
        mBinding.etInitialQuantity.setText(item.getQuantityErpString());

        Utils.configureInput(mBinding.etQuantity, item.getDecimalPlaces());
        if (isNewItem) {
            if (!TextUtils.isEmpty(mQuantity)) {
                mBinding.etQuantity.setText(mQuantity);
                mQuantity = "";
            } else {
                mBinding.etQuantity.setText("");
            }
        }
        mBinding.etQuantity.setEnabled(true);

        mBinding.etQuantity.requestFocus();

        toggleKeyboard(mBinding.etQuantity, true);
        updateMenu(true);
    }

    /**
     * Clears the ident and alternate ID search fields and optionally collapses
     * the alternative search view.
     *
     * @param collapseView If {@code true}, the alternative search view will be collapsed.
     */
    @Override
    public void resetAlternativeSearch(boolean collapseView) {
        if (mBinding != null) {
            if (mBinding.iAlternativeSearch.llAlternativeSearchContent.getVisibility() == VISIBLE && collapseView) {
                mBinding.iAlternativeSearch.llAlternativeSearch.performClick();
            }
            mBinding.iAlternativeSearch.etIdent.setText("");
            mBinding.iAlternativeSearch.etAltId.setText("");
        }
    }

    /**
     * Callback invoked when the {@link MasterItem} is successfully loaded from a weight barcode.
     * <p>
     * This method behaves similarly to {@link InventoryContract.View#onMasterItemLoaded(MasterItem, boolean)},
     * but additionally sets the quantity field to the parsed weight (in kilograms) extracted from the barcode,
     * and removes focus from the quantity field.
     * </p>
     *
     * @param masterItem the {@link MasterItem} loaded using the alternative code from the weight barcode; must not be {@code null}
     * @param weight     the parsed weight in kilograms extracted from the barcode; must be a positive decimal
     */
    @Override
    public void onMasterItemLoadedByWeightBarcode(@NonNull MasterItem masterItem, double weight) {
        if (mBinding == null) {
            return;
        }
        onMasterItemLoaded(masterItem, true);
        mBinding.etQuantity.setText(String.valueOf(weight));
        mBinding.etQuantity.clearFocus();
    }

    /**
     * Called when the loading of a {@link MasterItem} fails.
     * This method shows an error dialog to notify the user about the failure.
     *
     * @param scannerReaderError The error that occurred during the loading of the {@link MasterItem}.
     */
    @Override
    public void onMasterItemLoadingFailed(@NonNull ScannerReaderError scannerReaderError) {
        if (mErrorDialog != null) {
            mErrorDialog.dismiss();
        }

        mErrorDialog = DialogHelper.createErrorDialog(new DialogConfig(getContext(), getLayoutInflater(), scannerReaderError));

        if (mErrorDialog != null) {
            mErrorDialog.show();
        }
    }

    /**
     * Displays an error dialog if the unit of measure (UOM) quantity is zero or less.
     * <p>
     * The dialog is non-cancelable and informs the user that negative or zero quantities are not allowed.
     * When the user acknowledges the message, the quantity field is cleared and focused again
     * for correction.
     */
    @Override
    public void showDialogIfUomIsZeroOrLess() {
        if (mInvalidQuantityDialog == null) {
            DialogConfig dialogConfig = new DialogConfig(getContext(), getLayoutInflater())
                    .setTitle(getString(R.string.negative_quantity_title))
                    .setSubtitle(getString(R.string.negative_quantity_subtitle))
                    .setPositiveButton(getString(R.string.ok))
                    .setCancelable(false)
                    .setClickListener((dialog, which) -> {
                        mBinding.etQuantity.setText("");
                        mBinding.etQuantity.requestFocus();
                    });
            mInvalidQuantityDialog = DialogHelper.createErrorDialog(dialogConfig);
        }

        if (mInvalidQuantityDialog != null) {
            mInvalidQuantityDialog.show();
        }
    }

    @Override
    public void addItem(@NonNull ProductPreviewItem productPreviewItem) {
        mBinding.rvItems.setVisibility(VISIBLE);
        mBinding.tvNoData.setVisibility(View.GONE);
        mBinding.etQuantity.setEnabled(false);

        mInventoryItemAdapter.addNewItem(productPreviewItem);
        toggleKeyboard(mBinding.etQuantity, false);
        clearFields();
        updateMenu(false);
    }

    @Override
    public void showTryAgainDialog() {
        Context context = getContext();
        if (context == null) {
            clearFields();
            return;
        }
        AlertDialog alertDialog = DialogHelper.createTryAgainDialog(context, getLayoutInflater(), getString(R.string.add_product_error_subtitle), (dialog, which) -> {
            if (mPresenter == null) {
                clearFields();
                return;
            }
            String barcode = Utils.getStringOrNull(mBinding.etBarcode);
            if (barcode == null || barcode.isEmpty()) {
                clearFields();
            } else {
                mPresenter.loadMasterItem(barcode);
            }
        });
        if (alertDialog != null) {
            alertDialog.show();
        } else {
            clearFields();
        }
    }

    @Override
    public void showErrorDialog(@NonNull ScannerReaderError error) {
        DialogHelper.showErrorDialog(new DialogConfig(getContext(), getLayoutInflater(), error));
    }

    @Override
    public void populateInventoryAdapter(@NonNull List<ProductPreviewItem> items) {
        if (items.isEmpty()) {
            mBinding.rvItems.setVisibility(View.GONE);
            mBinding.tvNoData.setVisibility(VISIBLE);
        } else {
            mBinding.rvItems.setVisibility(VISIBLE);
            mBinding.tvNoData.setVisibility(View.GONE);
            mInventoryItemAdapter.setDataList(new ArrayList<>(items));
        }
    }

    @Override
    public void showAdditionalData(@NonNull InventoryItem inventoryItem) {
        Context context = getContext();
        if (context == null) {
            return;
        }
        if (mAdditionalInfoDialog != null) {
            mAdditionalInfoDialog.dismiss();
            mAdditionalInfoDialog = null;
        }

        mAdditionalInfoDialog = DialogHelper.createAdditionalInfoDialog(new DialogConfig(context, getLayoutInflater()), inventoryItem);
        mAdditionalInfoDialog.show();
    }

    @Override
    public void displayCurrentListData(String listName) {
        if (mBinding != null) {
            String listNameText = INFO_PREFIX + listName;
            mBinding.tvListName.setText(listNameText);
        }
    }

    /**
     * Displays a warning dialog when the specified quantity may be considered unusual or requires user confirmation.
     * <p>
     * This dialog is shown when the user inputs a quantity that passes validation but is potentially suspicious
     * (e.g. too large or unexpected based on business rules). The user can either confirm the action, which will
     * proceed with adding the item to inventory, or cancel it, which will clear and refocus the quantity input field.
     *
     * @param item     The {@link MasterItem} associated with the quantity input.
     * @param quantity The quantity value that triggered the warning dialog.
     */
    @Override
    public void showQuantityWarningDialog(@NonNull MasterItem item, double quantity) {
        if (mQuantityWarningDialog != null) {
            mQuantityWarningDialog.dismiss();
            mQuantityWarningDialog = null;
        }

        DialogConfig dialogConfig = new DialogConfig(getContext(), getLayoutInflater())
                .setTitle(getString(R.string.warning_quantity_title))
                .setSubtitle(getString(R.string.warning_quantity_subtitle))
                .setPositiveButton(getString(R.string.confirm))
                .setNegativeButton(getString(R.string.cancel))
                .setCancelable(false)
                .setClickListener((dialog, which) -> {
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        addInventoryItem(item, quantity);
                    } else {
                        mBinding.etQuantity.setText("");
                        mBinding.etQuantity.requestFocus();
                    }
                });

        mQuantityWarningDialog = DialogHelper.createAttentionDialog(dialogConfig);

        if (mQuantityWarningDialog != null) {
            mQuantityWarningDialog.show();
        }
    }

    @Override
    public void showProgress() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }

        if (mProgressDialog == null) {
            DialogConfig config = new DialogConfig(getContext(), getLayoutInflater())
                    .setTitle(getString(R.string.loading))
                    .setSubtitle(getString(R.string.loading_master_data_subtitle));
            mProgressDialog = DialogHelper.createProgressDialog(config);
        }
        mProgressDialog.show();
    }

    @Override
    public void hideProgress() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    protected MenuProvider provideMenuProvider() {
        return new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
                inflater.inflate(R.menu.toolbar_inventory, menu);
                menu.findItem(R.id.extraInfo).setEnabled(mEnableExtraInfo);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.extraInfo) {
                    if (hasAnyInput()) {
                        navigateToExtraInfo();
                    }
                    return true;
                } else if (id == R.id.search) {
                    handleSearchNavigation();
                    return true;
                }
                return false;
            }
        };
    }

    /**
     * Updates the menu state, enabling or disabling the Extra Info item,
     * and refreshes the menu in the main activity.
     *
     * @param enableExtraInfo Whether the Extra Info menu item should be enabled.
     */
    private void updateMenu(boolean enableExtraInfo) {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mEnableExtraInfo = enableExtraInfo;
            mainActivity.invalidateMenu();
        }
    }

    private void setupToolbarAndScanner() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.setupToolbar(R.id.InventoryFragment, getString(R.string.inventory));
            mainActivity.setOnScanCallback(new IOnScanCallback() {
                @Override
                public void onBarcodeScanResult(@NonNull String barcode) {
                    if (mPresenter != null) {
                        mPresenter.loadMasterItem(barcode);
                    }
                }

                @Override
                public void onWeightBarcodeScanResult(@NonNull String barcode) {
                    if (mPresenter != null) {
                        mPresenter.handleWeightBarcode(barcode);
                    }
                }
            });
        }
    }

    /**
     * Extracts navigation arguments passed to this fragment.
     * <p>
     * - Retrieves {@code itemId} from the Safe Args bundle and forwards it to the presenter.
     * - Removes the {@code itemId} key from the fragment arguments to ensure it is not reused
     * when the fragment is recreated.
     * - Updates the store code text on the UI using the device's store code from preferences.
     */
    private void extractArguments() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            InventoryFragmentArgs args = InventoryFragmentArgs.fromBundle(bundle);
            if (mPresenter != null) {
                mPresenter.setItemId(args.getItemId());
            }

            bundle.remove(Utils.ITEM_ID);
        }

        String storeCodeText = INFO_PREFIX + PrefManager.getDeviceStoreCode();
        mBinding.tvStoreCode.setText(storeCodeText);
    }

    /**
     * Sets up UI listeners for the fragment:
     * <p>
     * - Floating button click triggers quantity validation.
     * - Fragment result listener handles results from other fragments.
     * - Editor action listeners for ident and alternate ID fields trigger
     * search when the IME "Done" action is pressed.
     * - Editor action listener for quantity field triggers validation
     * when the IME "Done" action is pressed.
     * </p>
     */
    private void setupListeners() {
        mBinding.fbAdd.setOnClickListener(v -> validateEnteredQuantity());
        setFragmentResultListener();
        mBinding.iAlternativeSearch.etIdent.setOnEditorActionListener(
                createSearchListener(this::findItemByIdent)
        );
        mBinding.iAlternativeSearch.etAltId.setOnEditorActionListener(
                createSearchListener(this::findItemAltId)
        );
        mBinding.etQuantity.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                validateEnteredQuantity();
                return false;
            }
            return true;
        });
    }

    private void navigateToExtraInfo() {
        Bundle bundle = new Bundle();
        if (mPresenter != null) {
            bundle.putString(Utils.MASTER_ID, mPresenter.getItemIdent());
            bundle.putString(Utils.QUANTITY, Utils.getStringOrNull(mBinding.etQuantity));
            bundle.putString(Utils.PRODUCT_NAME, Utils.getStringOrNull(mBinding.etProductName));
        }
        navigate(mBinding.getRoot(), R.id.InventoryFragment, R.id.action_InventoryFragment_to_ExtraInfoFragment, bundle);
    }

    private void handleSearchNavigation() {
        if (hasAnyInput()) {
            showDataWillBeLostDialog(() -> {
                clearFields();
                navigateToSearch();
            });
        } else {
            navigateToSearch();
        }
    }

    private void navigateToSearch() {
        navigate(mBinding.getRoot(), R.id.InventoryFragment, R.id.action_InventoryFragment_to_SearchFragment, null);
    }

    /**
     * Displays a confirmation dialog warning the user that unsaved data will be lost.
     * <p>
     * This dialog is typically used when the user attempts to navigate away or perform an action
     * that would discard current changes. If the user confirms, the provided {@link Runnable} is executed.
     *
     * @param onConfirm A {@link Runnable} that will be run if the user confirms the action.
     */
    private void showDataWillBeLostDialog(Runnable onConfirm) {
        if (mDataWillBeLostDialog == null) {
            DialogConfig config = new DialogConfig(getContext(), getLayoutInflater())
                    .setTitle(getString(R.string.data_will_be_lost_title))
                    .setSubtitle(getString(R.string.data_will_be_lost_subtitle))
                    .setPositiveButton(getString(R.string.confirm))
                    .setNegativeButton(getString(R.string.cancel))
                    .setClickListener((dialog, which) -> {
                        if (which == DialogInterface.BUTTON_POSITIVE) onConfirm.run();
                    });
            mDataWillBeLostDialog = DialogHelper.createAttentionDialog(config);
        }

        if (mDataWillBeLostDialog != null) {
            mDataWillBeLostDialog.show();
        }
    }

    private void setAdapter() {
        mInventoryItemAdapter = new InventoryItemAdapter(productPreviewItem ->
                mPresenter.loadAdditionData(productPreviewItem.getInventoryId()));
        mInventoryItemAdapter.setDataList(new ArrayList<>());
        mBinding.rvItems.setLayoutManager(new LinearLayoutManager(getContext()));
        mBinding.rvItems.setAdapter(mInventoryItemAdapter);
    }

    /**
     * Sets up a FragmentResultListener to listen for results returned from ExtraInfoFragment.
     * <p>
     * The listener reacts to results sent with the {@code EXTRA_INFO_RESULT_KEY} key and
     * handles the following cases:
     * <ul>
     *     <li>If {@code EXTRA_INFO_ITEM_ADDED} is {@code true}, all input fields are cleared.</li>
     *     <li>If an {@code ident} is provided and the presenter is initialized,
     *         the presenter is updated with the new item identifier and triggers data loading.</li>
     *     <li>If a {@code QUANTITY} value is returned, it is stored in {@code mQuantity}.</li>
     * </ul>
     * <p>
     * The result is expected to be set by {@link ExtraInfoFragment} using:
     * <pre>{@code
     * Bundle result = new Bundle();
     * result.putBoolean(Utils.EXTRA_INFO_ITEM_ADDED, true);
     * result.putString(Utils.ITEM_ID, ident);
     * result.putString(Utils.QUANTITY, quantity);
     * getParentFragmentManager().setFragmentResult(Utils.EXTRA_INFO_RESULT_KEY, result);
     * }</pre>
     */
    private void setFragmentResultListener() {
        getParentFragmentManager().setFragmentResultListener(
                Utils.EXTRA_INFO_RESULT_KEY,
                getViewLifecycleOwner(),
                (requestKey, result) -> {
                    boolean itemAdded = result.getBoolean(Utils.EXTRA_INFO_ITEM_ADDED, false);
                    String returnedItemIdent = result.getString(Utils.ITEM_ID, "");
                    mQuantity = result.getString(Utils.QUANTITY);

                    if (itemAdded) {
                        mBinding.getRoot().post(this::clearFields);
                    } else if (!returnedItemIdent.isEmpty() && mPresenter != null) {
                        mPresenter.setItemId(returnedItemIdent);
                        mPresenter.getSearchedData();
                    }
                }
        );
    }

    /**
     * Adds a shared {@link TextWatcher} to all input fields.
     * <p>
     * The watcher:
     * - Shows or hides the "Add" button depending on whether all fields are filled.
     * - Adds or removes extra bottom padding accordingly.
     */
    private void setTextWatchers() {
        mEditTexts = new EditText[]{
                mBinding.etProductName,
                mBinding.etUnitOfMeasure,
                mBinding.etPrice,
                mBinding.etIdentToShow,
                mBinding.etInitialQuantity,
                mBinding.etQuantity
        };


        TextWatcher watcher = new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                mBinding.fbAdd.setVisibility(areAllFilled() ? VISIBLE : View.GONE);
                mBinding.fbAdd.post(() -> {
                    if (areAllFilled()) {
                        addAdditionalBottomPadding();
                    } else {
                        removeAdditionalBottomPadding();
                    }
                });
            }
        };

        for (EditText editText : mEditTexts) {
            editText.addTextChangedListener(watcher);
        }
    }

    /**
     * Clears all text input fields including the barcode field and those in {@code mEditTexts}.
     * <p>
     * This is typically used to reset the form after submitting or cancelling an operation.
     */
    private void clearFields() {
        if (mEditTexts == null || mBinding == null) {
            return;
        }

        mBinding.etBarcode.setText("");
        for (EditText field : mEditTexts) {
            if (field != null) field.setText("");
        }

        Bundle bundle = getArguments();
        if (bundle != null) bundle.remove(Utils.ITEM_ID);

        updateMenu(false);
    }

    /**
     * Checks whether any of the input fields in {@code mEditTexts} are empty.
     *
     * @return {@code false} if at least one field is empty or {@code mEditTexts} is null; {@code true} otherwise.
     */
    private boolean areAllFilled() {
        if (mEditTexts == null) return false;

        for (EditText field : mEditTexts) {
            if (field == null || field.getText() == null || field.getText().toString().trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether any of the input fields in {@code mEditTexts} have non-empty text.
     *
     * @return {@code true} if at least one field contains input; {@code false} if all are empty or {@code mEditTexts} is null.
     */
    private boolean hasAnyInput() {
        if (mEditTexts == null) {
            return false;
        }

        for (EditText field : mEditTexts) {
            if (field != null && field.getText() != null && !field.getText().toString().trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Parses and validates the quantity entered by the user.
     * <p>
     * Delegates the validation to the presenter using the value from the quantity input field.
     */
    private void validateEnteredQuantity() {
        if (mPresenter != null) {
            mPresenter.validateQuantity(Utils.parseSafeDouble(mBinding.etQuantity));
        }
    }

    /**
     * Sends a new {@link InventoryItem} to the presenter for processing.
     *
     * @param masterItem The {@link MasterItem} associated with the inventory item.
     * @param quantity   The quantity to be added to the inventory.
     */
    private void addInventoryItem(MasterItem masterItem, double quantity) {
        if (mPresenter != null) {
            mPresenter.addInventoryItem(new InventoryItem(masterItem.getIdent(), quantity));
        }
    }

    /**
     * Retrieves the text from the ident input field and requests the corresponding
     * {@link MasterItem} from the presenter if the field is not empty.
     * <p>
     * Does nothing if the presenter is null or the ident field is empty.
     */
    private void findItemByIdent() {
        String ident = Utils.getTextOrEmpty(mBinding.iAlternativeSearch.etIdent);
        if (mPresenter != null && !ident.isEmpty()) {
            mPresenter.getItemByIdent(ident);
        }
    }

    /**
     * Retrieves the text from the alternative ID field and requests the corresponding
     * {@link MasterItem} from the presenter if the field is not empty.
     * <p>
     * Does nothing if the presenter is null or the alternative ID field is empty.
     */
    private void findItemAltId() {
        String altId = Utils.getTextOrEmpty(mBinding.iAlternativeSearch.etAltId);
        if (mPresenter != null && !altId.isEmpty()) {
            mPresenter.getItemByAltId(altId);
        }
    }

    /**
     * Configures the alternative search UI behavior.
     * <p>
     * - Toggles the visibility of the alternative search content with animations
     * and rotates the arrow when the header is clicked.
     * - Adds a shared text watcher to the ident and altId input fields to handle changes
     * via {@link #handleSearchFieldsChanged()}.
     * - Sets click listeners on the input fields to manage focus between them.
     */
    private void setUpAlternativeSearch() {
        mBinding.iAlternativeSearch.llAlternativeSearch.setOnClickListener(v -> {
            boolean isCollapsed = mBinding.iAlternativeSearch.llAlternativeSearchContent.getVisibility() == View.GONE;

            setLayoutEnabled(false);
            if (isCollapsed) {
                ViewAnimationUtils.expand(mBinding.iAlternativeSearch.llAlternativeSearchContent, this::onAnimationEnd);
                ViewAnimationUtils.rotateArrow(mBinding.iAlternativeSearch.ivArrow, true);
            } else {
                ViewAnimationUtils.collapse(mBinding.iAlternativeSearch.llAlternativeSearchContent, this::onAnimationEnd);
                ViewAnimationUtils.rotateArrow(mBinding.iAlternativeSearch.ivArrow, false);
            }
        });

        TextWatcher watcher = new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                handleSearchFieldsChanged();
            }
        };

        mBinding.iAlternativeSearch.etIdent.setOnClickListener(v ->
                setFocusableEditText(mBinding.iAlternativeSearch.etIdent, mBinding.iAlternativeSearch.etAltId));

        mBinding.iAlternativeSearch.etAltId.setOnClickListener(v ->
                setFocusableEditText(mBinding.iAlternativeSearch.etAltId, mBinding.iAlternativeSearch.etIdent));


        mBinding.iAlternativeSearch.etIdent.addTextChangedListener(watcher);
        mBinding.iAlternativeSearch.etAltId.addTextChangedListener(watcher);
    }

    /**
     * Enables or disables the alternative search layout.
     *
     * @param enabled True to enable the layout, false to disable it.
     */
    private void setLayoutEnabled(boolean enabled) {
        if (mBinding != null) {
            mBinding.iAlternativeSearch.llAlternativeSearch.setEnabled(enabled);
        }
    }

    /**
     * Called when an expand/collapse animation ends to re-enable the layout.
     */
    private void onAnimationEnd() {
        setLayoutEnabled(true);
    }

    /**
     * Sets focus to one EditText and disables focus on another.
     * <p>
     * The keyboard is shown for the focused EditText.
     *
     * @param toEnable  The EditText to focus and enable input.
     * @param toDisable The EditText to disable input.
     */
    private void setFocusableEditText(EditText toEnable, EditText toDisable) {
        toEnable.setFocusable(true);
        toEnable.setFocusableInTouchMode(true);

        toDisable.setFocusable(false);
        toDisable.setFocusableInTouchMode(false);

        toEnable.requestFocus();
        toggleKeyboard(toEnable, true);
    }

    /**
     * Ensures only one of ident or altId has a value at a time while typing.
     */
    private void handleSearchFieldsChanged() {
        String ident = Utils.getTextOrEmpty(mBinding.iAlternativeSearch.etIdent);
        String altId = Utils.getTextOrEmpty(mBinding.iAlternativeSearch.etAltId);

        if (mBinding.iAlternativeSearch.etIdent.hasFocus() && !TextUtils.isEmpty(ident) && !TextUtils.isEmpty(altId)) {
            mBinding.iAlternativeSearch.etAltId.setText("");
        } else if (mBinding.iAlternativeSearch.etAltId.hasFocus() && !TextUtils.isEmpty(altId) && !TextUtils.isEmpty(ident)) {
            mBinding.iAlternativeSearch.etIdent.setText("");
        }
    }

    /**
     * Creates a listener for the keyboard "search" action.
     * <p>
     * When the user presses the search action on the keyboard, the provided {@link Runnable}
     * is executed.
     *
     * @param action The action to perform when the search editor action is triggered.
     * @return A configured {@link TextView.OnEditorActionListener}.
     */
    private TextView.OnEditorActionListener createSearchListener(Runnable action) {
        return (v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                action.run();
                return false;
            }
            return true;
        };
    }

    /**
     * Adds extra bottom padding to the scrollable container to accommodate the "Add" button,
     * preventing it from overlapping content.
     */
    private void addAdditionalBottomPadding() {
        if (mBinding != null) {
            int fabHeight = mBinding.fbAdd.getHeight();
            // Use margin_small since RecyclerView already has 8dp bottom padding
            float margin = getResources().getDimension(R.dimen.margin_small);
            int bottomPadding = fabHeight + (int) (margin);

            int left = mBinding.nsvContainer.getPaddingLeft();
            int top = mBinding.nsvContainer.getPaddingTop();
            int right = mBinding.nsvContainer.getPaddingRight();

            mBinding.nsvContainer.setPadding(left, top, right, bottomPadding);
        }
    }

    /**
     * Removes the extra bottom padding from the scrollable container,
     * restoring its original layout.
     */
    private void removeAdditionalBottomPadding() {
        if (mBinding != null) {
            int left = mBinding.nsvContainer.getPaddingLeft();
            int top = mBinding.nsvContainer.getPaddingTop();
            int right = mBinding.nsvContainer.getPaddingRight();
            mBinding.nsvContainer.setPadding(left, top, right, 0);
        }
    }
}
