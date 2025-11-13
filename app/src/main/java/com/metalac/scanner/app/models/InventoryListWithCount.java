package com.metalac.scanner.app.models;

import androidx.room.Embedded;
import androidx.room.Ignore;

public class InventoryListWithCount {
    @Embedded
    private InventoryList inventoryList;
    private int count;

    public InventoryListWithCount(InventoryList inventoryList, int count) {
        this.inventoryList = inventoryList;
        this.count = count;
    }

    @Ignore
    public InventoryListWithCount(InventoryList inventoryList) {
        this.inventoryList = inventoryList;
        this.count = 0;
    }

    public InventoryList getInventoryList() {
        return inventoryList;
    }

    public void setInventoryList(InventoryList inventoryList) {
        this.inventoryList = inventoryList;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
