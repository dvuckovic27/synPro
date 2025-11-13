package com.metalac.scanner.app.view.inventory.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;

import com.metalac.scanner.app.databinding.ProductItemViewHolderBinding;
import com.metalac.scanner.app.models.ProductPreviewItem;
import com.metalac.scanner.app.view.inventory.interfaces.InventoryItemClick;
import com.metalac.scanner.app.view.inventory.viewholder.ProductItemViewHolder;

public class SearchedInventoryDataAdapter extends PagingDataAdapter<ProductPreviewItem, ProductItemViewHolder> {

    private final InventoryItemClick mCallback;

    public SearchedInventoryDataAdapter(InventoryItemClick callback) {
        super(DIFF_CALLBACK);
        this.mCallback = callback;
    }

    static final DiffUtil.ItemCallback<ProductPreviewItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<>() {

                @Override
                public boolean areItemsTheSame(@NonNull ProductPreviewItem oldItem, @NonNull ProductPreviewItem newItem) {
                    return oldItem.getInventoryId() == newItem.getInventoryId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull ProductPreviewItem oldItem, @NonNull ProductPreviewItem newItem) {
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
        ProductPreviewItem productPreviewItem = getItem(position);
        if (productPreviewItem != null) {
            holder.bind(productPreviewItem);
        }
    }
}
