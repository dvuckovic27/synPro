package com.metalac.scanner.app.models;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Keep
@Entity(tableName = "damage_info")
public class DamageInfo {
    @PrimaryKey
    @NonNull
    @SerializedName("sifra")
    private String code;

    @SerializedName("naziv")
    private String description;

    public DamageInfo(@NonNull String code, String description) {
        this.code = code;
        this.description = description;
    }

    @NonNull
    public String getCode() {
        return code;
    }

    public void setCode(@NonNull String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public String getDamageInfoString() {
        if (description == null || description.isEmpty() || code.isEmpty()) {
            return "";
        }

        return description + " (" + code + ")";
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
