package com.metalac.scanner.app.view.inventory.presenters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.common.util.CollectionUtils;
import com.metalac.scanner.app.data.source.interfaces.InventoryItemDataSource;
import com.metalac.scanner.app.data.source.interfaces.InventoryListDataSource;
import com.metalac.scanner.app.data.source.interfaces.MasterItemDataSource;
import com.metalac.scanner.app.data.source.repositories.InventoryItemRepository;
import com.metalac.scanner.app.data.source.repositories.InventoryListRepository;
import com.metalac.scanner.app.models.InventoryItem;
import com.metalac.scanner.app.models.InventoryList;
import com.metalac.scanner.app.models.MasterItem;
import com.metalac.scanner.app.data.source.repositories.MasterItemRepository;
import com.metalac.scanner.app.models.ProductPreviewItem;
import com.metalac.scanner.app.models.WeightBarcode;
import com.metalac.scanner.app.view.BaseView;
import com.metalac.scanner.app.view.ScannerReaderError;
import com.metalac.scanner.app.view.inventory.contracts.InventoryContract;

import java.util.List;

public class InventoryPresenter implements InventoryContract.Presenter, MasterItemDataSource.ILoadMasterItemCallback {
    private InventoryList mInventoryList;
    @Nullable
    private InventoryContract.View mView;
    @Nullable
    private final MasterItemRepository mMasterItemRepository;
    @Nullable
    private final InventoryItemRepository mInventoryItemRepository;
    @Nullable
    private final InventoryListRepository mInventoryListRepository;
    private MasterItem mMasterItem;
    private String mItemIdent = "";

    public InventoryPresenter(@Nullable MasterItemRepository masterItemRepository,
                              @Nullable InventoryItemRepository inventoryItemRepository,
                              @Nullable InventoryListRepository inventoryListRepository) {
        this.mMasterItemRepository = masterItemRepository;
        this.mInventoryItemRepository = inventoryItemRepository;
        this.mInventoryListRepository = inventoryListRepository;
    }

    /**
     * Loads a {@link MasterItem} by its barcode asynchronously.
     * Shows progress on the view while loading, then delivers the result or error via callbacks.
     *
     * @param barcode the barcode string to find the master item by; must not be null
     */
    @Override
    public void loadMasterItem(@NonNull String barcode) {
        if (mMasterItemRepository == null) {
            return; // Repository is not initialized, cannot load item
        }

        if (mView != null) {
            mView.showProgress();
        }
        mMasterItemRepository.loadItemByBarcode(barcode, this);
    }

    /**
     * Handles a scanned weight barcode by parsing it and attempting to load the corresponding {@link MasterItem}.
     * <p>
     * The method follows these steps:
     * <ol>
     *     <li>Validates that the repository and view are initialized.</li>
     *     <li>Shows a loading progress indicator on the view.</li>
     *     <li>Parses the barcode using {@link WeightBarcode}, extracting the alternative code and weight in kilograms.</li>
     *     <li>Attempts to load the {@link MasterItem} by the extracted alternative code via the repository.</li>
     *     <li>If the item is found:
     *         <ul>
     *             <li>Updates the presenter’s internal state with the loaded item and its identifier.</li>
     *             <li>Resets the alternative search in the view.</li>
     *             <li>Notifies the view through {@code onMasterItemLoadedByWeightBarcode}, passing the item and weight.</li>
     *         </ul>
     *     </li>
     *     <li>If the item is not found or a parsing/loading error occurs, displays an error dialog in the view.</li>
     * </ol>
     *
     * @param barcode The scanned EAN-13 weight barcode to be processed.
     */
    @Override
    public void handleWeightBarcode(@NonNull String barcode) {
        if (mMasterItemRepository == null || mView == null) {
            return;
        }

        mView.showProgress();

        WeightBarcode weightBarcode;
        try {
            weightBarcode = new WeightBarcode(barcode);
        } catch (ScannerReaderError scannerReaderError) {
            mView.showErrorDialog(scannerReaderError);
            return;
        }

        mMasterItemRepository.loadItemByAltCode1(weightBarcode.getAltCode(), new MasterItemDataSource.ILoadMasterItemCallback() {
            @Override
            public void onMasterItemLoaded(@NonNull MasterItem masterItem) {
                boolean isNewItem = !masterItem.equals(mMasterItem);

                if (isNewItem) {
                    mMasterItem = masterItem;
                    mItemIdent = masterItem.getIdent();
                }

                if (mView != null) {
                    mView.hideProgress();
                    mView.resetAlternativeSearch(true);
                    mView.onMasterItemLoadedByWeightBarcode(masterItem, weightBarcode.getWeightKg());
                }
            }

            @Override
            public void onMasterItemLoadFailed(@NonNull ScannerReaderError scannerReaderError) {
                InventoryPresenter.this.onMasterItemLoadFailed(scannerReaderError);
            }
        });
    }

