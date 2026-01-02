package com.bignat.toutdoux.timeless_lists.timeless_list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.bignat.toutdoux.R;
import com.bignat.toutdoux.timeless_lists.TimelessListsActivity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class EditTimelessListBottomSheet extends BottomSheetDialogFragment {
    private int itemPosition;
    private TimelessListsActivity context;

    public static com.bignat.toutdoux.timeless_lists.timeless_list.EditTimelessListBottomSheet newInstance(
            int itemPosition,
            TimelessListsActivity context
    ) {
        com.bignat.toutdoux.timeless_lists.timeless_list.EditTimelessListBottomSheet sheet = new com.bignat.toutdoux.timeless_lists.timeless_list.EditTimelessListBottomSheet();
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

        return inflater.inflate(R.layout.bottomsheet_edit_timeless_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EditText titleEdit = view.findViewById(R.id.titleEdit);
        Button deleteButton = view.findViewById(R.id.deleteButton);
        Button saveButton = view.findViewById(R.id.saveButton);

        TimelessList item = context.getItems().get(itemPosition);

        titleEdit.setText(item.getTimelessListTitle());

        saveButton.setOnClickListener(v -> {
            item.setTimelessListTitle(titleEdit.getText().toString().trim());
            context.getTimelessListsDao().update(item);
            context.getAdapter().notifyItemChanged(itemPosition);
            dismiss();
        });

        deleteButton.setOnClickListener(v -> {
            showDeleteConfirmation(item);
        });
    }

    private void showDeleteConfirmation(TimelessList item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Remove " + item.getTimelessListTitle() + " ?");
        builder.setMessage("Are you sure you want to remove this list?");

        // Remove button
        builder.setPositiveButton("Remove", (dialog, which) -> {
            deleteList(item);
            dismiss();
        });

        // Cancel button
        builder.setNegativeButton("Cancel", (dialog, which) ->
                dialog.cancel()
        );

        builder.show();
    }

    private void deleteList(TimelessList timelessList) {
        // Delete from database
        context.getTimelessListDao().deleteByList(timelessList.id);
        context.getTimelessListsDao().delete(timelessList);

        // Delete from list
        int index = context.getItems().indexOf(timelessList);
        if (index != -1) {
            context.getItems().remove(index);
            context.getAdapter().notifyItemRemoved(index);
        }
    }
}
