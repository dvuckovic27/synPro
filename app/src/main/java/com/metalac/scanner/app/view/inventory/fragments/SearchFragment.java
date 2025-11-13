package com.metalac.scanner.app.view.inventory.fragments;

import static com.google.android.material.textfield.TextInputLayout.END_ICON_NONE;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.viewbinding.ViewBinding;

import com.metalac.scanner.app.Injection;
import com.metalac.scanner.app.R;
import com.metalac.scanner.app.utils.Utils;
import com.metalac.scanner.app.models.QueryMasterItem;
import com.metalac.scanner.app.databinding.FragmentSearchBinding;
import com.metalac.scanner.app.view.BaseFragment;
import com.metalac.scanner.app.view.BasePresenter;
import com.metalac.scanner.app.view.BaseView;
import com.metalac.scanner.app.view.MainActivity;
import com.metalac.scanner.app.view.inventory.contracts.SearchContract;
import com.metalac.scanner.app.view.inventory.presenters.SearchPresenter;

import java.util.ArrayList;

public class SearchFragment extends BaseFragment implements SearchContract.View {
    private FragmentSearchBinding mBinding;

    @Override
    protected void onCreateView(ViewBinding viewBinding, BasePresenter basePresenter) {
        mBinding = (FragmentSearchBinding) viewBinding;
        MainActivity activity = (MainActivity) getActivity();
        SearchPresenter mPresenter = (SearchPresenter) basePresenter;

        if (activity != null) {
            activity.setupToolbar(R.id.SearchFragment, getString(R.string.search));
        }

        mBinding.btMasterData.setOnClickListener(v -> openMasterData());
        mBinding.btDataList.setOnClickListener(v -> openInventoryData());

        mPresenter.getUnitOfMeasure();
    }

    private void openInventoryData() {
        if (mBinding == null) return;
        Bundle bundle = new Bundle();
        populateBundle(bundle);
        navigate(mBinding.getRoot(), R.id.SearchFragment, R.id.action_SearchFragment_to_SearchedInventoryDataFragment, bundle);
    }

    /**
     * Navigates to the {@link SearchedDataFragment} with a bundle of query parameters.
     * - Retrieves input data from the UI fields and populates it into a {@link Bundle}.
     * - Calls the `navigate` method to transition to the next fragment, passing the bundle with the required query parameters.
     */
    private void openMasterData() {
        if (mBinding == null) return;
        Bundle bundle = new Bundle();
        populateBundle(bundle);
        navigate(mBinding.getRoot(), R.id.SearchFragment, R.id.action_SearchFragment_to_SearchedDataFragment, bundle);
    }

    /**
     * Collects data from input fields and adds a {@link QueryMasterItem} to the given {@link Bundle}.
     * This includes values like product ID, name, unit, price, barcodes, and program info.
     * The created item is stored in the bundle using the {@link Utils#QUERY_MASTER_ITEM} key.
     *
     * @param bundle The bundle where the item data will be stored.
     */
    private void populateBundle(@NonNull Bundle bundle) {
        String ident = Utils.getStringOrNull(mBinding.etIdEnt);

        String productName = Utils.getStringOrNull(mBinding.etProductName);

        String measureUnit = Utils.getStringOrNull(mBinding.etMeasureUnit);

        String productPriceText = Utils.getStringOrNull(mBinding.etProductPrice);

        String altId1 = Utils.getStringOrNull(mBinding.etAltId1);

        String altId2 = Utils.getStringOrNull(mBinding.etAltId2);

        String barcode = Utils.getStringOrNull(mBinding.etBarcode);

        String salesProgram = Utils.getStringOrNull(mBinding.etSalesProgram);

        String procurementProgram = Utils.getStringOrNull(mBinding.etProcurementProgram);

        double price;
        try {
            price = (productPriceText != null && !productPriceText.isEmpty()) ? Double.parseDouble(productPriceText) : 0.0;
        } catch (NumberFormatException ignored) {
            price = 0.0;
        }

        QueryMasterItem queryMasterItem = new QueryMasterItem.QueryMasterBuilder()
                .setIdent(ident)
                .setName(productName)
                .setUnitOfMeasure(measureUnit)
                .setPrice(price)
                .setAltCode1(altId1)
                .setAltCode2(altId2)
                .setBarcode(barcode)
                .setSalesProgram(salesProgram)
                .setPurchaseProgram(procurementProgram)
                .setActive(mBinding.cbActive.isChecked() ? 1 : 0)
                .setAccounting(mBinding.cbBookkeeping.isChecked() ? 1 : 0)
                .setFilterText("")
                .build();

        bundle.putParcelable(Utils.QUERY_MASTER_ITEM, queryMasterItem);
    }

    @Override
    protected ViewBinding onBindLayout() {
        return FragmentSearchBinding.inflate(LayoutInflater.from(getContext()));
    }

    @Override
    protected BaseView onBindContract() {
        return this;
    }

    @Override
    protected BasePresenter onBindPresenter() {
        return new SearchPresenter(Injection.provideMasterItemRepository(requireContext()));
    }

    /**
     * Callback invoked when the list of unit of measures is loaded.
     * Populates the unit of measure dropdown with the provided list,
     * including a default prompt at the top. Clears the selection if the default item is selected.
     *
     * @param unitOfMeasuresList A list of available unit of measure strings.
     */
    @Override
    public void onUnitOfMeasureLoaded(ArrayList<String> unitOfMeasuresList) {
        Utils.setupDropdown(getContext(), mBinding.etMeasureUnit, unitOfMeasuresList, getString(R.string.unit_of_measure));

        mBinding.etMeasureUnit.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 0) {
                mBinding.etMeasureUnit.setText("");
            }
        });
    }

    /**
     * Called when loading the unit of measures fails.
     * - Sets the `etMeasureUnit` field to be focusable and editable again.
     * - Changes the input type to `TYPE_CLASS_TEXT` to allow the user to manually enter data if needed.
     * - Removes any end icon from the `tillMeasureUnit` field to reset the UI state.
     */
    @Override
    public void onUnitOfMeasureFailed() {
        mBinding.etMeasureUnit.setFocusable(true);
        mBinding.etMeasureUnit.setFocusableInTouchMode(true);
        mBinding.etMeasureUnit.setInputType(InputType.TYPE_CLASS_TEXT);
        mBinding.tillMeasureUnit.setEndIconMode(END_ICON_NONE);
    }

    @Override
    public void showProgress() {
        //not in use
    }

    @Override
    public void hideProgress() {
        // not in use
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}
