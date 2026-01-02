package com.bignat.toutdoux.timeless_lists.timeless_list.timeless_item;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.bignat.toutdoux.R;
import com.bignat.toutdoux.timeless_lists.timeless_list.TimelessListActivity;
import com.bignat.toutdoux.timeless_lists.timeless_list.TimelessListAdapter;
import com.bignat.toutdoux.timeless_lists.timeless_list.TimelessListDao;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

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

        deleteButton.setOnClickListener(v -> {
            showDeleteConfirmation(item);
        });
    }

    private void showDeleteConfirmation(TimelessItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Remove " + context.getItems().get(itemPosition).getTitle() + " ?");
        builder.setMessage("Are you sure you want to remove this item?");

        // Remove button
        builder.setPositiveButton("Remove", (dialog, which) -> {
            context.getTimelessListDao().delete(item);
            context.getItems().remove(itemPosition);
            context.getAdapter().notifyItemRemoved(itemPosition);
            dismiss();
        });

        // Cancel button
        builder.setNegativeButton("Cancel", (dialog, which) ->
                dialog.cancel()
        );

        builder.show();
    }
}
