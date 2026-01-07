package com.bignat.toutdoux.day_view.day_sections.timed_section;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditTimedItemBottomSheet extends BottomSheetDialogFragment {
    private TimedItem timedItem;
    private DayViewFragment parentFragment;

    public EditTimedItemBottomSheet(
            TimedItem timedItem,
            DayViewFragment context
    ) {
        this.timedItem = timedItem;
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

        return inflater.inflate(R.layout.bottomsheet_edit_timed_item, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EditText titleEdit = view.findViewById(R.id.titleEdit);
        EditText dateInput = view.findViewById(R.id.deadlineDate);
        EditText timeInput = view.findViewById(R.id.deadlineTime);
        CheckBox completedCheck = view.findViewById(R.id.completedCheck);
        CheckBox optionalCheck = view.findViewById(R.id.optionalCheck);
        Button deleteButton = view.findViewById(R.id.deleteButton);
        Button cancelButton = view.findViewById(R.id.cancelButton);
        Button saveButton = view.findViewById(R.id.saveButton);

        Calendar selectedDateTime = Calendar.getInstance();
        Date date = timedItem.getDeadline();
        selectedDateTime.setTime(date);
        DateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        titleEdit.setText(timedItem.getTitle());
        dateInput.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(date));
        timeInput.setText(timeFormat.format(date));
        completedCheck.setChecked(timedItem.isCompleted());
        optionalCheck.setChecked(timedItem.isOptional());

        dateInput.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();

            DatePickerDialog datePicker = new DatePickerDialog(
                    requireContext(),
                    (dateView, year, month, dayOfMonth) -> {
                        selectedDateTime.set(Calendar.YEAR, year);
                        selectedDateTime.set(Calendar.MONTH, month);
                        selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        Date newDate = selectedDateTime.getTime();
                        dateInput.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(newDate));
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            );

            datePicker.show();
        });

        timeInput.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();

            TimePickerDialog timePicker = new TimePickerDialog(
                    requireContext(),
                    (timeView, hourOfDay, minute) -> {
                        selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedDateTime.set(Calendar.MINUTE, minute);
                        selectedDateTime.set(Calendar.SECOND, 0);

                        String time = String.format("%02d:%02d", hourOfDay, minute);
                        timeInput.setText(time);
                    },
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    true
            );

            timePicker.show();
        });

        saveButton.setOnClickListener(v -> {
            timedItem.setTitle(titleEdit.getText().toString().trim());
            timedItem.setDeadline(selectedDateTime.getTime());
            timedItem.setCompleted(completedCheck.isChecked());
            timedItem.setOptional(optionalCheck.isChecked());
            parentFragment.getTimedItemDao().update(timedItem);
            parentFragment.getTimedItems().clear();
            parentFragment.getTimedItems().addAll(parentFragment.getTimedItemDao().getAll());
            parentFragment.getAdapter().notifyDataSetChanged();
            dismiss();
        });

        cancelButton.setOnClickListener(v -> {
            dismiss();
        });

        deleteButton.setOnClickListener(v -> {
            showDeleteConfirmation(timedItem);
        });
    }

    private void showDeleteConfirmation(TimedItem item) {
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

    private void deleteItem(TimedItem item) {
        // Delete from database
        parentFragment.getTimedItemDao().delete(item);

        // Delete from list
        int index = parentFragment.getTimedItems().indexOf(item);
        if (index != -1) {
            parentFragment.getTimedItems().remove(index);
            parentFragment.getAdapter().notifyItemRemoved(index + 1 + parentFragment.getDailyItems().size() + 1);
        }
    }
}
