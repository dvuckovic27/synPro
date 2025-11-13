package com.metalac.scanner.app.view.inventory.viewholder;

import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.metalac.scanner.app.R;
import com.metalac.scanner.app.databinding.ProductItemViewHolderBinding;
import com.metalac.scanner.app.models.MasterItem;
import com.metalac.scanner.app.models.ProductPreviewItem;
import com.metalac.scanner.app.view.BaseViewHolder;
import com.metalac.scanner.app.view.inventory.interfaces.InventoryItemClick;

public class ProductItemViewHolder extends BaseViewHolder {

    /**
     * View binding instance for accessing item layout views.
     */
    private final ProductItemViewHolderBinding mBinding;
    private final InventoryItemClick mCallback;

    /**
     * Constructs a new ProductItemViewHolder.
     *
     * @param binding The generated view binding for this item layout.
     * @param parent  The parent ViewGroup containing this item.
     */
    public ProductItemViewHolder(ProductItemViewHolderBinding binding, @NonNull ViewGroup parent, @Nullable InventoryItemClick callback) {
        super(binding, parent);
        mBinding = binding;
        mCallback = callback;
    }

    /**
     * Binds a InventoryItem object to the item views.
     * Updates the UI elements such as inventoryItem name, price, measure unit, and quantity.
     *
     * @param data The InventoryItem instance to display.
     * @throws ClassCastException if data is not a InventoryItem.
     */
    @Override
    public void bind(@NonNull Object data) {
        super.bind(data);
        ProductPreviewItem productPreviewItem;

        if (data instanceof MasterItem) {
            productPreviewItem = new ProductPreviewItem((MasterItem) data);
        } else {
            productPreviewItem = (ProductPreviewItem) data;
            switch (productPreviewItem.getStatus()) {
                case VOID:
                    updateUIBasedOnStatus(false, R.color.inventory_void_item_bg, 0, R.drawable.badge_item_void);
                    break;
                case VOIDED:
                    updateUIBasedOnStatus(false, R.color.inventory_voided_item_bg, Paint.STRIKE_THRU_TEXT_FLAG, R.drawable.badge_item_voided);
                    break;
                case NON_VOIDED:
                    Drawable drawable = productPreviewItem.hasExtraInfo()
                            ? ContextCompat.getDrawable(context, R.drawable.ic_info)
                            : null;

                    mBinding.tvProductName.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);

                    updateUIBasedOnStatus(true, R.color.inventory_item_bg, 0, R.drawable.badge_item_non_voided);
                    break;
            }
        }
        if (mCallback != null) {
            mBinding.getRoot().setOnClickListener(v -> mCallback.onItemClicked(productPreviewItem));
        }

        int indexInInventoryList = productPreviewItem.getIndexInInventoryList();
        if (indexInInventoryList > 0) {
            mBinding.tvIndex.setText(String.valueOf(indexInInventoryList));
        } else {
            mBinding.tvIndex.setVisibility(View.GONE);
        }

        mBinding.tvProductName.setText(productPreviewItem.getProductName());
        mBinding.tvProductPrice.setText(String.valueOf(productPreviewItem.getProductPrice()));
        mBinding.tvProductQuantity.setText(String.valueOf(productPreviewItem.getQuantityString()));
    }

    /**
     * Updates the UI state using the given values.
     *
     * @param isEnabled true to enable the view, false to disable it
     * @param color     background color resource ID
     * @param paintFlag paint flag for the product name text
     * @param drawable  background badge drawable resource ID
     */
    private void updateUIBasedOnStatus(boolean isEnabled, int color, int paintFlag, int drawable) {
        mBinding.getRoot().setEnabled(isEnabled);
        mBinding.clContainer.setBackgroundColor(
                ContextCompat.getColor(mBinding.clContainer.getContext(), color));
        mBinding.tvProductName.setPaintFlags(paintFlag);
        mBinding.tvIndex.setBackground(
                ContextCompat.getDrawable(context, drawable));
    }
}
