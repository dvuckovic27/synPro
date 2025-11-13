package com.metalac.scanner.app.helpers;

import android.content.Context;
import android.graphics.Insets;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.metalac.scanner.app.R;
import com.metalac.scanner.app.utils.Utils;
import com.metalac.scanner.app.view.inventorylist.interfaces.AddInventoryListClickListener;

public class BottomDialogHelper {
    /**
     * Creates and returns a BottomSheetDialog that allows the user to input a new inventory list name.
     * <p>
     * The dialog includes an EditText field and an "Add" button. The "Add" button is only enabled
     * when the input is not empty or blank. Upon clicking "Add", the dialog is dismissed and the
     * provided {@link AddInventoryListClickListener} is triggered with the entered name.
     * </p>
     *
     * @param context       the Context used to create the dialog
     * @param clickListener the callback invoked with the entered list name when "Add" is clicked
     * @return a configured and ready-to-show {@link BottomSheetDialog}
     */

    public static BottomSheetDialog createAddListDialog(Context context, AddInventoryListClickListener clickListener) {
        BottomSheetDialog dialog = new BottomSheetDialog(context, R.style.BottomSheetDialog);
        dialog.setContentView(R.layout.bottom_dialog_add_list);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        adjustSoftInputMode(dialog.getWindow());

        FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
        }

        adjustSoftInputMode(dialog.getWindow());

        EditText editText = dialog.findViewById(R.id.etListName);
        Button addButton = dialog.findViewById(R.id.btAdd);

        if (editText != null && addButton != null) {
            addButton.setEnabled(false);

            editText.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    String listName = s.toString();
                    addButton.setEnabled(!TextUtils.isEmpty(listName.trim()));
                }
            });

            addButton.setOnClickListener(v -> {
                dialog.dismiss();
                String listName = Utils.getStringOrNull(editText);
                if (clickListener != null) {
                    clickListener.addInventoryListClick(listName);
                    editText.setText("");
                }
            });
        }

        return dialog;
    }

    /**
     * Adjusts the window to handle the keyboard properly.
     * On Android 11 and above, it disables default fitting and adds padding
     * for the keyboard using window insets.
     *
     * @param window the window to adjust
     */
    private static void adjustSoftInputMode(Window window) {
        if (window == null) return;
        View rootView = window.getDecorView().findViewById(android.R.id.content);

        if (rootView != null) {
            rootView.setOnApplyWindowInsetsListener((v, insets) -> {
                Insets imeInsets = insets.getInsets(WindowInsets.Type.ime());

                v.setPadding(
                        v.getPaddingLeft(),
                        v.getPaddingTop(),
                        v.getPaddingRight(),
                        imeInsets.bottom
                );

                return insets;
            });

            rootView.requestApplyInsets();
        }
    }
}
