package com.metalac.scanner.app.view.inventory.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.metalac.scanner.app.databinding.ProductItemViewHolderBinding;
import com.metalac.scanner.app.view.BaseAdapter;
import com.metalac.scanner.app.view.BaseViewHolder;
import com.metalac.scanner.app.view.inventory.interfaces.InventoryItemClick;
import com.metalac.scanner.app.view.inventory.viewholder.ProductItemViewHolder;

public class InventoryItemAdapter extends BaseAdapter {
    /**
     * The maximum number of items that can be displayed in the list at once.
     */
    private static final int MAX_DISPLAYED_ITEM_COUNT = 5;
    private InventoryItemClick mCallback;

    public InventoryItemAdapter(@NonNull InventoryItemClick callback) {
        this.mCallback = callback;
    }

    /**
     * Creates and returns a new BaseViewHolder for the RecyclerView.
     * This method inflates the layout using ProductItemViewHolderBinding and wraps it
     * in a ScannedItemViewHolder instance.
     *
     * @param parent   The parent ViewGroup into which the new view will be added.
     * @param viewType The view type of the new view (not used in this implementation).
     * @return A new instance of BaseViewHolder.
     */
    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ProductItemViewHolder(ProductItemViewHolderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false), parent, mCallback);
    }

    /**
     * Adds a new Object to the list with a limit on the maximum displayed items.
     * If the list has reached the MAX_DISPLAYED_ITEM_COUNT, the last item
     * (at index MAX_DISPLAYED_ITEM_COUNT - 1) is removed to make space
     * for the new inventoryItem.
     *
     * @param inventoryItem The Object to be added to the list.
     */
    public void addNewItem(Object inventoryItem) {
        if (getDataList().size() == MAX_DISPLAYED_ITEM_COUNT) {
            remove(MAX_DISPLAYED_ITEM_COUNT - 1);
        }
        add(inventoryItem);
    }
}
