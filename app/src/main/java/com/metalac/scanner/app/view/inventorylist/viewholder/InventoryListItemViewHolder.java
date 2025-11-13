package com.metalac.scanner.app.view.inventorylist.viewholder;

import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.metalac.scanner.app.databinding.RvInventoryListItemBinding;
import com.metalac.scanner.app.models.InventoryList;
import com.metalac.scanner.app.models.InventoryListWithCount;
import com.metalac.scanner.app.view.BaseViewHolder;
import com.metalac.scanner.app.view.inventorylist.interfaces.IOnInventoryListClickCallback;

public class InventoryListItemViewHolder extends BaseViewHolder {

    private final RvInventoryListItemBinding mBinding;
    private final IOnInventoryListClickCallback mCallback;

    public InventoryListItemViewHolder(RvInventoryListItemBinding binding, @NonNull ViewGroup parent, IOnInventoryListClickCallback mCallback) {
        super(binding, parent);
        this.mBinding = binding;
        this.mCallback = mCallback;
    }

    @Override
    public void bind(@NonNull Object data) {
        super.bind(data);
        InventoryListWithCount inventoryListWithCount = (InventoryListWithCount) data;
        InventoryList inventoryList = inventoryListWithCount.getInventoryList();

        mBinding.tvListName.setText(inventoryList.getName());
        mBinding.tvCount.setText(String.valueOf(inventoryListWithCount.getCount()));

        mBinding.getRoot().setOnClickListener(v -> mCallback.onClick(inventoryList));
    }
}
