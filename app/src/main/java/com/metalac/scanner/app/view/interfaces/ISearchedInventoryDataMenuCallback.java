package com.metalac.scanner.app.view.interfaces;

import com.metalac.scanner.app.view.inventory.fragments.InventoryFragment;
import com.metalac.scanner.app.view.inventory.fragments.SearchedInventoryDataFragment;

/**
 * Callback for handling menu actions from {@link SearchedInventoryDataFragment}.
 * <p>
 * Invoked when the user chooses to navigate back to {@link InventoryFragment}.
 */
public interface ISearchedInventoryDataMenuCallback {
    void onNavigateToInventory();
}

