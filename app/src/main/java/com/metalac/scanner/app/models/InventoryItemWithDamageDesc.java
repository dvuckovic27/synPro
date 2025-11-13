package com.metalac.scanner.app.models;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;

public class InventoryItemWithDamageDesc {
    @Embedded
    private InventoryItem item;

    @ColumnInfo(name = "damage_desc")
    private String damageDesc;

    public InventoryItem getItem() {
        return item;
    }

    public void setItem(InventoryItem item) {
        this.item = item;
    }

    public String getDamageDesc() {
        return damageDesc;
    }

    public void setDamageDesc(String damageDesc) {
        this.damageDesc = damageDesc;
    }
}
