package com.metalac.scanner.app.models;

import androidx.annotation.NonNull;

public class ScanResult {
    public final String data;
    public final String type;

    public ScanResult(String data, String type) {
        this.data = data;
        this.type = type;
    }

    public boolean isValid() {
        return data != null && !data.isEmpty();
    }

    @NonNull
    @Override
    public String toString() {
        return "ScanResult{" +
                "data='" + data + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
