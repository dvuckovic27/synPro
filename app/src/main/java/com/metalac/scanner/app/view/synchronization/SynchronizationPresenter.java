package com.metalac.scanner.app.view.synchronization;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.metalac.scanner.app.helpers.DateHelper;
import com.metalac.scanner.app.helpers.DialogHelper;
import com.metalac.scanner.app.data.source.PrefManager;
import com.metalac.scanner.app.data.source.interfaces.InventoryItemDataSource;
import com.metalac.scanner.app.data.source.interfaces.InventoryListDataSource;
import com.metalac.scanner.app.data.source.interfaces.MasterItemDataSource;
import com.metalac.scanner.app.data.source.repositories.InventoryItemRepository;
import com.metalac.scanner.app.data.source.repositories.InventoryListRepository;
import com.metalac.scanner.app.data.source.repositories.MasterItemRepository;
import com.metalac.scanner.app.view.BaseView;
import com.metalac.scanner.app.view.ScannerReaderError;

import java.util.Date;

public class SynchronizationPresenter implements SynchronizationContract.Presenter {
    @Nullable
    private SynchronizationContract.View mView;
    @Nullable
    private final MasterItemRepository mMasterItemRepository;
    @Nullable
    private final InventoryItemRepository mInventoryItemRepository;
    @Nullable
    private final InventoryListRepository mInventoryListRepository;

    public SynchronizationPresenter(@Nullable MasterItemRepository masterItemRepository,
                                    @Nullable InventoryItemRepository inventoryItemRepository,
                                    @Nullable InventoryListRepository inventoryListRepository) {
        this.mMasterItemRepository = masterItemRepository;
        this.mInventoryItemRepository = inventoryItemRepository;
        this.mInventoryListRepository = inventoryListRepository;
    }

    /**
     * Starts loading and synchronizing master item data from the given JSON file URI.
     * <p>
     * The method performs the following steps:
     * <ul>
     *     <li>Validates that the URI is not null. If null, notifies the view of a failed sync.</li>
     *     <li>Shows a progress dialog while the data is being loaded.</li>
     *     <li>Handles the repository callback:
     *         <ul>
     *             <li>On success: hides the progress dialog and notifies the view of a successful sync with the formatted date.</li>
     *             <li>On failure: hides the progress dialog and notifies the view of a failed sync with the error.</li>
     *         </ul>
     *     </li>
     * </ul>
     *  @param uri              The URI of the selected JSON file to load. Must not be null.
     */

    @Override
    public void loadMasterItems(@Nullable Uri uri) {
        if (uri == null) {
            if (mView != null) {
                mView.onFailedSync(new ScannerReaderError(""));
            }
            return;
        }

        if (mView != null) {
            mView.createProgressDialog(DialogHelper.DialogMode.SYNC);
        }

        if (mMasterItemRepository != null) {
            mMasterItemRepository.loadAndSyncFromFile(uri, new MasterItemDataSource.ISyncMasterItemsCallback() {
                @Override
                public void onSuccess(String formattedSyncDate) {
                    if (mView != null) {
                        mView.hideProgress();
                        mView.onSuccessfulSync(formattedSyncDate);
                    }
                }

                @Override
                public void onFailure(ScannerReaderError error) {
                    if (mView != null) {
                        mView.hideProgress();
                        mView.onFailedSync(error);
                    }
                }
            });
        }
    }

    @Override
    public void exportData() {
        if (mView != null) {
            mView.createProgressDialog(DialogHelper.DialogMode.EXPORT);
        }

        if (mInventoryItemRepository != null) {
            mInventoryItemRepository.exportData(new InventoryItemDataSource.IOnDataLoadedCallback() {
                @Override
                public void onItemsLoaded() {
                    if (mView != null) {
                        mView.hideProgress();
                        mView.createSuccessfulDialog(DialogHelper.DialogMode.EXPORT);
                        String lastExportDate = DateHelper.formatDateToString(new Date());
                        PrefManager.setLastDataExportDate(lastExportDate);
                        mView.displayLastExportDate(lastExportDate);
                    }
                }

                @Override
                public void onFailToLoadItems(@NonNull ScannerReaderError scannerReaderError) {
                    if (mView != null) {
                        mView.hideProgress();
                        mView.showErrorDialog(scannerReaderError);
                    }
                }
            });
        }
    }

    @Override
    public void deleteInventoryData() {
        if (mView != null) {
            mView.createProgressDialog(DialogHelper.DialogMode.DELETE);
        }
        if (mInventoryItemRepository != null) {
            mInventoryItemRepository.deleteInventoryData(new InventoryItemDataSource.IOnInventoryDataDeletedCallback() {
                @Override
                public void onInventoryDataDeleted() {
                    deleteInventoryListData();
                }

                @Override
                public void onDeleteInventoryDataFailed(@NonNull ScannerReaderError scannerReaderError) {
                    if (mView != null) {
                        mView.hideProgress();
                        mView.showErrorDialog(scannerReaderError);
                    }
                }
            });
        }
    }

    private void deleteInventoryListData() {
        if (mInventoryListRepository != null) {
            mInventoryListRepository.deleteInventoryListData(new InventoryListDataSource.IOnInventoryListDataDeletedCallback() {
                @Override
                public void onInventoryListDataDeleted() {
                    if (mView != null) {
                        mView.hideProgress();
                        mView.enableDelete(false);
                        mView.enableExport(false);
                        mView.createSuccessfulDialog(DialogHelper.DialogMode.DELETE);
                    }
                }

                @Override
                public void onDeleteInventoryListDataFailed(@NonNull ScannerReaderError scannerReaderError) {
                    if (mView != null) {
                        mView.hideProgress();
                        mView.showErrorDialog(scannerReaderError);
                    }
                }
            });
        }
    }

    @Override
    public void checkInventoryData() {
        if (mInventoryItemRepository != null) {
            mInventoryItemRepository.checkIfAnyInventoryItemExists(new InventoryItemDataSource.ICheckInventoryItemExistsCallback() {
                @Override
                public void onResult(boolean exists) {
                    if (mView != null) {
                        mView.enableExport(exists);
                    }
                }

                @Override
                public void onError(@NonNull ScannerReaderError scannerReaderError) {
                    if (mView != null) {
                        mView.showErrorDialog(scannerReaderError);
                    }
                }
            });
        }
    }

    @Override
    public void checkInventoryListData() {
        if (mInventoryListRepository != null) {
            mInventoryListRepository.checkIfAnyInventoryListExists(new InventoryListDataSource.ICheckInventoryListExistsCallback() {
                @Override
                public void onResult(boolean exists) {
                    if (mView != null) {
                        mView.enableDelete(exists);
                    }
                }

                @Override
                public void onError(@NonNull ScannerReaderError scannerReaderError) {
                    if (mView != null) {
                        mView.showErrorDialog(scannerReaderError);
                    }
                }
            });
        }
    }

    @Override
    public void onAttach(BaseView view) {
        this.mView = (SynchronizationContract.View) view;
    }

    @Override
    public void onDetach() {
        this.mView = null;
    }
}
