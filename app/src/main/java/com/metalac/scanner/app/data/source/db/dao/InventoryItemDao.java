package com.metalac.scanner.app.data.source.db.dao;

import androidx.annotation.Nullable;
import androidx.paging.PagingSource;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.metalac.scanner.app.models.InventoryExportItem;
import com.metalac.scanner.app.models.InventoryItem;
import com.metalac.scanner.app.models.InventoryItemWithDamageDesc;
import com.metalac.scanner.app.models.ProductPreviewItem;

import java.util.List;

@Dao
public interface InventoryItemDao {

    @Insert
    long insertInventoryItem(InventoryItem inventoryItem);

    @Query("SELECT inventory_items.*, damage_info.description AS damage_desc " +
            "FROM inventory_items " +
            "LEFT JOIN damage_info ON inventory_items.damage_code = damage_info.code " +
            "WHERE inventory_items.id = :id")
    InventoryItemWithDamageDesc getItemById(long id);

    @Query("SELECT m.name AS productName, " +
            "m.price AS productPrice, " +
            "m.unit_of_measure AS measureUnit, " +
            "i.id AS inventoryId, " +
            "m.ident AS ident, " +
            "m.barcode AS barcode, " +
            "i.quantity AS quantity, " +
            "i.status AS status, " +
            "i.index_in_list AS indexInInventoryList, " +
            "CASE " +
            "  WHEN i.exp_date IS NOT NULL AND i.exp_date != '' THEN 1 " +
            "  WHEN i.note IS NOT NULL AND i.note != '' THEN 1 " +
            "  WHEN i.damage_code IS NOT NULL AND i.damage_code != '' THEN 1 " +
            "  ELSE 0 " +
            "END AS hasExtraInfo " +
            "FROM inventory_items i " +
            "JOIN master_items m ON i.ident = m.ident " +
            "WHERE i.inventory_list_id = :inventoryListId " +
            "ORDER BY i.id DESC LIMIT 5")
    List<ProductPreviewItem> getItemsForDisplay(int inventoryListId);

    @Query("SELECT m.name AS productName, " +
            "m.price AS productPrice, " +
            "m.unit_of_measure AS measureUnit, " +
            "i.id AS inventoryId, " +
            "m.ident AS ident, " +
            "m.barcode AS barcode, " +
            "i.quantity AS quantity, " +
            "i.status AS status, " +
            "i.index_in_list AS indexInInventoryList, " +
            "CASE " +
            "  WHEN i.exp_date IS NOT NULL AND i.exp_date != '' THEN 1 " +
            "  WHEN i.note IS NOT NULL AND i.note != '' THEN 1 " +
            "  WHEN i.damage_code IS NOT NULL AND i.damage_code != '' THEN 1 " +
            "  ELSE 0 " +
            "END AS hasExtraInfo " +
            "FROM inventory_items i " +
            "JOIN master_items m ON i.ident = m.ident " +
            "WHERE i.id = :inventoryItemId")
    ProductPreviewItem getProductPreviewByInventoryItemId(int inventoryItemId);

    @Query("SELECT m.name AS productName, " +
            "m.price AS productPrice, " +
            "m.unit_of_measure AS measureUnit, " +
            "i.id AS inventoryId, " +
            "m.ident AS ident, " +
            "m.barcode AS barcode, " +
            "i.quantity AS quantity, " +
            "i.status AS status, " +
            "i.index_in_list AS indexInInventoryList, " +
            "CASE " +
            "   WHEN i.exp_date IS NOT NULL AND i.exp_date != '' THEN 1 " +
            "   WHEN i.note IS NOT NULL AND i.note != '' THEN 1 " +
            "   WHEN i.damage_code IS NOT NULL AND i.damage_code != '' THEN 1 " +
            "   ELSE 0 " +
            "END AS hasExtraInfo " +
            "FROM master_items m " +
            "JOIN inventory_items i ON m.ident = i.ident " +
            "WHERE (:ident IS NULL OR m.ident LIKE '%' || :ident || '%') " +
            "AND (:barcode IS NULL OR m.barcode LIKE '%' || :barcode || '%') " +
            "AND (:active IS NULL OR m.active = :active) " +
            "AND (:accounting IS NULL OR m.accounting = :accounting) " +
            "AND (:unitOfMeasure IS NULL OR m.unit_of_measure = :unitOfMeasure) " +
            "AND (:price IS NULL OR m.price = :price) " +
            "AND (:altCode1 IS NULL OR m.alt_code_1 LIKE '%' || :altCode1 || '%') " +
            "AND (:altCode2 IS NULL OR m.alt_code_2 = :altCode2) " +
            "AND (:salesProgram IS NULL OR m.sales_program LIKE '%' || :salesProgram || '%') " +
            "AND (:purchaseProgram IS NULL OR m.purchase_program = :purchaseProgram) " +
            "AND (:name IS NULL OR m.name LIKE '%' || :name || '%') " +
            "AND (:filterText IS NULL OR m.name LIKE '%' || :filterText || '%') " +
            "AND (i.inventory_list_id = :inventoryListId) " +
            "ORDER BY " +
            "   CASE i.status " +
            "       WHEN 'NON_VOIDED' THEN 0 " +
            "       ELSE 1 " +
            "   END ASC, " +
            "   i.index_in_list DESC")
    PagingSource<Integer, ProductPreviewItem> getFilteredInventoryItems(
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
            @Nullable String filterText,
            int inventoryListId
    );

