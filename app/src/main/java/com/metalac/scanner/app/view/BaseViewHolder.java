package com.metalac.scanner.app.view;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

/**
 * BaseViewHolder is an abstract base class for custom RecyclerView ViewHolders.
 * It provides a standard structure for binding data and handling view recycling.
 */
public abstract class BaseViewHolder extends RecyclerView.ViewHolder {
    /**
     * Context associated with the parent view group.
     * Useful for accessing resources and performing context-related operations.
     */
    @NonNull
    protected Context context;

    /**
     * Constructs a new {@code BaseViewHolder} with the given binding and parent view group.
     *
     * @param binding The ViewBinding associated with this ViewHolder.
     * @param parent  The parent ViewGroup into which this ViewHolder's view will be inserted.
     */
    public BaseViewHolder(ViewBinding binding, @NonNull ViewGroup parent) {
        super(binding.getRoot());
        context = parent.getContext();
    }

    /**
     * Binds data to this ViewHolder.
     * This method should be overridden by subclasses to update the ViewHolder's views
     * based on the provided data object.
     *
     * @param data The data object to bind to the ViewHolder.
     */
    public void bind(@NonNull Object data) {
    }

    /**
     * Called when this ViewHolder is being recycled.
     * Subclasses can override this method to clean up resources or reset view state.
     */
    public void onViewRecycled() {
    }
}
