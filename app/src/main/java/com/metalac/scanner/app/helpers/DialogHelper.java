package com.metalac.scanner.app.helpers;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.metalac.scanner.app.R;
import com.metalac.scanner.app.models.InventoryItem;

/**
 * Utility class for creating and managing dialogs with customizable content and actions.
 * <p>
 * Provides methods to build dialogs featuring titles, subtitles, icons, and configurable buttons.
 */
public class DialogHelper {

    /**
     * Creates a progress dialog using the given dialog configuration.
     *
     * @param dialogConfig Configuration for the dialog; must have valid context and inflater
     * @return The created progress {@link AlertDialog}
     */
    @NonNull
    public static AlertDialog createProgressDialog(@NonNull DialogConfig dialogConfig) {
        AlertDialog alertDialog = new AlertDialog.Builder(dialogConfig.getContext(), R.style.DialogWithCorners).create();
        View dialogView = dialogConfig.getInflater().inflate(R.layout.progress_dialog, null);
        setupTitleAndSubtitle(dialogView, dialogConfig.getTitle(), dialogConfig.getSubtitle());
        alertDialog.setCancelable(false);

        alertDialog.setView(dialogView);

        return alertDialog;
    }

    /**
     * Creates and returns an {@link AlertDialog} based on the provided {@link DialogConfig} object.
     * <p>
     * If the configuration is invalid (as determined by {@link DialogConfig#isValid()}), the method returns {@code null}.
     * Otherwise, it inflates a custom dialog layout, sets up the title, subtitle, icon, buttons, and click listener,
     * and returns a fully constructed and styled dialog using the {@code R.style.DialogWithCorners} theme.
     * </p>
     *
     * @param dialogConfig the configuration object containing all dialog properties such as title, icon, buttons, and context
     * @return a configured {@link AlertDialog}, or {@code null} if the configuration is invalid
     */
    @Nullable
    private static AlertDialog createDialog(@NonNull DialogConfig dialogConfig) {
        if (!dialogConfig.isValid()) {
            return null;
        }

        AlertDialog alertDialog = new AlertDialog.Builder(dialogConfig.getContext(), R.style.DialogWithCorners).create();
        View dialogView = inflateAndSetupView(dialogConfig.getInflater(), dialogConfig.getIconResId(), alertDialog);

        setupTitleAndSubtitle(dialogView, dialogConfig.getTitle(), dialogConfig.getSubtitle());
        setupDialogButtons(dialogView, alertDialog, dialogConfig.getPositiveButton(), dialogConfig.getNegativeButton(), dialogConfig.getClickListener());

        alertDialog.setCancelable(dialogConfig.isCancelable());

        return alertDialog;
    }

    /**
     * Inflates the dialog view and sets up the icon and associates the view with the AlertDialog.
     *
     * @param inflater    LayoutInflater to inflate the dialog layout
     * @param iconResId   Resource ID for the icon to display
     * @param alertDialog The AlertDialog to which the view will be set
     * @return The inflated and configured dialog view
     */
    @NonNull
    private static View inflateAndSetupView(@NonNull LayoutInflater inflater, int iconResId, @NonNull AlertDialog alertDialog) {
        View dialogView = inflater.inflate(R.layout.dialog_with_icon, null);

        TextView titleTextView = dialogView.findViewById(R.id.tvTitle);
        TextView subtitleTextView = dialogView.findViewById(R.id.tvSubtitle);

        ImageView iconImageView = dialogView.findViewById(R.id.ivIcon);
        iconImageView.setVisibility(View.VISIBLE);

        titleTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        subtitleTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        iconImageView.setImageResource(iconResId);

        alertDialog.setView(dialogView);
        return dialogView;
    }

