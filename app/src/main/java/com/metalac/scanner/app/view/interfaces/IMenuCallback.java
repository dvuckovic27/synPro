package com.metalac.scanner.app.view.interfaces;

/**
 * Callback interface for menu-related actions.
 * <p>
 * Implement this interface to handle menu events such as changing the store code.
 */
public interface IMenuCallback {
    /**
     * Called when the user initiates a store code change via the menu.
     */
    void onChangeStoreCode();
}
