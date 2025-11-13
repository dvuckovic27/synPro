package com.metalac.scanner.app.models;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;
import com.metalac.scanner.app.utils.Utils;

import java.util.Objects;

@Keep
@Entity(tableName = "master_items")
public class MasterItem {

    @PrimaryKey
    @NonNull
    @SerializedName("ident")
    private String ident = "";

    @SerializedName("sifoj")
    @ColumnInfo(name = "store_code")
    private String storeCode;

    @SerializedName("datum")
    @ColumnInfo(name = "import_date")
    private String importDate;

    @SerializedName("barkod")
    private String barcode;

    @SerializedName("alt1")
    @ColumnInfo(name = "alt_code_1")
    private String altCode1;

    @SerializedName("alt2")
    @ColumnInfo(name = "alt_code_2")
    private String altCode2;

    @SerializedName("prodpr")
    @ColumnInfo(name = "sales_program")
    private String salesProgram;

    @SerializedName("nabpr")
    @ColumnInfo(name = "purchase_program")
    private String purchaseProgram;

    @SerializedName("jm")
    @ColumnInfo(name = "unit_of_measure")
    private String unitOfMeasure;

    @SerializedName("brdec")
    @ColumnInfo(name = "decimal_places")
    private int decimalPlaces;

    @SerializedName("nazart")
    private String name;

    @SerializedName("maxkol")
    @ColumnInfo(name = "max_count_qty")
    private int maxCountQty;

    @SerializedName("aktivan")
    private int active;

    @SerializedName("knjg")
    private int accounting;

    @SerializedName("cena")
    private double price;

    @SerializedName("kolerp")
    @ColumnInfo(name = "quantity_erp")
    private double quantityErp;

    public MasterItem() {
        //Default constructor needed for JSON deserialization libraries like Gson.
        //Do not remove.
    }

    public String getStoreCode() {
        return storeCode;
    }

    public void setStoreCode(String storeCode) {
        this.storeCode = storeCode;
    }

    public String getImportDate() {
        return importDate;
    }

    public void setImportDate(String importDate) {
        this.importDate = importDate;
    }

    @NonNull
    public String getIdent() {
        return ident;
    }

    public void setIdent(@NonNull String ident) {
        this.ident = ident;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getAltCode1() {
        return altCode1;
    }

    public void setAltCode1(String altCode1) {
        this.altCode1 = altCode1;
    }

    public String getAltCode2() {
        return altCode2;
    }

    public void setAltCode2(String altCode2) {
        this.altCode2 = altCode2;
    }

    public String getSalesProgram() {
        return salesProgram;
    }

    public void setSalesProgram(String salesProgram) {
        this.salesProgram = salesProgram;
    }

    public String getPurchaseProgram() {
        return purchaseProgram;
    }

    public void setPurchaseProgram(String purchaseProgram) {
        this.purchaseProgram = purchaseProgram;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public int getDecimalPlaces() {
        return decimalPlaces;
    }

    public void setDecimalPlaces(int decimalPlaces) {
        this.decimalPlaces = decimalPlaces;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxCountQty() {
        return maxCountQty;
    }

    public void setMaxCountQty(int maxCountQty) {
        this.maxCountQty = maxCountQty;
    }

    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
    }

    public int getAccounting() {
        return accounting;
    }

    public void setAccounting(int accounting) {
        this.accounting = accounting;
    }

    public double getPrice() {
        return price;
    }

    public String getPriceString() {
        return Utils.getFormatedPrice(price);
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getQuantityErp() {
        return quantityErp;
    }

    public String getQuantityErpString() {
        return Utils.getQuantityString(quantityErp);
    }

    public void setQuantityErp(double quantityErp) {
        this.quantityErp = quantityErp;
    }

    @Override
    public boolean equals(@Nullable Object masterItemObj) {
        if (this == masterItemObj) return true;
        if (!(masterItemObj instanceof MasterItem)) return false;

        MasterItem newMasterItem = (MasterItem) masterItemObj;

        return this.decimalPlaces == newMasterItem.decimalPlaces &&
                this.maxCountQty == newMasterItem.maxCountQty &&
                this.active == newMasterItem.active &&
                this.accounting == newMasterItem.accounting &&
                Double.compare(this.price, newMasterItem.price) == 0 &&
                Double.compare(this.quantityErp, newMasterItem.quantityErp) == 0 &&
                Objects.equals(this.storeCode, newMasterItem.storeCode) &&
                Objects.equals(this.importDate, newMasterItem.importDate) &&
                Objects.equals(this.ident, newMasterItem.ident) &&
                Objects.equals(this.barcode, newMasterItem.barcode) &&
                Objects.equals(this.altCode1, newMasterItem.altCode1) &&
                Objects.equals(this.altCode2, newMasterItem.altCode2) &&
                Objects.equals(this.salesProgram, newMasterItem.salesProgram) &&
                Objects.equals(this.purchaseProgram, newMasterItem.purchaseProgram) &&
                Objects.equals(this.unitOfMeasure, newMasterItem.unitOfMeasure) &&
                Objects.equals(this.name, newMasterItem.name);
    }


    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @NonNull
    @Override
    public String toString() {
        return "MasterItem{" +
                "storeCode='" + storeCode + '\'' +
                ", importDate='" + importDate + '\'' +
                ", ident='" + ident + '\'' +
                ", barcode='" + barcode + '\'' +
                ", altCode1='" + altCode1 + '\'' +
                ", altCode2='" + altCode2 + '\'' +
                ", salesProgram='" + salesProgram + '\'' +
                ", purchaseProgram='" + purchaseProgram + '\'' +
                ", unitOfMeasure='" + unitOfMeasure + '\'' +
                ", decimalPlaces=" + decimalPlaces +
                ", name='" + name + '\'' +
                ", maxCountQty=" + maxCountQty +
                ", active=" + active +
                ", accounting=" + accounting +
                ", price=" + price +
                ", quantityErp=" + quantityErp +
                '}';
    }
}