    private static void setupTitleAndSubtitle(@NonNull View dialogView, @Nullable String title, @Nullable String subtitle) {
        TextView titleTextView = dialogView.findViewById(R.id.tvTitle);
        TextView subtitleTextView = dialogView.findViewById(R.id.tvSubtitle);

        if (title == null || title.isEmpty()) {
            titleTextView.setVisibility(View.GONE);
        } else {
            titleTextView.setText(title);
            titleTextView.setVisibility(View.VISIBLE);
        }

        if (subtitle == null || subtitle.isEmpty()) {
            subtitleTextView.setVisibility(View.GONE);
        } else {
            subtitleTextView.setText(subtitle);
            subtitleTextView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Configures positive and negative buttons for a custom dialog view.
     * <p>
     * Ensures button text and click listeners are set, hides unused buttons,
     * and adjusts their layout to stack vertically if they do not fit side by side.
     *
     * @param dialogView    The root view of the custom dialog (must include tvPositive, tvNegative, dialogRoot).
     * @param alertDialog   The AlertDialog instance to dismiss on button click.
     * @param positiveText  Text for the positive button (null/empty hides the button).
     * @param negativeText  Text for the negative button (null/empty hides the button).
     * @param clickListener Optional listener to handle button clicks.
     */
    private static void setupDialogButtons(@NonNull View dialogView,
                                           @NonNull AlertDialog alertDialog,
                                           @Nullable String positiveText,
                                           @Nullable String negativeText,
                                           @Nullable DialogInterface.OnClickListener clickListener) {

        TextView btnNegative = dialogView.findViewById(R.id.tvNegative);
        TextView btnPositive = dialogView.findViewById(R.id.tvPositive);

        // Configure negative button
        if (negativeText == null || negativeText.isEmpty()) {
            btnNegative.setVisibility(View.GONE);
        } else {
            btnNegative.setVisibility(View.VISIBLE);
            btnNegative.setText(negativeText);
            btnNegative.setOnClickListener(view -> {
                if (clickListener != null) {
                    clickListener.onClick(alertDialog, DialogInterface.BUTTON_NEGATIVE);
                }
                alertDialog.dismiss();
            });
        }

        // Configure positive button
        if (positiveText == null || positiveText.isEmpty()) {
            btnPositive.setVisibility(View.GONE);
        } else {
            btnPositive.setVisibility(View.VISIBLE);
            btnPositive.setText(positiveText);
            btnPositive.setOnClickListener(view -> {
                if (clickListener != null) {
                    clickListener.onClick(alertDialog, DialogInterface.BUTTON_POSITIVE);
                }
                alertDialog.dismiss();
            });
        }
    }

    /**
     * Creates an attention dialog with a predefined attention icon.
     *
     * @param dialogConfig Configuration for the dialog
     * @return The created attention {@link AlertDialog}, or null if configuration invalid
     */
    @Nullable
    public static AlertDialog createAttentionDialog(@NonNull DialogConfig dialogConfig) {
        dialogConfig.setIconResId(R.drawable.ic_attention);
        return createDialog(dialogConfig);
    }

    /**
     * Creates an error dialog with a predefined error icon.
     *
     * @param dialogConfig Configuration for the dialog
     * @return The created error {@link AlertDialog}, or null if configuration invalid
     */
    @Nullable
    public static AlertDialog createErrorDialog(@NonNull DialogConfig dialogConfig) {
        dialogConfig.setIconResId(R.drawable.ic_error);
        return createDialog(dialogConfig);
    }

    /**
     * Creates a success dialog with a predefined success icon.
     *
     * @param dialogConfig Configuration for the dialog
     * @return The created success {@link AlertDialog}, or null if configuration invalid
     */
    @Nullable
    public static AlertDialog createSuccessDialog(@NonNull DialogConfig dialogConfig) {
        dialogConfig.setIconResId(R.drawable.ic_correct);
        return createDialog(dialogConfig);
    }

    /**
     * Creates and shows an error dialog immediately.
     *
     * @param dialogConfig Configuration for the dialog
     */
    public static void showErrorDialog(@NonNull DialogConfig dialogConfig) {
        AlertDialog dialog = createErrorDialog(dialogConfig);
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }

    /**
     * Creates an AlertDialog prompting the user to try again after an error.
     *
     * @param context         The context used to build and display the dialog.
     * @param layoutInflater  The LayoutInflater used to inflate the custom dialog view.
     * @param description     The message or subtitle shown in the dialog explaining the error or reason to try again.
     * @param dialogInterface The click listener for the dialog's positive button (e.g., "OK" button).
     * @return An AlertDialog configured with a title, description, and an OK button.
     */
    public static AlertDialog createTryAgainDialog(@NonNull Context context, LayoutInflater layoutInflater, String description, DialogInterface.OnClickListener dialogInterface) {
        DialogConfig dialogConfig = new DialogConfig(context, layoutInflater);
        dialogConfig.setTitle(context.getString(R.string.error_title)).
                setSubtitle(description).
                setPositiveButton(context.getString(R.string.ok)).
                setClickListener(dialogInterface);
        return createAttentionDialog(dialogConfig);
    }

    public static AlertDialog createAdditionalInfoDialog(DialogConfig dialogConfig, InventoryItem inventoryItem) {
        AlertDialog alertDialog = new AlertDialog.Builder(dialogConfig.getContext(), R.style.DialogWithCorners).create();
        View dialogView = dialogConfig.getInflater().inflate(R.layout.dialog_additional_info, null);
        alertDialog.setView(dialogView);

        EditText etExpDate = dialogView.findViewById(R.id.etExpirationDate);
        EditText etDamage = dialogView.findViewById(R.id.etDamage);
        EditText etNote = dialogView.findViewById(R.id.etNote);

        etExpDate.setText(inventoryItem.getExpDate());
        etDamage.setText(inventoryItem.getDamageInfoString());
        etNote.setText(inventoryItem.getNote());

        alertDialog.setCancelable(dialogConfig.isCancelable());

        TextView tvCancel = dialogView.findViewById(R.id.tvCancel);
        tvCancel.setOnClickListener(v -> alertDialog.dismiss());

        return alertDialog;
    }

    @NonNull
    public static AlertDialog createItemOptionsDialog(DialogConfig dialogConfig) {
        AlertDialog alertDialog = new AlertDialog.Builder(dialogConfig.getContext(), R.style.DialogWithCorners).create();
        View dialogView = dialogConfig.getInflater().inflate(R.layout.options_dialog_with_icon, null);
        alertDialog.setView(dialogView);

        TextView tvCancel = dialogView.findViewById(R.id.tvCancel);
        TextView tvVoid = dialogView.findViewById(R.id.tvVoid);
        TextView tvUpdate = dialogView.findViewById(R.id.tvUpdate);

        TextView tvSubtitle = dialogView.findViewById(R.id.tvSubtitle);
        tvSubtitle.setText(dialogConfig.getSubtitle());

        tvCancel.setOnClickListener(v -> alertDialog.dismiss());
        tvVoid.setOnClickListener(v -> {
            dialogConfig.getClickListener().onClick(alertDialog, DialogInterface.BUTTON_POSITIVE);
            alertDialog.dismiss();
        });
        tvUpdate.setOnClickListener(v -> {
            dialogConfig.getClickListener().onClick(alertDialog, DialogInterface.BUTTON_NEGATIVE);
            alertDialog.dismiss();
        });

        alertDialog.setCancelable(false);

        return alertDialog;
    }

    public static void showVoidItemDialog(DialogConfig dialogConfig) {
        AlertDialog alertDialog = new AlertDialog.Builder(dialogConfig.getContext(), R.style.DialogWithCorners).create();
        View dialogView = dialogConfig.getInflater().inflate(R.layout.dialog_void, null);
        alertDialog.setView(dialogView);

        TextView tvPositive = dialogView.findViewById(R.id.tvPositive);
        TextView tvNegative = dialogView.findViewById(R.id.tvNegative);

        TextView tvItemData = dialogView.findViewById(R.id.tvItemData);
        tvItemData.setText(dialogConfig.getSubtitle());

        tvNegative.setOnClickListener(v -> alertDialog.dismiss());

        tvPositive.setOnClickListener(v -> {
            dialogConfig.getClickListener().onClick(alertDialog, DialogInterface.BUTTON_POSITIVE);
            alertDialog.dismiss();
        });

        alertDialog.setCancelable(false);

        alertDialog.show();
    }

    public enum DialogMode {
        SYNC, EXPORT, DELETE
    }
}