package com.metalac.scanner.app.view.configuration;

import static com.metalac.scanner.app.utils.Utils.DEVICE_NAME_NUM_OF_CHAR;
import static com.metalac.scanner.app.utils.Utils.STORE_CODE_NUM_OF_CHAR;

import android.content.DialogInterface;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.viewbinding.ViewBinding;

import com.metalac.scanner.app.Injection;
import com.metalac.scanner.app.helpers.DialogConfig;
import com.metalac.scanner.app.helpers.DialogHelper;
import com.metalac.scanner.app.data.source.PrefManager;
import com.metalac.scanner.app.R;
import com.metalac.scanner.app.databinding.FragmentConfigurationBinding;
import com.metalac.scanner.app.view.BaseFragment;
import com.metalac.scanner.app.view.BasePresenter;
import com.metalac.scanner.app.view.BaseView;
import com.metalac.scanner.app.view.MainActivity;
import com.metalac.scanner.app.view.ScannerReaderError;
import com.metalac.scanner.app.view.configuration.contracts.ConfigurationContract;
import com.metalac.scanner.app.view.configuration.presenters.ConfigurationPresenter;

public class ConfigurationFragment extends BaseFragment implements ConfigurationContract.View {
    private FragmentConfigurationBinding mBinding;
    private AlertDialog mConfirmationDialog;
    private AlertDialog mProgressDialog;
    private ConfigurationPresenter mPresenter;

    @Override
    protected void onCreateView(ViewBinding viewBinding, BasePresenter basePresenter) {
        mBinding = (FragmentConfigurationBinding) viewBinding;
        MainActivity activity = (MainActivity) getActivity();
        mPresenter = (ConfigurationPresenter) basePresenter;
        if (activity != null) {
            activity.setupToolbar(R.id.ConfigurationFragment, getString(R.string.configuration));
        }

        configureViews();
        configureInputs(mBinding.iConfigInfo.etDeviceName);
        configureInputs(mBinding.iConfigInfo.etStoreCode);
    }

    /**
     * Configures an EditText to prevent all whitespace characters (spaces, tabs, etc.)
     * from being entered, while preserving any existing InputFilters.
     *
     * <p>This method adds an InputFilter that blocks any whitespace from being typed.
     * Existing filters, such as maxLength defined in XML, are retained.</p>
     *
     * @param editText the EditText to apply the filter to
     */
    private void configureInputs(EditText editText) {
        InputFilter noSpacesFilter = (source, start, end, dest, dstart, dend) -> {
            for (int i = start; i < end; i++) {
                if (Character.isWhitespace(source.charAt(i))) {
                    return "";
                }
            }
            return null;
        };

        InputFilter[] existingFilters = editText.getFilters();
        InputFilter[] newFilters = new InputFilter[existingFilters.length + 1];
        System.arraycopy(existingFilters, 0, newFilters, 0, existingFilters.length);
        newFilters[existingFilters.length] = noSpacesFilter;

        editText.setFilters(newFilters);
    }

    @Override
    protected ViewBinding onBindLayout() {
        return FragmentConfigurationBinding.inflate(LayoutInflater.from(getContext()));
    }

    @Override
    protected BaseView onBindContract() {
        return this;
    }