    @Query("SELECT m.name AS productName, " +
            "m.price AS productPrice, " +
            "m.unit_of_measure AS measureUnit, " +
            "i.id AS inventoryId, " +
            "m.ident AS ident, " +
            "m.barcode AS barcode, " +
            "i.quantity AS quantity, " +
            "i.status AS status, " +
            "i.index_in_list AS indexInInventoryList, " +
            "CASE " +
            "  WHEN i.exp_date IS NOT NULL AND i.exp_date != '' THEN 1 " +
            "  WHEN i.note IS NOT NULL AND i.note != '' THEN 1 " +
            "  WHEN i.damage_code IS NOT NULL AND i.damage_code != '' THEN 1 " +
            "  ELSE 0 " +
            "END AS hasExtraInfo " +
            "FROM inventory_items i " +
            "JOIN master_items m ON i.ident = m.ident " +
            "WHERE i.inventory_list_id = :inventoryListId " +
            "ORDER BY " +
            "   CASE i.status " +
            "       WHEN 'NON_VOIDED' THEN 0 " +
            "       ELSE 1 " +
            "   END ASC, " +
            "   i.index_in_list DESC")
    PagingSource<Integer, ProductPreviewItem> getAllInventoryItemsPaged(int inventoryListId);

    @Update
    int updateInventoryItem(InventoryItem inventoryItem);

    @Query("DELETE FROM inventory_items")
    void deleteAllInventoryData();

    @Query("DELETE FROM sqlite_sequence WHERE name = 'inventory_items'")
    void resetInventoryData();

    @Transaction
    default void deleteAndRestartAllInventoryData() {
        deleteAllInventoryData();
        resetInventoryData();
    }

    @Query("SELECT " +
            "i.device_number AS deviceNumber, " +
            "i.ident AS ident, " +
            "i.index_in_list AS indexInList, " +
            "i.inventory_list_id AS inventoryListId, " +
            "i.quantity AS quantity, " +
            "i.store_code AS storeCode, " +
            "l.name AS listName, " +
            "CASE i.status " +
            "   WHEN 0 THEN 'VOID' " +
            "   WHEN 1 THEN 'VOIDED' " +
            "   WHEN 2 THEN 'NON_VOIDED' " +
            "   ELSE 'UNKNOWN' END AS status, " +
            "i.exp_date AS expDate, " +
            "i.note AS note, " +
            "i.damage_code AS damageCode, " +
            "d.description AS damageDesc " +
            "FROM inventory_items i " +
            "LEFT JOIN inventory_lists l ON i.inventory_list_id = l.id " +
            "LEFT JOIN damage_info d ON i.damage_code = d.code")
    List<InventoryExportItem> getAllInventoryData();


    @Query("SELECT EXISTS(SELECT 1 FROM inventory_items)")
    boolean checkIfAnyInventoryItemExists();

    @Transaction
    default void voidItem(long id, int newIndex) {
        markAsVoided(id);
        insertNegatedItemWithIndex(id, newIndex);
    }

    @Query("SELECT * FROM inventory_items WHERE id = :id")
    InventoryItem getInventoryItemById(long id);

    @Query("UPDATE inventory_items SET status = 1 WHERE id = :id AND status = 2")
    void markAsVoided(long id);

    @Query(
            "INSERT INTO inventory_items (" +
                    "    device_number, store_code, inventory_list_id, ident, quantity, exp_date, damage_code, note, status, index_in_list" +
                    ") " +
                    "SELECT " +
                    "    device_number, store_code, inventory_list_id, ident, -quantity, exp_date, damage_code, note, 0, :newIndexInList " +
                    "FROM inventory_items WHERE id = :id"
    )
    void insertNegatedItemWithIndex(long id, int newIndexInList);

    @Query("SELECT COALESCE(MAX(index_in_list), 0) FROM inventory_items WHERE inventory_list_id = :inventoryListId")
    int getMaxIndexInList(int inventoryListId);
}
