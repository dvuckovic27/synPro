package com.metalac.scanner.app.view;

import androidx.annotation.NonNull;
import androidx.annotation.Size;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * BaseAdapter is an abstract RecyclerView adapter that provides
 * common functionality for managing a list of generic Object items.
 * Subclasses must implement {RecyclerView.Adapter#onCreateViewHolder(ViewGroup, int)}
 * to provide custom view holder creation logic.
 */
public abstract class BaseAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    /**
     * The list of items managed by this adapter.
     */
    protected List<Object> dataList;

    /**
     * Binds data at the specified position to the provided view holder.
     *
     * @param holder   The BaseViewHolder to bind data to.
     * @param position The position of the item in the list.
     */
    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.bind(dataList.get(position));
    }

    /**
     * Returns the number of items in the data list.
     *
     * @return Item count, or 0 if the data list is null.
     */
    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }

    /**
     * Returns the current data list.
     *
     * @return The current list of data items, never null.
     */
    @NonNull
    public List<Object> getDataList() {
        return dataList;
    }

    /**
     * Sets a new data list and notifies the adapter that the data set has changed.
     *
     * @param dataList A non-null list of data items.
     */
    public void setDataList(@NonNull @Size(min = 0) List<Object> dataList) {
        this.dataList = new ArrayList<>(dataList);
        notifyDataSetChanged();
    }

    /**
     * Called when a view holder is recycled.
     * This method allows the view holder to release or reset its internal state.
     *
     * @param holder The BaseViewHolder being recycled.
     */
    @Override
    public void onViewRecycled(@NonNull BaseViewHolder holder) {
        super.onViewRecycled(holder);
        holder.onViewRecycled();
    }

    /**
     * Removes the item at the specified position from the list and updates the adapter.
     *
     * @param position The index of the item to remove. If -1, nothing happens.
     */
    public void remove(int position) {
        if (position == -1 || dataList == null) return;
        dataList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
    }

    /**
     * Adds a new item to the beginning of the list and notifies the adapter.
     *
     * @param newObject The item to add.
     */
    public void add(Object newObject) {
        add(0, newObject);
    }

    /**
     * Adds a new item to the specified index in the list and notifies the adapter.
     * If the index is invalid or exceeds the list size, the item is inserted at position 0.
     *
     * @param index     The index at which to insert the new item.
     * @param newObject The item to add.
     */
    public void add(int index, Object newObject) {
        if (index == -1) return;
        if (dataList == null) dataList = new ArrayList<>(1);
        if (index > dataList.size()) index = 0;
        dataList.add(index, newObject);
        notifyItemInserted(index);
    }
}