package com.metalac.scanner.app.data.source.db.dao;

import androidx.annotation.Nullable;
import androidx.paging.PagingSource;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Upsert;

import com.metalac.scanner.app.models.DamageInfo;
import com.metalac.scanner.app.models.MasterItem;

import java.util.List;

@Dao
public interface MasterItemDao {
    @Update
    void update(MasterItem item);

    @Delete
    void delete(MasterItem item);

    @Query("SELECT * FROM master_items WHERE barcode = :barcode")
    MasterItem getByBarcode(String barcode);

    @Query("SELECT * FROM master_items WHERE alt_code_1 = :altCode1")
    MasterItem getByAltCode1(int altCode1);

    @Query("SELECT * FROM master_items")
    List<MasterItem> getAll();

    @Upsert
    void upsertAll(List<MasterItem> masterItems);

    @Upsert
    void upsertDamageInfo(List<DamageInfo> damageInfo);

    @Query("SELECT * FROM master_items " +
            "WHERE (:ident IS NULL OR ident LIKE '%' || :ident || '%') " +
            "AND (:barcode IS NULL OR barcode LIKE '%' || :barcode || '%') " +
            "AND (:active IS NULL OR active = :active) " +
            "AND (:accounting IS NULL OR accounting = :accounting) " +
            "AND (:unitOfMeasure IS NULL OR unit_of_measure = :unitOfMeasure) " +
            "AND (:price IS NULL OR price = :price) " +
            "AND (:altCode1 IS NULL OR alt_code_1 LIKE '%' || :altCode1 || '%') " +
            "AND (:altCode2 IS NULL OR alt_code_2 = :altCode2) " +
            "AND (:salesProgram IS NULL OR sales_program LIKE '%' || :salesProgram || '%') " +
            "AND (:purchaseProgram IS NULL OR purchase_program = :purchaseProgram) " +
            "AND (:name IS NULL OR name LIKE '%' || :name || '%')" +
            "AND (:filterText IS NULL OR name LIKE '%' || :filterText || '%') "
    )
    PagingSource<Integer, MasterItem> getFilteredItems(
            @Nullable String ident,
            @Nullable String barcode,
            @Nullable String altCode1,
            @Nullable String altCode2,
            @Nullable String salesProgram,
            @Nullable String purchaseProgram,
            @Nullable String unitOfMeasure,
            @Nullable String name,
            @Nullable Integer active,
            @Nullable Integer accounting,
            @Nullable Double price,
            @Nullable String filterText
    );

    @Query("SELECT * FROM master_items")
    PagingSource<Integer, MasterItem> getAllPaged();

    @Query("SELECT * FROM master_items WHERE ident = :ident")
    MasterItem getItemByIdent(String ident);

    @Query("SELECT DISTINCT unit_of_measure FROM master_items")
    List<String> getUniteOfMeasure();

    @Query("SELECT * FROM damage_info")
    List<DamageInfo> getDamageInfo();

    @Query("SELECT description FROM damage_info WHERE code = :code")
    String getDamageNameByCode(String code);

    @Query("SELECT * FROM master_items WHERE alt_code_1 = :altId1")
    MasterItem getItemByAltId1(String altId1);

    @Query("SELECT * FROM master_items WHERE alt_code_2 = :altId2")
    MasterItem getItemByAltId2(String altId2);

    @Query("DELETE FROM master_items")
    void clearAllMasterItems();

    @Query("DELETE FROM damage_info")
    void clearAllDamageInfo();
}
