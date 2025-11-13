package com.metalac.scanner.app.models;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;

public class InventoryExportItem {
    @Expose
    private String deviceNumber;
    @Expose
    private String ident;
    @Expose
    private int indexInList;
    @Expose
    private int inventoryListId;
    @Expose
    private double quantity;
    @Expose
    private String storeCode;
    @Expose
    private String listName;
    @Expose
    private String status;
    @Expose
    private String expDate;
    @Expose
    private String damageCode;
    @Expose
    private String damageDesc;
    @Expose
    private String note;

    public InventoryExportItem(String deviceNumber, String ident, int indexInList, int inventoryListId, double quantity, String storeCode, String listName, String status, String expDate, String damageCode, String damageDesc, String note) {
        this.deviceNumber = deviceNumber;
        this.ident = ident;
        this.indexInList = indexInList;
        this.inventoryListId = inventoryListId;
        this.quantity = quantity;
        this.storeCode = storeCode;
        this.listName = listName;
        this.status = status;
        this.expDate = expDate;
        this.damageCode = damageCode;
        this.damageDesc = damageDesc;
        this.note = note;
    }

    public String getDeviceNumber() {
        return deviceNumber;
    }

    public void setDeviceNumber(String deviceNumber) {
        this.deviceNumber = deviceNumber;
    }

    public String getIdent() {
        return ident;
    }

    public void setIdent(String ident) {
        this.ident = ident;
    }

    public int getIndexInList() {
        return indexInList;
    }

    public void setIndexInList(int indexInList) {
        this.indexInList = indexInList;
    }

    public int getInventoryListId() {
        return inventoryListId;
    }

    public void setInventoryListId(int inventoryListId) {
        this.inventoryListId = inventoryListId;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getStoreCode() {
        return storeCode;
    }

    public void setStoreCode(String storeCode) {
        this.storeCode = storeCode;
    }

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExpDate() {
        return expDate;
    }

    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }

    public String getDamageCode() {
        return damageCode;
    }

    public void setDamageCode(String damageCode) {
        this.damageCode = damageCode;
    }

    public String getDamageDesc() {
        return damageDesc;
    }

    public void setDamageDesc(String damageDesc) {
        this.damageDesc = damageDesc;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @NonNull
    @Override
    public String toString() {
        return "InventoryExportItem{" +
                "deviceNumber='" + deviceNumber + '\'' +
                ", ident='" + ident + '\'' +
                ", indexInList=" + indexInList +
                ", inventoryListId=" + inventoryListId +
                ", quantity=" + quantity +
                ", storeCode='" + storeCode + '\'' +
                ", listName='" + listName + '\'' +
                ", itemStatus='" + status + '\'' +
                '}';
    }
}
