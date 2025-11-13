package com.metalac.scanner.app.helpers;

import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.metalac.scanner.app.R;
import com.metalac.scanner.app.ScannerReaderApplication;
import com.metalac.scanner.app.view.ScannerReaderError;

/**
 * Configuration class for building dialogs.
 * Holds dialog properties such as icon, title, buttons, and click listeners.
 */
public class DialogConfig {
    @Nullable
    private final Context context;
    private final LayoutInflater inflater;
    private int iconResId;
    private String title;
    private String subtitle;
    private String positiveButton;
    private String negativeButton;
    private boolean cancelable = true;
    private DialogInterface.OnClickListener clickListener;

    public DialogConfig(@Nullable Context context, LayoutInflater inflater) {
        this.context = context;
        this.inflater = inflater;
    }

    /**
     * Creates a DialogConfig from a {@link ScannerReaderError}.
     * Sets the title and subtitle from the error, and positive button to "Ok" string resource or default.
     *
     * @param context            Context, nullable (used to resolve string resources)
     * @param inflater           LayoutInflater instance, must not be null
     * @param scannerReaderError Error instance providing title and message
     */
    public DialogConfig(@Nullable Context context, LayoutInflater inflater, ScannerReaderError scannerReaderError) {
        String defaultPositiveButtonText = "Ok";
        this.context = context;
        this.inflater = inflater;
        this.title = scannerReaderError.getTitle();
        this.subtitle = scannerReaderError.getMessage();
        this.positiveButton = context == null ? defaultPositiveButtonText : context.getString(R.string.ok);
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    @NonNull
    public DialogConfig setTitle(String title) {
        this.title = title;
        return this;
    }

    @NonNull
    public DialogConfig setSubtitle(String subtitle) {
        this.subtitle = subtitle;
        return this;
    }

    @NonNull
    public DialogConfig setPositiveButton(String positiveButton) {
        this.positiveButton = positiveButton;
        return this;
    }

    @NonNull
    public DialogConfig setNegativeButton(String negativeButton) {
        if (!TextUtils.isEmpty(negativeButton)) {
            this.negativeButton = negativeButton;
        }
        return this;
    }

    @NonNull
    public DialogConfig setTitle(@StringRes int titleRes) {
        this.title = getString(titleRes);
        return this;
    }

    @NonNull
    public DialogConfig setSubtitle(@StringRes int subtitleRes) {
        this.subtitle = getString(subtitleRes);
        return this;
    }

    @NonNull
    public DialogConfig setPositiveButton(@StringRes int positiveButtonRes) {
        this.positiveButton = getString(positiveButtonRes);
        return this;
    }

    @NonNull
    public DialogConfig setNegativeButton(@StringRes int negativeButtonRes) {
        this.negativeButton = getString(negativeButtonRes);
        return this;
    }

    @NonNull
    public DialogConfig setCancelable(boolean cancelable) {
        this.cancelable = cancelable;
        return this;
    }

    @NonNull
    public DialogConfig setClickListener(DialogInterface.OnClickListener clickListener) {
        if (clickListener != null) {
            this.clickListener = clickListener;
        }
        return this;
    }

    @NonNull
    public Context getContext() {
        if (context == null) {
            return ScannerReaderApplication.getAppContext();
        }
        return context;
    }

    public LayoutInflater getInflater() {
        return inflater;
    }

    public int getIconResId() {
        return iconResId;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getPositiveButton() {
        return positiveButton;
    }

    public String getNegativeButton() {
        return negativeButton;
    }

    public boolean isCancelable() {
        return cancelable;
    }

    public DialogInterface.OnClickListener getClickListener() {
        return clickListener;
    }

    public boolean isValid() {
        return context != null && inflater != null;
    }

    private String getString(@StringRes int stringRes) {
        return context == null ? ScannerReaderApplication.getAppContext().getString(stringRes) : context.getString(stringRes);
    }
}
