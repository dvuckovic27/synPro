package com.metalac.scanner.app.view.inventory.presenters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.metalac.scanner.app.data.source.interfaces.InventoryItemDataSource;
import com.metalac.scanner.app.data.source.interfaces.InventoryListDataSource;
import com.metalac.scanner.app.data.source.interfaces.MasterItemDataSource;
import com.metalac.scanner.app.data.source.repositories.InventoryItemRepository;
import com.metalac.scanner.app.data.source.repositories.InventoryListRepository;
import com.metalac.scanner.app.data.source.repositories.MasterItemRepository;
import com.metalac.scanner.app.models.DamageInfo;
import com.metalac.scanner.app.models.InventoryItem;
import com.metalac.scanner.app.models.InventoryList;
import com.metalac.scanner.app.models.MasterItem;
import com.metalac.scanner.app.models.ProductPreviewItem;
import com.metalac.scanner.app.view.BaseView;
import com.metalac.scanner.app.view.ScannerReaderError;
import com.metalac.scanner.app.view.inventory.contracts.ExtraInfoContract;
import com.metalac.scanner.app.view.inventory.interfaces.ILoadDamageDescriptionCallback;
import com.metalac.scanner.app.view.inventory.interfaces.ILoadDamageInfoCallback;
import com.metalac.scanner.app.view.inventory.interfaces.ISetResultArgsCallback;

import java.util.ArrayList;

public class ExtraInfoPresenter implements ExtraInfoContract.Presenter {

    private InventoryList mInventoryList;
    @Nullable
    private ExtraInfoContract.View mView;
    @Nullable
    private final InventoryItemRepository mInventoryItemRepository;
    @Nullable
    private final MasterItemRepository mMasterItemRepository;
    @Nullable
    private final InventoryListRepository mInventoryListRepository;
    private MasterItem mMasterItem;
    private String mMasterIdent;
    private long mInventoryItemId;
    private boolean mExtraInfoItemAdded;

    public ExtraInfoPresenter(@Nullable MasterItemRepository masterItemRepository,
                              @Nullable InventoryItemRepository inventoryItemRepository,
                              @Nullable InventoryListRepository inventoryListRepository) {
        this.mMasterItemRepository = masterItemRepository;
        this.mInventoryItemRepository = inventoryItemRepository;
        this.mInventoryListRepository = inventoryListRepository;
    }

    /**
     * Loads all necessary data related to the master item:
     * - The master item itself
     * - The list of available damage codes
     * - The corresponding inventory item (including damage description if applicable)
     *
     * @param inventoryItemId The sequence number associated with the inventory item.
     * @param ident           The unique identifier for the master item.
     */
    @Override
    public void loadAllMasterData(long inventoryItemId, String ident) {
        mInventoryItemId = inventoryItemId;
        mMasterIdent = ident;

        loadMasterItem(ident);
        loadDamageInfo();
        loadInventoryItem(inventoryItemId);
    }

    /**
     * Loads the master item with the given ID from the repository.
     * On success, stores the loaded item internally.
     * On failure, notifies the view with an error dialog.
     *
     * @param ident The unique identifier of the master item to load.
     */
    private void loadMasterItem(String ident) {
        if (mMasterItemRepository == null) {
            return;
        }

        mMasterItemRepository.getItemByIdent(ident, new MasterItemDataSource.ILoadMasterItemCallback() {
            @Override
            public void onMasterItemLoaded(@NonNull MasterItem masterItem) {
                mMasterItem = masterItem;
                if (mView != null) {
                    mView.configureQuantityInput(masterItem.getDecimalPlaces());
                }
            }

            @Override
            public void onMasterItemLoadFailed(@NonNull ScannerReaderError scannerReaderError) {
                if (mView != null) {
                    mView.showErrorDialog(scannerReaderError);
                }
            }
        });
    }

    /**
     * Loads the list of available damage codes and descriptions.
     * Passes the data to the view on success.
     * If the list fails to load, triggers a fallback UI message on the view.
     */
    private void loadDamageInfo() {
        if (mMasterItemRepository == null) return;

        mMasterItemRepository.getDamageInfoList(new ILoadDamageInfoCallback() {
            @Override
            public void onDamageInfoLoaded(@NonNull ArrayList<DamageInfo> damageInfoList) {
                if (mView != null) {
                    mView.populateDamageInfo(damageInfoList);
                }
            }

            @Override
            public void onDamageInfoLoadFailed(@NonNull ScannerReaderError scannerReaderError) {
                if (mView != null) {
                    mView.showNoDamageInfo();
                }
            }
        });
    }

    /**
     * Loads the inventory item associated with the given sequence number.
     * If a damage code exists, triggers loading of the corresponding damage description.
     * Otherwise, immediately displays the inventory item data.
     *
     * @param inventoryItemId The sequence number of the inventory item to load.
     */
    private void loadInventoryItem(long inventoryItemId) {
        if (mInventoryItemRepository == null) return;

        mInventoryItemRepository.getInventoryItemById(inventoryItemId, new InventoryItemDataSource.ILoadInventoryItemCallback() {
            @Override
            public void onInventoryItemLoaded(@NonNull InventoryItem inventoryItem) {
                if (mView == null) return;

                String damageCode = inventoryItem.getDamageCode();
                if (damageCode != null && !damageCode.isEmpty()) {
                    resolveDamageDescription(inventoryItem, damageCode);
                } else {
                    mView.showInitialData(inventoryItem);
                }
            }

            @Override
            public void onInventoryItemLoadFailed(@NonNull ScannerReaderError scannerReaderError) {
                // No action needed
            }
        });
    }

