package com.bignat.minima_list.timeless_lists.timeless_list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.bignat.minima_list.R;
import com.bignat.minima_list.timeless_lists.TimelessListsFragment;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class EditTimelessListBottomSheet extends BottomSheetDialogFragment {
    private TimelessList timelessList;
    private TimelessListsFragment parentFragment;

    public EditTimelessListBottomSheet(
        TimelessList timelessList,
        TimelessListsFragment parentFragment
    ) {
        this.timelessList = timelessList;
        this.parentFragment = parentFragment;
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
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.bottomsheet_edit_timeless_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EditText titleEdit = view.findViewById(R.id.titleEdit);
        Button deleteButton = view.findViewById(R.id.deleteButton);
        Button cancelButton = view.findViewById(R.id.cancelButton);
        Button saveButton = view.findViewById(R.id.saveButton);

        titleEdit.setText(timelessList.getTimelessListTitle());

        saveButton.setOnClickListener(v -> {
            timelessList.setTimelessListTitle(titleEdit.getText().toString().trim());
            parentFragment.getTimelessListsDao().update(timelessList);
            int index = parentFragment.getItems().indexOf(timelessList);
            parentFragment.getAdapter().notifyItemChanged(index);
            dismiss();
        });

        cancelButton.setOnClickListener(v -> {
            dismiss();
        });

        deleteButton.setOnClickListener(v -> {
            showDeleteConfirmation(timelessList);
        });
    }

    private void showDeleteConfirmation(TimelessList item) {
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
        dialogTitle.setText("Delete " + item.getTimelessListTitle() + " ?");
        dialogDescription.setText("Are you sure you want to delete this list and its contents?");
        input.setVisibility(View.GONE);
        positiveButton.setText("Delete");
        negativeButton.setText("Cancel");
        dialog.setView(dialogView);

        // Remove button
        positiveButton.setOnClickListener(v -> {
            deleteList(item);
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

    private void deleteList(TimelessList timelessList) {
        // Delete from database
        parentFragment.getTimelessListDao().deleteByList(timelessList.id);
        parentFragment.getTimelessListsDao().delete(timelessList);

        // Delete from list
        int index = parentFragment.getItems().indexOf(timelessList);
        if (index != -1) {
            parentFragment.getItems().remove(index);
            parentFragment.getAdapter().notifyItemRemoved(index);
        }
    }
}
