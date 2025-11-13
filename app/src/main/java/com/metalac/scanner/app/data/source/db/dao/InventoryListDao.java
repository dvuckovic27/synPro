package com.metalac.scanner.app.data.source.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.metalac.scanner.app.models.InventoryList;
import com.metalac.scanner.app.models.InventoryListWithCount;

import java.util.List;

@Dao
public interface InventoryListDao {

    @Insert
    long insert(InventoryList item);

    @Query("SELECT il.*, COUNT(ii.id) AS count " +
            "FROM inventory_lists il " +
            "LEFT JOIN inventory_items ii ON il.id = ii.inventory_list_id " +
            "GROUP BY il.id")
    List<InventoryListWithCount> getAllInventoryListsWithItemCount();

    @Query("SELECT * FROM inventory_lists WHERE id = :inventoryListId")
    InventoryList getListById(int inventoryListId);

    @Query("SELECT * FROM inventory_lists WHERE selected = 1")
    InventoryList getCurrentList();

    @Update
    int updateInventoryList(InventoryList inventoryList);

    @Query("DELETE FROM inventory_lists")
    void deleteAllInventoryListData();

    @Query("DELETE FROM sqlite_sequence WHERE name = 'inventory_lists'")
    void resetInventoryListData();

    @Query("SELECT EXISTS(SELECT 1 FROM inventory_lists)")
    boolean checkIfAnyInventoryListExists();

    @Transaction
    default void deleteAndRestartAllInventoryListData() {
        deleteAllInventoryListData();
        resetInventoryListData();
    }
}
