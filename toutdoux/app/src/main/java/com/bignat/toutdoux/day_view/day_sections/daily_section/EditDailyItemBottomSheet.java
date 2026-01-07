package com.bignat.toutdoux.day_view.day_sections.daily_section;

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
import com.bignat.toutdoux.day_view.DayViewFragment;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

public class EditDailyItemBottomSheet extends BottomSheetDialogFragment {
    private int itemPosition;
    private DayViewFragment parentFragment;

    public EditDailyItemBottomSheet(
            int itemPosition,
            DayViewFragment context
    ) {
        this.itemPosition = itemPosition;
        this.parentFragment = context;
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

        return inflater.inflate(R.layout.bottomsheet_edit_daily_item, container, false);
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

        cancelButton.setOnClickListener(v -> {
            dismiss();
        });

        List<DailyItem> dailyItems = parentFragment.getDailyItems();
        if (itemPosition < 1 || itemPosition > dailyItems.size())
            return;
        DailyItem item = dailyItems.get(itemPosition - 1);

        titleEdit.setText(item.getTitle());
        completedCheck.setChecked(item.isCompleted());
        optionalCheck.setChecked(item.isOptional());

        saveButton.setOnClickListener(v -> {
            item.setTitle(titleEdit.getText().toString().trim());
            item.setCompleted(completedCheck.isChecked());
            item.setOptional(optionalCheck.isChecked());
            parentFragment.getDailyItemDao().update(item);
            parentFragment.getAdapter().notifyItemChanged(itemPosition + 1);
            dismiss();
        });

        deleteButton.setOnClickListener(v -> {
            showDeleteConfirmation(item);
        });
    }

    private void showDeleteConfirmation(DailyItem item) {
        AlertDialog dialog = new AlertDialog.Builder(requireContext()).create();

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.alert_dialog, null);

        // Get elements
        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        TextView dialogDescription = dialogView.findViewById(R.id.dialogDescription);
        EditText input = dialogView.findViewById(R.id.etItemTitle);
        Button positiveButton = dialogView.findViewById(R.id.buttonPositive);
        Button negativeButton = dialogView.findViewById(R.id.buttonNegative);

        // Set values
        dialogTitle.setText("Delete " + item.getTitle() + " ?");
        dialogDescription.setText("Are you sure you want to delete this item?");
        input.setVisibility(View.GONE);
        positiveButton.setText("Delete");
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

    private void deleteItem(DailyItem item) {
        // Delete from database
        parentFragment.getDailyItemDao().delete(item);

        // Delete from list
        int index = parentFragment.getDailyItems().indexOf(item);
        if (index != -1) {
            parentFragment.getDailyItems().remove(index);
            parentFragment.getAdapter().notifyItemRemoved(index + 1);
        }
    }
}