    /**
     * Checks if the given quantity is valid and takes the appropriate action.
     * - Does nothing if the view is not available.
     * - Shows a "Try Again" dialog if the item is missing.
     * - Shows a warning if the quantity is zero or less.
     * - Shows a warning if the quantity is too high.
     * - Adds the item if the quantity is valid.
     *
     * @param quantity the quantity to check
     */
    @Override
    public void validateQuantity(double quantity) {
        if (mView == null) {
            return;
        }
        if (mMasterItem == null) {
            mView.showTryAgainDialog();
        } else if (quantity <= 0) {
            mView.showDialogIfUomIsZeroOrLess();
        } else if (quantity > mMasterItem.getMaxCountQty()) {
            mView.showQuantityWarningDialog(mMasterItem, quantity);
        } else {
            addInventoryItem(new InventoryItem(mMasterItem.getIdent(), quantity));
        }
    }

    @Override
    public void getInventoryItemList() {
        if (mInventoryItemRepository == null) {
            return;
        }
        mInventoryItemRepository.getInventoryItemList(mInventoryList.getId(), new InventoryItemDataSource.ILoadInventoryItemsCallback() {
            @Override
            public void onSuccess(@NonNull List<ProductPreviewItem> productPreviewItems) {
                if (mView != null && !CollectionUtils.isEmpty(productPreviewItems)) {
                    mView.populateInventoryAdapter(productPreviewItems);
                }
            }

            @Override
            public void onFailure(@NonNull ScannerReaderError scannerReaderError) {
                //No action needed (inventory list is empty)
            }
        });
    }