    @Override
    protected BasePresenter onBindPresenter() {
        return new ConfigurationPresenter(Injection.provideMasterItemRepository(requireContext()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
        mConfirmationDialog = null;
        mProgressDialog = null;
        mPresenter = null;
    }

    @Override
    public void navigateToMenu() {
        if (mBinding != null) {
            navigate(mBinding.getRoot(), R.id.ConfigurationFragment, R.id.action_ConfigurationFragment_to_MenuFragment, null);
        }
    }

    @Override
    public void showErrorDialog(@NonNull ScannerReaderError scannerReaderError) {
        DialogHelper.showErrorDialog(new DialogConfig(getContext(), getLayoutInflater(), scannerReaderError));
    }

    @Override
    public void showProgress() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }

        if (mProgressDialog == null) {
            DialogConfig config = new DialogConfig(getContext(), getLayoutInflater())
                    .setTitle(getString(R.string.loading));
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

    /**
     * Configures the initial state and listeners of the views in the fragment.
     */
    private void configureViews() {
        setupInitialUIState();
        setupDeviceNameTextWatcher();
        setupStoreCodeTextWatcher();
        setupConfirmButtonClickListener();
    }

    /**
     * Sets the initial UI state depending on whether the device name and store code are already stored.
     * - Enables/disables input fields accordingly.
     * - Sets stored values in input fields if available.
     */
    private void setupInitialUIState() {
        String deviceName = PrefManager.getDeviceName();
        String storeCode = PrefManager.getDeviceStoreCode();

        if (deviceName.isEmpty()) {
            initiateDeviceNameEntryState();
        } else {
            mBinding.iConfigInfo.etDeviceName.setText(deviceName);
            mBinding.iConfigInfo.etDeviceName.setEnabled(false);
            initiateStoreCodeEntryState(storeCode);
        }
    }

    /**
     * Prepares the UI for device name entry:
     * - Enables device name EditText.
     * - Disables store code EditText.
     * - Sets confirm button text and disables it initially.
     * - Focuses and opens the keyboard on the device name input.
     */
    private void initiateDeviceNameEntryState() {
        mBinding.iConfigInfo.etDeviceName.setEnabled(true);
        mBinding.iConfigInfo.etStoreCode.setEnabled(false);
        mBinding.btConfirmStoreCode.setText(R.string.confirm_device_name);
        mBinding.btConfirmStoreCode.setEnabled(false);
        toggleKeyboard(mBinding.iConfigInfo.etDeviceName, true);
    }


    /**
     * Prepares the UI for store code entry:
     * - Enables store code EditText and fills it with current store code.
     * - Sets confirm button text and enables it only if the store code is not empty.
     * - Focuses and opens the keyboard on the store code input.
     *
     * @param storeCode The current store code to prefill.
     */
    private void initiateStoreCodeEntryState(@NonNull String storeCode) {
        mBinding.iConfigInfo.etStoreCode.setEnabled(true);
        mBinding.iConfigInfo.etStoreCode.setText(storeCode);
        mBinding.btConfirmStoreCode.setText(R.string.confirm_store_code);
        mBinding.btConfirmStoreCode.setEnabled(!storeCode.isEmpty());
        toggleKeyboard(mBinding.iConfigInfo.etStoreCode, true);
    }

    /**
     * Sets up a TextWatcher on the device name EditText to enable the confirm button
     * only when the input length matches the required number of characters.
     */
    private void setupDeviceNameTextWatcher() {
        mBinding.iConfigInfo.etDeviceName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // not in use
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //not in use
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mBinding.btConfirmStoreCode.setEnabled(editable.toString().trim().length() >= DEVICE_NAME_NUM_OF_CHAR);
            }
        });
    }

    /**
     * Sets up a TextWatcher on the store code EditText to enable the confirm button
     * only when the input length matches the required number of characters.
     */
    private void setupStoreCodeTextWatcher() {
        mBinding.iConfigInfo.etStoreCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // not in use
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // not in use
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mBinding.btConfirmStoreCode.setEnabled(editable.toString().trim().length() == STORE_CODE_NUM_OF_CHAR);
            }
        });
    }

    /**
     * Sets a click listener on the confirm button to show a confirmation dialog.
     */
    private void setupConfirmButtonClickListener() {
        mBinding.btConfirmStoreCode.setOnClickListener(view -> showConfirmationDialogIfNeeded());
    }

    /**
     * Displays a confirmation dialog based on the current configuration state of the device.
     * <p>
     * This method checks whether the device name has been set using {@link PrefManager#isDeviceNameSet()}:
     * <ul>
     *     <li>If not set, it shows a confirmation dialog for the device name using the value from the corresponding EditText.
     *     On confirmation, it executes {@link #handleDeviceNameConfirmation()}.</li>
     *     <li>If the device name is already set, it checks the store code:
     *         <ul>
     *             <li>If the entered store code matches the stored one, it immediately proceeds by calling {@link #handleStoreCodeConfirmation()} with no dialog.</li>
     *             <li>If it doesn't match, it shows a confirmation dialog for the store code and triggers {@link #handleStoreCodeConfirmation()} upon confirmation.</li>
     *         </ul>
     *     </li>
     * </ul>
     * </p>
     * Before showing a new dialog, it dismisses any currently visible one via {@link #dismissCurrentDialog()}.
     */
    private void showConfirmationDialogIfNeeded() {
        dismissCurrentDialog();

        String title;
        String subtitle;
        Runnable positiveAction;

        if (!PrefManager.isDeviceNameSet()) {
            title = getString(R.string.device_name_confirmation_dialog_title);
            String deviceName = getEtText(mBinding.iConfigInfo.etDeviceName);
            subtitle = getString(R.string.device_name_confirmation_dialog_subtitle, deviceName);
            positiveAction = this::handleDeviceNameConfirmation;
        } else {
            String storeCodeInput = getEtText(mBinding.iConfigInfo.etStoreCode);

            if (storeCodeInput.equals(PrefManager.getDeviceStoreCode())) {
                handleStoreCodeConfirmation();
                return;
            }

            title = getString(R.string.store_code_confirmation_dialog_title);
            String storeCode = getEtText(mBinding.iConfigInfo.etStoreCode);
            subtitle = getString(R.string.store_code_confirmation_dialog_subtitle, storeCode);
            positiveAction = this::handleStoreCodeConfirmation;
        }

        createAndShowDialog(title, subtitle, positiveAction);
    }

    /**
     * Dismisses the current confirmation dialog if it is showing.
     */
    private void dismissCurrentDialog() {
        if (mConfirmationDialog != null && mConfirmationDialog.isShowing()) {
            mConfirmationDialog.dismiss();
        }
        mConfirmationDialog = null;
    }

    /**
     * Creates and shows an attention dialog with given title, subtitle, confirm, and change buttons.
     *
     * @param title          The dialog title.
     * @param subtitle       The dialog message/subtitle.
     * @param positiveAction The action to run when the positive (confirm) button is clicked.
     */
    private void createAndShowDialog(String title, String subtitle, Runnable positiveAction) {
        DialogConfig dialogConfig = new DialogConfig(getContext(), getLayoutInflater());

        dialogConfig.setTitle(title)
                .setSubtitle(subtitle)
                .setPositiveButton(getString(R.string.confirm))
                .setNegativeButton(getString(R.string.change))
                .setClickListener((dialogInterface, which) -> {
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        positiveAction.run();
                    } else {
                        dismissCurrentDialog();
                    }
                });

        mConfirmationDialog = DialogHelper.createAttentionDialog(dialogConfig);

        if (mConfirmationDialog != null) {
            mConfirmationDialog.show();
        }
    }

    /**
     * Handles confirmation of the device name input.
     * If valid, saves the device name and updates the UI to enable store code entry.
     */
    private void handleDeviceNameConfirmation() {
        String deviceNameInput = getEtText(mBinding.iConfigInfo.etDeviceName);
        if (deviceNameInput.length() >= DEVICE_NAME_NUM_OF_CHAR) {
            PrefManager.setDeviceName(deviceNameInput);

            mBinding.iConfigInfo.etDeviceName.setEnabled(false);
            mBinding.iConfigInfo.etStoreCode.setEnabled(true);
            mBinding.btConfirmStoreCode.setText(R.string.confirm_store_code);
            mBinding.btConfirmStoreCode.setEnabled(false);
        }
    }

    /**
     * Validates and handles the confirmation of the store code input.
     * <p>
     * Behavior:
     * <ul>
     *   <li>If the input has the expected length and no store code is set yet,
     *       it saves the new store code and navigates to the menu fragment.</li>
     *   <li>If a store code already exists, it delegates handling to the presenter
     *       (to manage store code change logic).</li>
     *   <li>If the input is invalid, no action is taken.</li>
     * </ul>
     */
    private void handleStoreCodeConfirmation() {
        String storeCodeInput = getEtText(mBinding.iConfigInfo.etStoreCode).trim();

        if (storeCodeInput.length() != STORE_CODE_NUM_OF_CHAR) {
            return; // Invalid input length, ignore
        }

        if (PrefManager.getDeviceStoreCode().isEmpty()) {
            PrefManager.setDeviceStoreCode(storeCodeInput);
            navigate(
                    mBinding.getRoot(),
                    R.id.ConfigurationFragment,
                    R.id.action_ConfigurationFragment_to_MenuFragment,
                    null
            );
        } else if (mPresenter != null) {
            mPresenter.changeStoreCode(storeCodeInput);
        }
    }

    /**
     * Retrieves the text content of the specified EditText safely.
     *
     * @param editText The EditText to get text from.
     * @return The string content of the EditText, or an empty string if null.
     */
    private String getEtText(EditText editText) {
        Editable editable = editText.getText();
        if (editable == null) {
            return "";
        }
        return editable.toString().trim();
    }
}
