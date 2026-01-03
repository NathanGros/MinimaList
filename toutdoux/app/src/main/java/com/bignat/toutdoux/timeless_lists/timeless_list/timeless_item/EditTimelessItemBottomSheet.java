package com.bignat.toutdoux.timeless_lists.timeless_list.timeless_item;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.bignat.toutdoux.R;
import com.bignat.toutdoux.timeless_lists.timeless_list.TimelessListActivity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class EditTimelessItemBottomSheet extends BottomSheetDialogFragment {
    private int itemPosition;
    private TimelessListActivity context;

    public static EditTimelessItemBottomSheet newInstance(
        int itemPosition,
        TimelessListActivity context
    ) {
        EditTimelessItemBottomSheet sheet = new EditTimelessItemBottomSheet();
        sheet.itemPosition = itemPosition;
        sheet.context = context;
        return sheet;
    }

    @Override
    public void onStart() {
        super.onStart();
        View sheet = getDialog().findViewById(
                com.google.android.material.R.id.design_bottom_sheet
        );
        sheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.bottomsheet_edit_timeless_item, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EditText titleEdit = view.findViewById(R.id.titleEdit);
        CheckBox completedCheck = view.findViewById(R.id.completedCheck);
        CheckBox optionalCheck = view.findViewById(R.id.optionalCheck);
        Button deleteButton = view.findViewById(R.id.deleteButton);
        Button cancelButton = view.findViewById(R.id.cancelButton);
        Button saveButton = view.findViewById(R.id.saveButton);

        TimelessItem item = context.getItems().get(itemPosition);

        titleEdit.setText(item.getTitle());
        completedCheck.setChecked(item.isCompleted());
        optionalCheck.setChecked(item.isOptional());

        saveButton.setOnClickListener(v -> {
            item.setTitle(titleEdit.getText().toString().trim());
            item.setCompleted(completedCheck.isChecked());
            item.setOptional(optionalCheck.isChecked());
            context.getTimelessListDao().update(item);
            context.getAdapter().notifyItemChanged(itemPosition);
            dismiss();
        });

        cancelButton.setOnClickListener(v -> {
            dismiss();
        });

        deleteButton.setOnClickListener(v -> {
            showDeleteConfirmation(item);
        });
    }

    private void showDeleteConfirmation(TimelessItem item) {
        AlertDialog dialog = new AlertDialog.Builder(context).create();

        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.alert_dialog, null);

        // Get elements
        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        TextView dialogDescription = dialogView.findViewById(R.id.dialogDescription);
        EditText input = dialogView.findViewById(R.id.etItemTitle);
        Button positiveButton = dialogView.findViewById(R.id.buttonPositive);
        Button negativeButton = dialogView.findViewById(R.id.buttonNegative);

        // Set values
        dialogTitle.setText("Remove " + item.getTitle() + " ?");
        dialogDescription.setText("Are you sure you want to remove this item?");
        input.setVisibility(View.GONE);
        positiveButton.setText("Remove");
        negativeButton.setText("Cancel");
        dialog.setView(dialogView);

        // Remove button
        positiveButton.setOnClickListener(v -> {
            deleteItem(item);
            dialog.dismiss();
            dismiss();
        });

        // Cancel button
        negativeButton.setOnClickListener(v -> dialog.cancel());

        // Show
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    private void deleteItem(TimelessItem item) {
        // Delete from database
        context.getTimelessListDao().delete(item);

        // Delete from list
        int index = context.getItems().indexOf(item);
        if (index != -1) {
            context.getItems().remove(index);
            context.getAdapter().notifyItemRemoved(index);
        }
    }
}
