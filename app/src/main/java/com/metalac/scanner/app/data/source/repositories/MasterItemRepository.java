package com.metalac.scanner.app.data.source.repositories;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingLiveData;

import com.metalac.scanner.app.utils.Utils;
import com.metalac.scanner.app.data.source.db.MasterItemLocalDataSource;
import com.metalac.scanner.app.data.source.interfaces.MasterItemDataSource;
import com.metalac.scanner.app.models.MasterItem;
import com.metalac.scanner.app.models.QueryMasterItem;
import com.metalac.scanner.app.view.inventory.interfaces.ILoadDamageDescriptionCallback;
import com.metalac.scanner.app.view.inventory.interfaces.ILoadDamageInfoCallback;

/**
 * Repository for managing access to master item data.
 * Acts as an abstraction layer over the local data source.
 */
public class MasterItemRepository implements MasterItemDataSource {

    private static MasterItemRepository INSTANCE = null;
    private final MasterItemLocalDataSource mMasterItemLocalDataSource;

    /**
     * Returns the singleton instance of the repository.
     *
     * @param masterItemLocalDataSource The local data source
     * @return Singleton instance
     */
    public static MasterItemRepository getInstance(MasterItemLocalDataSource masterItemLocalDataSource) {
        if (INSTANCE == null) {
            INSTANCE = new MasterItemRepository(masterItemLocalDataSource);
        }
        return INSTANCE;
    }

    public MasterItemRepository(MasterItemLocalDataSource masterItemLocalDataSource) {
        this.mMasterItemLocalDataSource = masterItemLocalDataSource;
    }

    @Override
    public void loadAndSyncFromFile(@NonNull Uri uri, @NonNull ISyncMasterItemsCallback callback) {
        mMasterItemLocalDataSource.loadAndSyncFromFile(uri, callback);
    }

    @Override
    public void loadItemByBarcode(String barcode, @NonNull ILoadMasterItemCallback loadMasterItemCallback) {
        mMasterItemLocalDataSource.loadItemByBarcode(barcode, loadMasterItemCallback);
    }

    @Override
    public void loadItemByAltCode1(int altCode1, @NonNull MasterItemDataSource.ILoadMasterItemCallback loadMasterItemCallback) {
        mMasterItemLocalDataSource.loadItemByAltCode1(altCode1, loadMasterItemCallback);
    }

    @Override
    public void getUnitOfMeasureList(ILoadUnitOfMeasureCallback callback) {
        mMasterItemLocalDataSource.getUnitOfMeasureList(callback);
    }

    @Override
    public void getDamageInfoList(ILoadDamageInfoCallback callback) {
        mMasterItemLocalDataSource.getDamageInfoList(callback);
    }

    @Override
    public void getDamageDescriptionByCode(String code, ILoadDamageDescriptionCallback callback) {
        mMasterItemLocalDataSource.getDamageDescriptionByCode(code, callback);
    }

    @Override
    public void getItemByIdent(@NonNull String ident, ILoadMasterItemCallback callback) {
        mMasterItemLocalDataSource.getItemByIdent(ident, callback);
    }

    @Override
    public void getItemByAltId(@NonNull String altId, ILoadMasterItemCallback callback) {
        mMasterItemLocalDataSource.getItemByAltId(altId, callback);
    }

    @Override
    public void changeStoreCode(@NonNull String storeCode, @NonNull StoreCodeChangeCallback storeCodeChangeCallback) {
        mMasterItemLocalDataSource.changeStoreCode(storeCode, storeCodeChangeCallback);
    }

    public LiveData<PagingData<MasterItem>> getMasterData(QueryMasterItem queryMasterItem) {
        Pager<Integer, MasterItem> pager = new Pager<>(
                new PagingConfig(Utils.PAGE_SIZE),
                () -> mMasterItemLocalDataSource.getMasterData(queryMasterItem)
        );
        return PagingLiveData.getLiveData(pager);
    }
}
