package com.metalac.scanner.app.view;

import android.content.Context;

import com.metalac.scanner.app.R;
import com.metalac.scanner.app.ScannerReaderApplication;

public class ScannerReaderError extends RuntimeException {

    public static final int UNKNOWN = 0;
    public static final int ALREADY_VOIDED = 1;
    public static final int ITEM_NOT_FOUND = 2;

    private final String title;
    private final String message;

    public ScannerReaderError(int errorCode) {
        Context context = ScannerReaderApplication.getAppContext();
        switch (errorCode) {
            case ALREADY_VOIDED:
                this.title = context.getString(R.string.already_voided_error_title);
                this.message = context.getString(R.string.already_voided_error_subtitle);
                break;
            case ITEM_NOT_FOUND:
                this.title = context.getString(R.string.item_not_found_error_title);
                this.message = context.getString(R.string.item_not_found_error_subtitle);
                break;
            case UNKNOWN:
            default:
                this.title = context.getString(R.string.error_title);
                this.message = "";
                break;
        }
    }

    public ScannerReaderError(String title, String message) {
        this.title = title;
        this.message = message;
    }

    public ScannerReaderError(String message) {
        this.title = ScannerReaderApplication.getAppContext().getString(R.string.error_title);
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
