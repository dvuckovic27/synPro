package com.metalac.scanner.app.view.inventory.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;

import com.metalac.scanner.app.models.MasterItem;
import com.metalac.scanner.app.databinding.ProductItemViewHolderBinding;
import com.metalac.scanner.app.view.inventory.interfaces.InventoryItemClick;
import com.metalac.scanner.app.view.inventory.viewholder.ProductItemViewHolder;

import java.util.Objects;

public class SearchedDataAdapter extends PagingDataAdapter<MasterItem, ProductItemViewHolder> {

    private final InventoryItemClick mCallback;

    public SearchedDataAdapter(InventoryItemClick callback) {
        super(DIFF_CALLBACK);
        this.mCallback = callback;
    }

    /**
     * A {@link DiffUtil.ItemCallback} implementation used to efficiently determine
     * whether two {@link MasterItem} objects are the same or have the same content.
     * This callback is used by PagingDataAdapter to optimize updates and minimize unnecessary rebinds.
     */
    static final DiffUtil.ItemCallback<MasterItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<>() {
                /**
                 * Checks if two {@link MasterItem} objects are the same item.
                 *
                 * @param oldItem  The old item.
                 * @param newItem  The new item.
                 * @return true if both items are the same (i.e., they have the same ID).
                 */
                @Override
                public boolean areItemsTheSame(@NonNull MasterItem oldItem, @NonNull MasterItem newItem) {
                    return Objects.equals(oldItem.getIdent(), newItem.getIdent());
                }

                /**
                 * Checks if the contents of two {@link MasterItem} objects are the same.
                 *
                 * @param oldItem  The old item.
                 * @param newItem  The new item.
                 * @return true if both items have the same content (i.e., their properties are equal).
                 */
                @Override
                public boolean areContentsTheSame(@NonNull MasterItem oldItem, @NonNull MasterItem newItem) {
                    return oldItem.equals(newItem);
                }
            };

    @NonNull
    @Override
    public ProductItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ProductItemViewHolder(ProductItemViewHolderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false), parent, mCallback);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductItemViewHolder holder, int position) {
        MasterItem masterItem = getItem(position);
        if (masterItem != null) {
            holder.bind(masterItem);
        }
    }
}
