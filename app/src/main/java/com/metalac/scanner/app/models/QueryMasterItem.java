package com.metalac.scanner.app.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class QueryMasterItem implements Parcelable {

    private final String ident;
    private final String barcode;
    private final String altCode1;
    private final String altCode2;
    private final String salesProgram;
    private final String purchaseProgram;
    private final String unitOfMeasure;
    private final String name;
    private final Integer active;
    private final Integer accounting;
    private final Double price;
    private final String filterText;

    public QueryMasterItem(QueryMasterBuilder builder) {
        this.ident = builder.ident;
        this.barcode = builder.barcode;
        this.altCode1 = builder.altCode1;
        this.altCode2 = builder.altCode2;
        this.salesProgram = builder.salesProgram;
        this.purchaseProgram = builder.purchaseProgram;
        this.unitOfMeasure = builder.unitOfMeasure;
        this.name = builder.name;
        this.active = builder.active;
        this.accounting = builder.accounting;
        this.price = builder.price;
        this.filterText = builder.filterText;
    }

    protected QueryMasterItem(Parcel in) {
        ident = in.readString();
        barcode = in.readString();
        altCode1 = in.readString();
        altCode2 = in.readString();
        salesProgram = in.readString();
        purchaseProgram = in.readString();
        unitOfMeasure = in.readString();
        name = in.readString();
        if (in.readByte() == 0) {
            active = null;
        } else {
            active = in.readInt();
        }
        if (in.readByte() == 0) {
            accounting = null;
        } else {
            accounting = in.readInt();
        }
        if (in.readByte() == 0) {
            price = 0.0;
        } else {
            price = in.readDouble();
        }
        filterText = in.readString();
    }

    public static final Creator<QueryMasterItem> CREATOR = new Creator<>() {
        @Override
        public QueryMasterItem createFromParcel(Parcel in) {
            return new QueryMasterItem(in);
        }

        @Override
        public QueryMasterItem[] newArray(int size) {
            return new QueryMasterItem[size];
        }
    };

    public String getIdent() {
        return ident.isEmpty() ? null : ident;
    }

    public String getBarcode() {
        return barcode.isEmpty() ? null : barcode;
    }

    public String getAltCode1() {
        return altCode1.isEmpty() ? null : altCode1;
    }

    public String getAltCode2() {
        return altCode2.isEmpty() ? null : altCode2;
    }

    public String getSalesProgram() {
        return salesProgram.isEmpty() ? null : salesProgram;
    }

    public String getPurchaseProgram() {
        return purchaseProgram.isEmpty() ? null : purchaseProgram;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure.isEmpty() ? null : unitOfMeasure;
    }

    public String getName() {
        return name.isEmpty() ? "" : name;
    }

    public Integer getActive() {
        return active;
    }

    public Integer getAccounting() {
        return accounting;
    }

    public Double getPrice() {
        return price != 0 ? price : null;
    }

    public String getFilterText() {
        return filterText;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(ident);
        dest.writeString(barcode);
        dest.writeString(altCode1);
        dest.writeString(altCode2);
        dest.writeString(salesProgram);
        dest.writeString(purchaseProgram);
        dest.writeString(unitOfMeasure);
        dest.writeString(name);
        dest.writeInt(active);
        dest.writeInt(accounting);
        dest.writeDouble(price);
        dest.writeString(filterText);
    }

    public boolean isNoFilterApplied() {
        return ident.isEmpty() && barcode.isEmpty() && altCode1.isEmpty() && altCode2.isEmpty() &&
                salesProgram.isEmpty() && purchaseProgram.isEmpty() && unitOfMeasure.isEmpty() &&
                name.isEmpty() && active == 1 && accounting == 1 && price == 0.0 && filterText.isEmpty();
    }

    @NonNull
    @Override
    public String toString() {
        return "ClassName{" +
                "ident='" + ident + '\'' +
                ", barcode='" + barcode + '\'' +
                ", altCode1='" + altCode1 + '\'' +
                ", altCode2='" + altCode2 + '\'' +
                ", salesProgram='" + salesProgram + '\'' +
                ", purchaseProgram='" + purchaseProgram + '\'' +
                ", unitOfMeasure='" + unitOfMeasure + '\'' +
                ", name='" + name + '\'' +
                ", active=" + active +
                ", accounting=" + accounting +
                ", price=" + price +
                ", filterText=" + filterText +
                '}';
    }

    public static class QueryMasterBuilder {
        private String ident;
        private String barcode;
        private String altCode1;
        private String altCode2;
        private String salesProgram;
        private String purchaseProgram;
        private String unitOfMeasure;
        private String name;
        private Integer active;
        private Integer accounting;
        private Double price;
        private String filterText;

        public QueryMasterBuilder setPrice(Double price) {
            this.price = price;
            return this;
        }

        public QueryMasterBuilder setAccounting(Integer accounting) {
            this.accounting = accounting;
            return this;
        }

        public QueryMasterBuilder setActive(Integer active) {
            this.active = active;
            return this;
        }

        public QueryMasterBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public QueryMasterBuilder setUnitOfMeasure(String unitOfMeasure) {
            this.unitOfMeasure = unitOfMeasure;
            return this;
        }

        public QueryMasterBuilder setPurchaseProgram(String purchaseProgram) {
            this.purchaseProgram = purchaseProgram;
            return this;
        }

        public QueryMasterBuilder setSalesProgram(String salesProgram) {
            this.salesProgram = salesProgram;
            return this;
        }

        public QueryMasterBuilder setAltCode2(String altCode2) {
            this.altCode2 = altCode2;
            return this;
        }

        public QueryMasterBuilder setAltCode1(String altCode1) {
            this.altCode1 = altCode1;
            return this;
        }

        public QueryMasterBuilder setBarcode(String barcode) {
            this.barcode = barcode;
            return this;
        }

        public QueryMasterBuilder setIdent(String ident) {
            this.ident = ident;
            return this;
        }

        public QueryMasterBuilder setFilterText(String filterText) {
            this.filterText = filterText;
            return this;
        }

        public QueryMasterItem build() {
            return new QueryMasterItem(this);
        }
    }

    public QueryMasterBuilder toBuilder() {
        return new QueryMasterBuilder()
                .setIdent(this.ident)
                .setBarcode(this.barcode)
                .setAltCode1(this.altCode1)
                .setAltCode2(this.altCode2)
                .setSalesProgram(this.salesProgram)
                .setPurchaseProgram(this.purchaseProgram)
                .setUnitOfMeasure(this.unitOfMeasure)
                .setName(this.name)
                .setActive(this.active)
                .setAccounting(this.accounting)
                .setPrice(this.price)
                .setFilterText(this.filterText);
    }
}