    /**
     * Resolves the description for a given damage code by fetching it from the repository.
     * Once retrieved, updates the inventory item and passes it to the view.
     * If resolving fails, still displays the item without the damage description.
     *
     * @param inventoryItem The inventory item being updated.
     * @param damageCode    The damage code to resolve into a readable description.
     */
    private void resolveDamageDescription(InventoryItem inventoryItem, String damageCode) {
        if (mMasterItemRepository == null) return;

        mMasterItemRepository.getDamageDescriptionByCode(damageCode, new ILoadDamageDescriptionCallback() {
            @Override
            public void onDamageDescriptionLoaded(@NonNull String description) {
                inventoryItem.setDamageDesc(description);
                if (mView != null) {
                    mView.showInitialData(inventoryItem);
                }
            }

            @Override
            public void onDamageDescriptionLoadFailed(@NonNull ScannerReaderError scannerReaderError) {
                if (mView != null) {
                    mView.showInitialData(inventoryItem);
                }
            }
        });
    }

    /**
     * Validates the given quantity and optional metadata before proceeding to add the inventory item.
     * <p>
     * The method checks several conditions in the following order:
     * <ul>
     *     <li>If the view or repository is null, the method exits early.</li>
     *     <li>If the master item is null, navigates back via the view.</li>
     *     <li>If the quantity is less than or equal to zero, shows a warning dialog.</li>
     *     <li>If the quantity exceeds the maximum allowed for the item, shows a quantity warning dialog.</li>
     *     <li>Otherwise, creates an {@link InventoryItem}, attaches additional data, and adds it to the inventory.</li>
     * </ul>
     *
     * @param quantity   the quantity of the item to validate
     * @param expDate    optional expiration date for the item (nullable)
     * @param damageCode optional damage code if the item is damaged (nullable)
     * @param note       optional user note about the item (nullable)
     */
    @Override
    public void validateQuantity(double quantity, @Nullable String expDate, @Nullable String damageCode, @Nullable String note) {
        if (mView == null || mInventoryItemRepository == null) {
            return;
        }
        if (mMasterItem == null) {
            mView.goBack();
        } else if (quantity <= 0) {
            mView.showDialogIfUomIsZeroOrLess();
        } else if (quantity > mMasterItem.getMaxCountQty()) {
            mView.showQuantityWarningDialog(mMasterItem, quantity);
        } else {
            InventoryItem inventoryItem = new InventoryItem(mMasterItem.getIdent(), quantity);
            inventoryItem.addAdditionallyData(expDate, damageCode, note);
            addItem(inventoryItem);
        }
    }

    @Override
    public void addItem(@NonNull InventoryItem inventoryItem) {
        if (mInventoryItemRepository == null) {
            return;
        }
        inventoryItem.setInventoryListId(mInventoryList.getId());
        mInventoryItemRepository.addInventoryItem(inventoryItem, new InventoryItemDataSource.IAddInventoryItemCallback() {
            @Override
            public void onSuccess(@NonNull ProductPreviewItem productPreviewItem) {
                if (mView != null) {
                    mView.goBack();
                }
            }

            @Override
            public void onFailure(@NonNull ScannerReaderError scannerReaderError) {
                if (mView != null) {
                    mView.showErrorDialog(scannerReaderError);
                }
            }
        });
    }

    @Override
    public void updateItem(@Nullable String expDate, @Nullable String damageCode, @Nullable String note) {
        if (mView == null || mInventoryItemRepository == null) return;

        // No update needed, just navigate back
        if (expDate == null && damageCode == null && note == null) {
            mView.goBack();
            return;
        }

        mInventoryItemRepository.getInventoryItemById(mInventoryItemId, new InventoryItemDataSource.ILoadInventoryItemCallback() {
            @Override
            public void onInventoryItemLoaded(@NonNull InventoryItem inventoryItem) {
                inventoryItem.addAdditionallyData(expDate, damageCode, note);

                mInventoryItemRepository.updateInventoryItem(inventoryItem, new InventoryItemDataSource.IOnInventoryItemUpdatedCallback() {
                    @Override
                    public void onSuccess() {
                        if (mView != null) {
                            mView.goBack();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull ScannerReaderError error) {
                        if (mView != null) {
                            mView.showErrorDialog(error);
                        }
                    }
                });
            }

            @Override
            public void onInventoryItemLoadFailed(@NonNull ScannerReaderError error) {
                if (mView != null) {
                    mView.showErrorDialog(error);
                }
            }
        });
    }

    @Override
    public void setResultArgs(@NonNull ISetResultArgsCallback setResultArgsCallback) {
        setResultArgsCallback.setResultArgs(mMasterIdent, mExtraInfoItemAdded);
    }

    @Override
    public void setExtraInfoItemAdded(boolean extraInfoItemAdded) {
        this.mExtraInfoItemAdded = extraInfoItemAdded;
    }

    @Override
    public void loadCurrentList() {
        if (mInventoryListRepository != null) {
            mInventoryListRepository.getCurrentList(new InventoryListDataSource.ILoadInventoryListCallback() {
                @Override
                public void onInventoryListLoaded(@NonNull InventoryList inventoryList) {
                    mInventoryList = inventoryList;
                }

                @Override
                public void onFailToLoadInventoryList(@NonNull ScannerReaderError scannerReaderError) {
                    if (mView != null) {
                        mView.showErrorDialog(scannerReaderError);
                    }
                }
            });
        }
    }

    @Override
    public void onAttach(BaseView view) {
        this.mView = (ExtraInfoContract.View) view;
    }

    @Override
    public void onDetach() {
        this.mView = null;
    }
}