    @Override
    public void addInventoryItem(@NonNull InventoryItem inventoryItem) {
        if (mInventoryItemRepository == null) {
            return;
        }
        inventoryItem.setInventoryListId(mInventoryList.getId());
        mInventoryItemRepository.addInventoryItem(inventoryItem, new InventoryItemDataSource.IAddInventoryItemCallback() {
            @Override
            public void onSuccess(@NonNull ProductPreviewItem productPreviewItem) {
                if (mView != null) {
                    mView.addItem(productPreviewItem);
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
    public void loadAdditionData(long inventoryItemId) {
        if (mInventoryItemRepository == null) {
            return;
        }

        mInventoryItemRepository.getInventoryItemById(inventoryItemId, new InventoryItemDataSource.ILoadInventoryItemCallback() {
            @Override
            public void onInventoryItemLoaded(@NonNull InventoryItem inventoryItem) {
                if (mView != null && inventoryItem.hasAdditionalData()) {
                    mView.showAdditionalData(inventoryItem);
                }
            }

            @Override
            public void onInventoryItemLoadFailed(@NonNull ScannerReaderError scannerReaderError) {
                if (mView != null) {
                    mView.showErrorDialog(scannerReaderError);
                }
            }
        });
    }

    @Override
    public void setItemId(String itemIdent) {
        mItemIdent = itemIdent;
    }

    @Override
    public String getItemIdent() {
        return mItemIdent;
    }

    @Override
    public void loadCurrentList() {
        if (mInventoryListRepository != null) {
            mInventoryListRepository.getCurrentList(new InventoryListDataSource.ILoadInventoryListCallback() {
                @Override
                public void onInventoryListLoaded(@NonNull InventoryList inventoryList) {
                    mInventoryList = inventoryList;
                    if (mView != null) {
                        mView.displayCurrentListData(inventoryList.getName());
                    }
                    getInventoryItemList();
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

    /**
     * Loads a {@link MasterItem} by its ident from the repository.
     * <p>
     * If the item is successfully loaded:
     * <ul>
     *     <li>Updates the presenter’s internal state ({@code mMasterItem} and {@code mItemIdent}) if it is a new item.</li>
     *     <li>Hides the loading progress indicator in the view.</li>
     *     <li>Resets the alternative search state in the view.</li>
     *     <li>Notifies the view through {@code onMasterItemLoaded}, passing the item and whether it is new.</li>
     * </ul>
     * If loading fails, the error is handled via {@link #onMasterItemLoadFailed(ScannerReaderError)}.
     *
     * @param ident The ident of the item to load; must not be null.
     */
    @Override
    public void getItemByIdent(@NonNull String ident) {
        if (mMasterItemRepository != null) {
            mMasterItemRepository.getItemByIdent(ident, new MasterItemDataSource.ILoadMasterItemCallback() {
                @Override
                public void onMasterItemLoaded(@NonNull MasterItem masterItem) {
                    boolean isNewItem = !masterItem.equals(mMasterItem);

                    if (isNewItem) {
                        mMasterItem = masterItem;
                        mItemIdent = masterItem.getIdent();
                    }

                    if (mView != null) {
                        mView.hideProgress();
                        mView.resetAlternativeSearch(false);
                        mView.onMasterItemLoaded(masterItem, isNewItem);
                    }
                }

                @Override
                public void onMasterItemLoadFailed(@NonNull ScannerReaderError scannerReaderError) {
                    InventoryPresenter.this.onMasterItemLoadFailed(scannerReaderError);
                }
            });
        }
    }

    /**
     * Loads a {@link MasterItem} by its alternative identifier (altId) from the repository.
     * <p>
     * If the item is successfully loaded:
     * <ul>
     *     <li>Updates the presenter’s internal state ({@code mMasterItem} and {@code mItemIdent}) if it is a new item.</li>
     *     <li>Hides the loading progress indicator in the view.</li>
     *     <li>Resets the alternative search state in the view.</li>
     *     <li>Notifies the view through {@code onMasterItemLoaded}, passing the item and whether it is new.</li>
     * </ul>
     * If loading fails, the error is handled via {@link #onMasterItemLoadFailed(ScannerReaderError)}.
     *
     * @param altId The alternative identifier of the item to load; must not be null.
     */

    @Override
    public void getItemByAltId(@NonNull String altId) {
        if (mMasterItemRepository != null) {
            mMasterItemRepository.getItemByAltId(altId, new MasterItemDataSource.ILoadMasterItemCallback() {
                @Override
                public void onMasterItemLoaded(@NonNull MasterItem masterItem) {
                    boolean isNewItem = !masterItem.equals(mMasterItem);

                    if (isNewItem) {
                        mMasterItem = masterItem;
                        mItemIdent = masterItem.getIdent();
                    }

                    if (mView != null) {
                        mView.hideProgress();
                        mView.resetAlternativeSearch(false);
                        mView.onMasterItemLoaded(masterItem, isNewItem);
                    }
                }

                @Override
                public void onMasterItemLoadFailed(@NonNull ScannerReaderError scannerReaderError) {
                    InventoryPresenter.this.onMasterItemLoadFailed(scannerReaderError);
                }
            });
        }
    }

    /**
     * Loads a {@link MasterItem} asynchronously by the current {@code mItemIdent}.
     * <p>
     * Preconditions:
     * <ul>
     *     <li>The repository {@code mMasterItemRepository} must be initialized.</li>
     *     <li>{@code mItemIdent} must not be empty.</li>
     * </ul>
     * <p>
     * While loading, a progress indicator is shown in the view. The result is delivered via
     * the presenter’s callback methods:
     * <ul>
     *     <li>On success: the loaded item is passed to the appropriate callback for further handling.</li>
     *     <li>On failure: the error is handled via {@link #onMasterItemLoadFailed(ScannerReaderError)}.</li>
     * </ul>
     */
    @Override
    public void getSearchedData() {
        if (mMasterItemRepository == null || mItemIdent.isEmpty()) {
            return; // Repository is not initialized or there is no itemIdent, cannot load item
        }

        if (mView != null) {
            mView.showProgress();
        }

        mMasterItemRepository.getItemByIdent(mItemIdent, this);
    }

    @Override
    public void onAttach(BaseView view) {
        this.mView = (InventoryContract.View) view;
    }

    @Override
    public void onDetach() {
        this.mView = null;
    }

    @Override
    public void onMasterItemLoaded(@NonNull MasterItem masterItem) {
        boolean isNewItem = !masterItem.equals(mMasterItem);

        if (isNewItem) {
            mMasterItem = masterItem;
            mItemIdent = masterItem.getIdent();
        }

        if (mView != null) {
            mView.hideProgress();
            mView.resetAlternativeSearch(true);
            mView.onMasterItemLoaded(masterItem, isNewItem);
        }
    }

    @Override
    public void onMasterItemLoadFailed(@NonNull ScannerReaderError scannerReaderError) {
        if (mView != null) {
            mView.hideProgress();
            mView.onMasterItemLoadingFailed(scannerReaderError);
        }
    }
}
