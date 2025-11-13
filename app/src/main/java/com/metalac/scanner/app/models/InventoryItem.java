package com.metalac.scanner.app.models;

import static com.metalac.scanner.app.models.InventoryItem.Status.NON_VOIDED;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.metalac.scanner.app.data.source.PrefManager;
import com.metalac.scanner.app.utils.Utils;

@Entity(
        tableName = "inventory_items",
        foreignKeys = {
                @ForeignKey(
                        entity = InventoryList.class,
                        parentColumns = "id",
                        childColumns = "inventory_list_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = MasterItem.class,
                        parentColumns = "ident",
                        childColumns = "ident"
                ),
                @ForeignKey(
                        entity = DamageInfo.class,
                        parentColumns = "code",
                        childColumns = "damage_code",
                        onDelete = ForeignKey.SET_NULL
                )
        },
        indices = {
                @Index("inventory_list_id"),
                @Index("ident"),
                @Index("damage_code"),
                @Index(value = {"ident", "inventory_list_id"}),
                @Index(value = {"status"})
        }
)
public class InventoryItem {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    @Expose(serialize = false, deserialize = false)
    private long id;

    @ColumnInfo(name = "device_number")
    @Expose
    private String deviceNumber;

    @ColumnInfo(name = "store_code")
    @Expose
    private String storeCode;

    @ColumnInfo(name = "inventory_list_id")
    @Expose
    private int inventoryListId;

    @Expose
    private final String ident;

    @Expose
    private double quantity;

    @Nullable
    @ColumnInfo(name = "exp_date")
    @Expose
    private String expDate;

    @Nullable
    @ColumnInfo(name = "damage_code")
    @Expose
    private String damageCode;

    @Ignore
    @Expose
    private String damageDesc;

    @Nullable
    @Expose
    private String note;

    @Expose(serialize = false, deserialize = false)
    private int status;

    @ColumnInfo(name = "index_in_list")
    @Expose
    private int indexInList;

    public InventoryItem(String ident, double quantity) {
        this.deviceNumber = PrefManager.getDeviceName();
        this.storeCode = PrefManager.getDeviceStoreCode();
        this.ident = ident;
        this.quantity = quantity;
        this.status = Status.NON_VOIDED.getStatusVal();
    }

    public InventoryItem(InventoryItem inventoryItem) {
        this.id = inventoryItem.id;
        this.deviceNumber = inventoryItem.deviceNumber;
        this.storeCode = inventoryItem.storeCode;
        this.inventoryListId = inventoryItem.inventoryListId;
        this.ident = inventoryItem.ident;
        this.quantity = inventoryItem.quantity;
        this.expDate = inventoryItem.expDate;
        this.damageCode = inventoryItem.damageCode;
        this.damageDesc = inventoryItem.damageDesc;
        this.note = inventoryItem.note;
        this.status = inventoryItem.status;
        this.indexInList = inventoryItem.indexInList;
    }

    public InventoryItem(InventoryItemWithDamageDesc itemWithDesc) {
        this(itemWithDesc.getItem());
        this.damageDesc = itemWithDesc.getDamageDesc();
    }

    /**
     * Creates a new {@link InventoryItem} that represents the voided version of this item.
     * It negates the quantity and sets status to {@link Status#VOID}.
     *
     * @return a new {@link InventoryItem} instance with voided status and negative quantity.
     */
    public InventoryItem getVoidInventoryItem() {
        InventoryItem voided = new InventoryItem(this);
        voided.setQuantity(-this.quantity);
        voided.setStatus(Status.VOID.getStatusVal());
        return voided;
    }

    public void addAdditionallyData(@Nullable String expDate, @Nullable String damageCode, @Nullable String note) {
        this.expDate = expDate;
        this.damageCode = damageCode;
        this.note = note;
    }

    public String getDeviceNumber() {
        return deviceNumber;
    }

    public String getStoreCode() {
        return storeCode;
    }

    public int getInventoryListId() {
        return inventoryListId;
    }

    public long getId() {
        return id;
    }

    public String getIdent() {
        return ident;
    }

    public double getQuantity() {
        return quantity;
    }

    @Nullable
    public String getExpDate() {
        return getAdditionalData(expDate);
    }

    @Nullable
    public String getDamageCode() {
        return getAdditionalData(damageCode);
    }

    @Nullable
    public String getNote() {
        return getAdditionalData(note);
    }

    private String getAdditionalData(String data) {
        return data == null || data.isEmpty() ? null : data;
    }

    public int getStatus() {
        return status;
    }

    public boolean isVoided() {
        return NON_VOIDED.getStatusVal() != status;
    }

    public void setDeviceNumber(String deviceNumber) {
        this.deviceNumber = deviceNumber;
    }

    public void setStoreCode(String storeCode) {
        this.storeCode = storeCode;
    }

    public void setInventoryListId(int inventoryListId) {
        this.inventoryListId = inventoryListId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public void setExpDate(@Nullable String expDate) {
        this.expDate = expDate;
    }

    public void setDamageCode(@Nullable String damageCode) {
        this.damageCode = damageCode;
    }

    public String getDamageDesc() {
        return damageDesc;
    }

    public String getDamageInfoString() {
        if (damageDesc == null || damageDesc.isEmpty() || damageCode == null || damageCode.isEmpty()) {
            return "";
        }
        return damageDesc + " (" + damageCode + ")";
    }


    public void setDamageDesc(String damageDesc) {
        this.damageDesc = damageDesc;
    }

    public void setNote(@Nullable String note) {
        this.note = note;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean hasAdditionalData() {
        return Utils.isNotEmptyOrNull(expDate) ||
                Utils.isNotEmptyOrNull(damageCode) ||
                Utils.isNotEmptyOrNull(note);
    }

    public int getIndexInList() {
        return indexInList;
    }

    public void setIndexInList(int indexInList) {
        this.indexInList = indexInList;
    }

    public enum Status {
        VOID(0), VOIDED(1), NON_VOIDED(2);

        private final int statusVal;

        Status(int statusVal) {
            this.statusVal = statusVal;
        }

        public int getStatusVal() {
            return statusVal;
        }

        public static Status fromValue(int value) {
            for (Status status : values()) {
                if (status.statusVal == value) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Unknown value: " + value);
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "InventoryItem{" +
                "id=" + id +
                ", deviceNumber='" + deviceNumber + '\'' +
                ", storeCode='" + storeCode + '\'' +
                ", inventoryListId=" + inventoryListId +
                ", ident='" + ident + '\'' +
                ", quantity=" + quantity +
                ", expDate='" + expDate + '\'' +
                ", damageCode='" + damageCode + '\'' +
                ", damageDesc='" + damageDesc + '\'' +
                ", note='" + note + '\'' +
                ", status=" + status +
                '}';
    }
}
