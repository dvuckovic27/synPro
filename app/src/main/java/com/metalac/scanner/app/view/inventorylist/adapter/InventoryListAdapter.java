package com.metalac.scanner.app.view.inventorylist.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.metalac.scanner.app.databinding.RvInventoryListItemBinding;
import com.metalac.scanner.app.view.BaseAdapter;
import com.metalac.scanner.app.view.BaseViewHolder;
import com.metalac.scanner.app.view.inventorylist.interfaces.IOnInventoryListClickCallback;
import com.metalac.scanner.app.view.inventorylist.viewholder.InventoryListItemViewHolder;

public class InventoryListAdapter extends BaseAdapter {
    private final IOnInventoryListClickCallback onInventoryListClickCallback;

    public InventoryListAdapter(IOnInventoryListClickCallback onInventoryListClickCallback) {
        this.onInventoryListClickCallback = onInventoryListClickCallback;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new InventoryListItemViewHolder(RvInventoryListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false), parent, onInventoryListClickCallback);
    }
}
