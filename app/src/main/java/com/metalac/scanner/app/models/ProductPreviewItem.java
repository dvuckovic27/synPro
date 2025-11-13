package com.metalac.scanner.app.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.metalac.scanner.app.utils.Utils;

import java.util.Objects;

/**
 * Represents a product with a name, price, unit of measure, and quantity.
 * The product price is formatted according to the Serbian locale with
 * two decimal places and appended with the currency "RSD".
 */
public class ProductPreviewItem {
    private final String productName;
    private final double productPrice;
    private final String measureUnit;
    private final long inventoryId;
    private final String ident;
    private final String barcode;
    private final double quantity;
    private int status;
    private int indexInInventoryList;
    private final boolean hasExtraInfo;

    public ProductPreviewItem(String productName, double productPrice, String measureUnit, long inventoryId, String ident, String barcode, double quantity, int status, int indexInInventoryList, boolean hasExtraInfo) {
        this.productName = productName;
        this.productPrice = productPrice;
        this.measureUnit = measureUnit;
        this.inventoryId = inventoryId;
        this.barcode = barcode;
        this.ident = ident;
        this.quantity = quantity;
        this.status = status;
        this.indexInInventoryList = indexInInventoryList;
        this.hasExtraInfo = hasExtraInfo;
    }

    public ProductPreviewItem(MasterItem masterItem) {
        this.productName = masterItem.getName();
        this.productPrice = masterItem.getPrice();
        this.measureUnit = masterItem.getUnitOfMeasure();
        this.ident = masterItem.getIdent();
        this.barcode = masterItem.getBarcode();
        this.inventoryId = -1;
        this.quantity = masterItem.getQuantityErp();
        this.hasExtraInfo = false;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductPrice() {
        return Utils.getFormatedPrice(productPrice);
    }

    public long getInventoryId() {
        return inventoryId;
    }


    public String getIdent() {
        return ident;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getQuantityString() {
        return Utils.getQuantityString(quantity) + " " + measureUnit;
    }

    public double getQuantity() {
        return quantity;
    }

    public InventoryItem.Status getStatus() {
        return InventoryItem.Status.fromValue(status);
    }

    public int getIndexInInventoryList() {
        return indexInInventoryList;
    }

    public boolean hasExtraInfo() {
        return hasExtraInfo;
    }

    @Override
    public boolean equals(@Nullable Object productPreview) {
        if (this == productPreview) return true;
        if (!(productPreview instanceof ProductPreviewItem)) return false;

        ProductPreviewItem newProductPreviewItem = (ProductPreviewItem) productPreview;

        return Double.compare(this.productPrice, newProductPreviewItem.productPrice) == 0 &&
                this.inventoryId == newProductPreviewItem.inventoryId &&
                this.quantity == newProductPreviewItem.quantity &&
                this.indexInInventoryList == newProductPreviewItem.indexInInventoryList &&
                this.hasExtraInfo == newProductPreviewItem.hasExtraInfo &&
                this.status == newProductPreviewItem.status &&
                Objects.equals(this.productName, newProductPreviewItem.productName) &&
                Objects.equals(this.measureUnit, newProductPreviewItem.measureUnit) &&
                Objects.equals(this.ident, newProductPreviewItem.ident) &&
                Objects.equals(this.barcode, newProductPreviewItem.barcode);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @NonNull
    @Override
    public String toString() {
        return "ProductPreviewItem{" +
                "productName='" + productName + '\'' +
                ", productPrice=" + productPrice +
                ", measureUnit='" + measureUnit + '\'' +
                ", inventoryId=" + inventoryId +
                ", ident='" + ident + '\'' +
                ", barcode='" + barcode + '\'' +
                ", quantity=" + quantity +
                ", status=" + status +
                ", indexInInventoryList=" + indexInInventoryList +
                ", hasExtraInfo=" + hasExtraInfo +
                '}';
    }
}
