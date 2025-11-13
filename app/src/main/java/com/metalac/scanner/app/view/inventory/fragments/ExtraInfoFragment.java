package com.metalac.scanner.app.view.inventory.fragments;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.inputmethod.EditorInfo;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.navigation.Navigation;
import androidx.viewbinding.ViewBinding;

import com.metalac.scanner.app.helpers.DateHelper;
import com.metalac.scanner.app.helpers.DialogConfig;
import com.metalac.scanner.app.helpers.DialogHelper;
import com.metalac.scanner.app.Injection;
import com.metalac.scanner.app.R;
import com.metalac.scanner.app.helpers.SimpleTextWatcher;
import com.metalac.scanner.app.utils.Utils;
import com.metalac.scanner.app.databinding.FragmentExtraInfoBinding;
import com.metalac.scanner.app.models.DamageInfo;
import com.metalac.scanner.app.models.InventoryItem;
import com.metalac.scanner.app.models.MasterItem;
import com.metalac.scanner.app.view.BaseFragment;
import com.metalac.scanner.app.view.BasePresenter;
import com.metalac.scanner.app.view.BaseView;
import com.metalac.scanner.app.view.MainActivity;
import com.metalac.scanner.app.view.ScannerReaderError;
import com.metalac.scanner.app.view.inventory.contracts.ExtraInfoContract;
import com.metalac.scanner.app.view.inventory.presenters.ExtraInfoPresenter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class ExtraInfoFragment extends BaseFragment implements ExtraInfoContract.View {
    private FragmentExtraInfoBinding mBinding;
    private ExtraInfoPresenter mPresenter;
    private AlertDialog mQuantityWarningDialog;
    private AlertDialog mInvalidQuantityDialog;
    private EditText[] mEditTexts;
    private DatePickerDialog mDatePickerDialog;
    private boolean mIsUpdateAction = false;

    private String initialExpDate = "";
    private String initialDamage = "";
    private String initialNote = "";

    @Override
    protected void onCreateView(ViewBinding viewBinding, BasePresenter basePresenter) {
        mBinding = (FragmentExtraInfoBinding) viewBinding;
        mPresenter = (ExtraInfoPresenter) basePresenter;
        MainActivity activity = (MainActivity) getActivity();

        mPresenter.loadCurrentList();

        Bundle bundle = getArguments();

        ExtraInfoFragmentArgs args = ExtraInfoFragmentArgs.fromBundle(bundle);
        String masterIdent = args.getMasterId();
        String quantity = args.getQuantity();

        mBinding.getRoot().post(() -> {
            if (mBinding != null) {
                mBinding.etQuantity.setText(quantity);
            }
        });

        mIsUpdateAction = args.getIsUpdateAction();

        String productName = args.getProductName();
        if (productName.isEmpty()) {
            mBinding.tilProductName.setVisibility(GONE);
        } else {
            mBinding.tilProductName.setVisibility(VISIBLE);
            mBinding.etProductName.setText(productName);
        }

        if (activity != null) {
            activity.setupToolbar(R.id.ExtraInfoFragment, getString(R.string.extra_info));
        }

        if (mIsUpdateAction) {
            mBinding.tilQuantity.setVisibility(GONE);
        } else setupFieldChangeListeners();
        setupInputFields();
        setButtonClick();

        if (!masterIdent.isEmpty()) {
            mPresenter.loadAllMasterData(args.getInventoryItemId(), masterIdent);
        }
    }

    @Override
    protected ViewBinding onBindLayout() {
        return FragmentExtraInfoBinding.inflate(LayoutInflater.from(getContext()));
    }

    @Override
    protected BaseView onBindContract() {
        return this;
    }

    @Override
    protected BasePresenter onBindPresenter() {
        return new ExtraInfoPresenter(Injection.provideMasterItemRepository(requireContext()),
                Injection.provideInventoryItemRepository(requireContext()),
                Injection.provideInventoryListRepository(requireContext()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (!mIsUpdateAction) {
            setResultArgs();
        }

        mBinding = null;
        mEditTexts = null;
        mPresenter = null;
        mQuantityWarningDialog = null;
        mInvalidQuantityDialog = null;
        mDatePickerDialog = null;
    }

    @Override
    public void goBack() {
        clearFields();
        Navigation.findNavController(mBinding.getRoot()).navigateUp();
    }

    /**
     * Displays a non-cancelable error dialog if the unit of measure (UoM) or quantity is zero or less.
     * <p>
     * The dialog informs the user that the entered quantity is invalid (negative or zero),
     * provides an explanation, and allows the user to acknowledge the message with an "OK" button.
     * When the user confirms, the quantity input field is cleared and focused for correction.
     * </p>
     * <p>
     * If the dialog has already been created, it is simply shown again.
     * </p>
     */
    @Override
    public void showDialogIfUomIsZeroOrLess() {
        if (mInvalidQuantityDialog == null) {
            Context context = getContext();
            if (context == null) {
                return;
            }
            DialogConfig dialogConfig = new DialogConfig(context, getLayoutInflater());
            dialogConfig.setTitle(getString(R.string.negative_quantity_title));
            dialogConfig.setSubtitle(getString(R.string.negative_quantity_subtitle));
            dialogConfig.setPositiveButton(getString(R.string.ok));
            dialogConfig.setCancelable(false);
            dialogConfig.setClickListener((dialog, which) -> {
                mBinding.etQuantity.setText("");
                mBinding.etQuantity.requestFocus();
            });
            mInvalidQuantityDialog = DialogHelper.createErrorDialog(dialogConfig);
            if (mInvalidQuantityDialog != null) {
                mInvalidQuantityDialog.show();
            }
        } else {
            mInvalidQuantityDialog.show();
        }
    }

    @Override
    public void showInitialData(InventoryItem inventoryItem) {
        if (mBinding != null) {
            String expDate = inventoryItem.getExpDate();
            if (expDate != null && !expDate.isEmpty()) {
                mBinding.etExpirationDate.setText(expDate);
            }

            mBinding.actDamage.setText(inventoryItem.getDamageInfoString(), false);
            mBinding.etNote.setText(inventoryItem.getNote());

            // Save initial values for change tracking
            initialExpDate = Utils.getStringOrNull(mBinding.etExpirationDate);
            initialDamage = Utils.getStringOrNull(mBinding.actDamage);
            initialNote = Utils.getStringOrNull(mBinding.etNote);

            // Setup listeners to monitor field changes
            setupFieldChangeListeners();
        }
    }

    /**
     * Displays a non-cancelable warning dialog when the entered quantity may be unusually high or questionable.
     * <p>
     * The dialog shows a warning title and description, asking the user to confirm or cancel the action.
     * If the user confirms (presses the positive button), the item is added with the specified quantity.
     * If the user cancels, the quantity input field is cleared and focused again for correction.
     * </p>
     * <p>
     * This dialog is shown only once and reused if already created.
     * </p>
     *
     * @param masterItem The {@link MasterItem} being added to the inventory.
     * @param quantity   The quantity entered by the user, potentially requiring confirmation.
     */
    @Override
    public void showQuantityWarningDialog(@NonNull MasterItem masterItem, double quantity) {
        if (mQuantityWarningDialog == null) {
            Context context = getContext();
            if (context == null) {
                return;
            }
            DialogConfig dialogConfig = new DialogConfig(context, getLayoutInflater());
            dialogConfig.setTitle(getString(R.string.warning_quantity_title));
            dialogConfig.setSubtitle(getString(R.string.warning_quantity_subtitle));
            dialogConfig.setPositiveButton(getString(R.string.confirm));
            dialogConfig.setNegativeButton(getString(R.string.cancel));
            dialogConfig.setCancelable(false);
            dialogConfig.setClickListener((dialog, which) -> {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    addItem(masterItem, quantity);
                } else {
                    if (mBinding != null) {
                        mBinding.etQuantity.setText("");
                        mBinding.etQuantity.requestFocus();
                    }
                }
            });
            mQuantityWarningDialog = DialogHelper.createAttentionDialog(dialogConfig);
            if (mQuantityWarningDialog != null) {
                mQuantityWarningDialog.show();
            }
        } else {
            mQuantityWarningDialog.show();
        }
    }

    @Override
    public void showErrorDialog(@NonNull ScannerReaderError scannerReaderError) {
        DialogHelper.showErrorDialog(new DialogConfig(getContext(), getLayoutInflater(), scannerReaderError));
    }

    /**
     * Populates two linked AutoCompleteTextViews with damage codes and their descriptions.
     * Adds default prompt items at the top of both dropdowns and ensures selection
     * consistency between them (i.e., selecting a code automatically selects the matching
     * description and vice versa).
     * <p>
     * If the default option is selected (position 0), both fields are cleared.
     *
     * @param damageInfo A list of {@link DamageInfo} objects containing damage codes and their descriptions.
     */
    @Override
    public void populateDamageInfo(@NonNull ArrayList<DamageInfo> damageInfo) {
        ArrayList<String> damageInfoStrings = new ArrayList<>();

        for (DamageInfo info : damageInfo) {
            damageInfoStrings.add(info.getDamageInfoString());
        }

        Utils.setupDropdown(getContext(), mBinding.actDamage, damageInfoStrings, getString(R.string.damage));

        mBinding.actDamage.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 0) {
                mBinding.actDamage.setText("");
            }
        });
    }

    @Override
    public void showNoDamageInfo() {
        if (mBinding != null) {
            mBinding.tilDamage.setEnabled(false);
        }
    }

    @Override
    public void configureQuantityInput(int decimalPlaces) {
        if (mBinding != null) {
            Utils.configureInput(mBinding.etQuantity, decimalPlaces);
        }
    }

    @Override
    public void showProgress() {
        // not in use
    }

    @Override
    public void hideProgress() {
        // not in use
    }

    /**
     * Prepares an array of EditText input fields that will be used for validation or focus handling.
     */
    private void setupInputFields() {
        mBinding.etExpirationDate.setOnClickListener(v -> showDatePicker());

        mEditTexts = new EditText[]{
                mBinding.etExpirationDate,
                mBinding.actDamage,
                mBinding.etNote,
                mBinding.etQuantity
        };
    }

    /**
     * Sets up the click listener for the floating action button and attaches
     * "Done" action listeners for quantity and note input fields.
     */
    private void setButtonClick() {
        handleAddOrUpdateItem();
        mBinding.etQuantity.setOnEditorActionListener(
                createAddOrUpdateListener(this::handleAddOrUpdateItem)
        );
        mBinding.etNote.setOnEditorActionListener(
                createAddOrUpdateListener(this::handleAddOrUpdateItem)
        );
    }

    /**
     * Creates an OnEditorActionListener that runs the given action when the
     * IME "Done" button is pressed.
     *
     * @param action The Runnable to execute on "Done".
     * @return A configured OnEditorActionListener.
     */
    private TextView.OnEditorActionListener createAddOrUpdateListener(Runnable action) {
        return (v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                action.run();
                return false;
            }
            return true;
        };
    }

    /**
     * Updates the floating button icon and sets its click listener to either
     * add a new item or update an existing one with extra info.
     */
    private void handleAddOrUpdateItem() {
        if (mBinding != null && mPresenter != null) {
            mBinding.fbAdd.setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), mIsUpdateAction ? R.drawable.ic_update_done : R.drawable.ic_add)
            );
            mBinding.fbAdd.setOnClickListener(v -> {
                mPresenter.setExtraInfoItemAdded(true);

                if (!mIsUpdateAction) {
                    mPresenter.validateQuantity(Utils.parseSafeDouble(mBinding.etQuantity),
                            Utils.getStringOrNull(mBinding.etExpirationDate),
                            Utils.extractDamageCode(mBinding.actDamage),
                            Utils.getStringOrNull(mBinding.etNote));
                } else {
                    mPresenter.updateItem(
                            Utils.getStringOrNull(mBinding.etExpirationDate),
                            Utils.extractDamageCode(mBinding.actDamage),
                            Utils.getStringOrNull(mBinding.etNote));
                }
            });
        }
    }

    private void addItem(@NonNull MasterItem masterItem, double quantity) {
        if (mPresenter != null) {
            mPresenter.addItem(createInventoryItem(masterItem, quantity));
        }
    }

    @NonNull
    private InventoryItem createInventoryItem(@NonNull MasterItem masterItem, double quantity) {
        InventoryItem inventoryItem = new InventoryItem(masterItem.getIdent(), quantity);
        String expDate = Utils.getStringOrNull(mBinding.etExpirationDate);
        String damageCode = Utils.extractDamageCode(mBinding.actDamage);
        String note = Utils.getStringOrNull(mBinding.etNote);
        inventoryItem.addAdditionallyData(expDate, damageCode, note);
        return inventoryItem;
    }

    private void clearFields() {
        if (mEditTexts == null) {
            return;
        }

        for (EditText field : mEditTexts) {
            if (field != null) {
                field.setText("");
            }
        }
    }

    private void setResultArgs() {
        if (mPresenter == null) {
            return;
        }

        mPresenter.setResultArgs((itemIdent, extraInfoItemAdded) -> {
            Bundle result = new Bundle();
            result.putBoolean(Utils.EXTRA_INFO_ITEM_ADDED, extraInfoItemAdded);
            result.putString(Utils.ITEM_ID, itemIdent);
            result.putString(Utils.QUANTITY, Utils.getStringOrNull(mBinding.etQuantity));
            getParentFragmentManager().setFragmentResult(Utils.EXTRA_INFO_RESULT_KEY, result);
        });
    }

    /**
     * Monitors all relevant input fields and enables the button only if any value has changed.
     */
    private void setupFieldChangeListeners() {
        TextWatcher watcher = new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                checkIfFieldsChanged();
            }
        };

        mBinding.etExpirationDate.addTextChangedListener(watcher);
        mBinding.actDamage.addTextChangedListener(watcher);
        mBinding.etNote.addTextChangedListener(watcher);
        mBinding.etQuantity.addTextChangedListener(watcher);
    }

    /**
     * Checks if any of the input fields (expiration date, damage code, note, or quantity)
     * have changed compared to their initial values and updates the visibility of the
     * floating action button accordingly.
     *
     * <p>If the quantity field is visible, the button is shown only when both the
     * quantity is not empty and other fields have changed. Otherwise, the button
     * is shown whenever any field has changed.</p>
     */
    private void checkIfFieldsChanged() {
        String currentExpDate = Objects.toString(Utils.getStringOrNull(mBinding.etExpirationDate), "");
        String currentDamage = Objects.toString(Utils.getStringOrNull(mBinding.actDamage), "");
        String currentNote = Objects.toString(Utils.getStringOrNull(mBinding.etNote), "");
        String quantity = Objects.toString(Utils.getStringOrNull(mBinding.etQuantity), "");

        boolean changed = !currentExpDate.equals(initialExpDate) ||
                !currentDamage.equals(initialDamage) ||
                !currentNote.equals(initialNote);

        boolean showButton = mBinding.tilQuantity.getVisibility() != GONE ?
                !quantity.isEmpty() && changed
                : changed;

        mBinding.fbAdd.setVisibility(showButton ? VISIBLE : GONE);
    }

    /**
     * Opens a date picker dialog and updates the expiration date field.
     */
    private void showDatePicker() {
        if (mDatePickerDialog != null && mDatePickerDialog.isShowing()) {
            return;
        }

        Calendar calendar = Calendar.getInstance();
        String currentText = Utils.getStringOrNull(mBinding.etExpirationDate);
        if (currentText != null && !currentText.isEmpty()) {
            Date parsedDate = DateHelper.parseExpDateFromString(currentText);
            if (parsedDate != null) {
                calendar.setTime(parsedDate);
            }
        }

        mDatePickerDialog = new DatePickerDialog(
                requireContext(),
                R.style.SpinnerDatePickerDialogTheme,
                null,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        mDatePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(android.R.string.ok),
                (dialog, which) -> {
                    DatePicker datePicker = mDatePickerDialog.getDatePicker();
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                    Date date = selectedDate.getTime();

                    String formattedDate = DateHelper.formatExpDateToString(date);
                    mBinding.etExpirationDate.setText(formattedDate);
                    mDatePickerDialog = null;
                });

        mDatePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(android.R.string.cancel),
                (dialog, which) -> mDatePickerDialog = null);

        mDatePickerDialog.setOnShowListener(dialog -> {
            int customColor = ContextCompat.getColor(requireContext(), R.color.main_color);
            mDatePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(customColor);
            mDatePickerDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(customColor);
        });

        mDatePickerDialog.setOnDismissListener(dialog -> mDatePickerDialog = null);

        mDatePickerDialog.show();
    }
}
