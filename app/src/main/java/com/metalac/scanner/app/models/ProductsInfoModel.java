package com.metalac.scanner.app.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ProductsInfoModel {
    @SerializedName("maticni")
    private List<MasterItem> masterItems;

    @SerializedName("ostecenja")
    private List<DamageInfo> damageInfo;

    public List<MasterItem> getMasterItems() {
        return masterItems;
    }

    public void setMasterItems(List<MasterItem> masterItems) {
        this.masterItems = masterItems;
    }

    public List<DamageInfo> getDamageInfo() {
        return damageInfo;
    }

    public void setDamageInfo(List<DamageInfo> damageInfo) {
        this.damageInfo = damageInfo;
    }
}
