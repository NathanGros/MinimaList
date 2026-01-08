package com.bignat.minima_list.day_view.day_sections.event_section;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
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

import com.bignat.minima_list.R;
import com.bignat.minima_list.day_view.DayViewFragment;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditEventItemBottomSheet extends BottomSheetDialogFragment {
    private EventItem eventItem;
    private DayViewFragment parentFragment;

    public EditEventItemBottomSheet(
            EventItem eventItem,
            DayViewFragment context
    ) {
        this.eventItem = eventItem;
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

        return inflater.inflate(R.layout.bottomsheet_edit_event_item, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Get view items
        super.onViewCreated(view, savedInstanceState);
        EditText titleEdit = view.findViewById(R.id.titleEdit);
        CheckBox optionalCheck = view.findViewById(R.id.optionalCheck);
        EditText startDateInput = view.findViewById(R.id.eventStartDate);
        EditText startTimeInput = view.findViewById(R.id.eventStartTime);
        EditText endDateInput = view.findViewById(R.id.eventEndDate);
        EditText endTimeInput = view.findViewById(R.id.eventEndTime);
        CheckBox repeatCheck = view.findViewById(R.id.repeatCheck);
        EditText repeatNbDaysInput = view.findViewById(R.id.nbDaysRepeat);
        Button deleteButton = view.findViewById(R.id.deleteButton);
        Button cancelButton = view.findViewById(R.id.cancelButton);
        Button saveButton = view.findViewById(R.id.saveButton);

        // Get item dates
        Calendar selectedStartDateTime = Calendar.getInstance();
        Calendar selectedEndDateTime = Calendar.getInstance();
        Date startDate = eventItem.getStartDate();
        Date endDate = eventItem.getEndDate();
        selectedStartDateTime.setTime(startDate);
        selectedEndDateTime.setTime(endDate);

        // Set default values
        titleEdit.setText(eventItem.getTitle());
        optionalCheck.setChecked(eventItem.isOptional());
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
        DateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        startDateInput.setText(dateFormat.format(startDate));
        startTimeInput.setText(timeFormat.format(startDate));
        endDateInput.setText(dateFormat.format(endDate));
        endTimeInput.setText(timeFormat.format(endDate));
        repeatCheck.setChecked(eventItem.isRepeat());
        repeatNbDaysInput.setText(String.valueOf(eventItem.getNbDaysRepeat()));
        if (eventItem.isRepeat()) {
            repeatNbDaysInput.setVisibility(View.VISIBLE);
        } else {
            repeatNbDaysInput.setVisibility(View.GONE);
        }

        repeatCheck.setOnClickListener(v -> {
            if (repeatCheck.isChecked()) {
                repeatNbDaysInput.setVisibility(View.VISIBLE);
            } else {
                repeatNbDaysInput.setVisibility(View.GONE);
            }
        });

        // Edit fields
        startDateInput.setOnClickListener(v -> {
            DatePickerDialog datePicker = new DatePickerDialog(
                    requireContext(),
                    (dateView, year, month, dayOfMonth) -> {
                        selectedStartDateTime.set(Calendar.YEAR, year);
                        selectedStartDateTime.set(Calendar.MONTH, month);
                        selectedStartDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        Date newDate = selectedStartDateTime.getTime();
                        startDateInput.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(newDate));
                    },
                    selectedStartDateTime.get(Calendar.YEAR),
                    selectedStartDateTime.get(Calendar.MONTH),
                    selectedStartDateTime.get(Calendar.DAY_OF_MONTH)
            );
            datePicker.show();
        });

        startTimeInput.setOnClickListener(v -> {
            TimePickerDialog timePicker = new TimePickerDialog(
                    requireContext(),
                    (timeView, hourOfDay, minute) -> {
                        selectedStartDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedStartDateTime.set(Calendar.MINUTE, minute);
                        selectedStartDateTime.set(Calendar.SECOND, 0);

                        String time = String.format("%02d:%02d", hourOfDay, minute);
                        startTimeInput.setText(time);
                    },
                    selectedStartDateTime.get(Calendar.HOUR_OF_DAY),
                    selectedStartDateTime.get(Calendar.MINUTE),
                    true
            );
            timePicker.show();
        });

        endDateInput.setOnClickListener(v -> {
            DatePickerDialog datePicker = new DatePickerDialog(
                    requireContext(),
                    (dateView, year, month, dayOfMonth) -> {
                        selectedEndDateTime.set(Calendar.YEAR, year);
                        selectedEndDateTime.set(Calendar.MONTH, month);
                        selectedEndDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        Date newDate = selectedEndDateTime.getTime();
                        endDateInput.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(newDate));
                    },
                    selectedEndDateTime.get(Calendar.YEAR),
                    selectedEndDateTime.get(Calendar.MONTH),
                    selectedEndDateTime.get(Calendar.DAY_OF_MONTH)
            );
            datePicker.show();
        });

        endTimeInput.setOnClickListener(v -> {
            TimePickerDialog timePicker = new TimePickerDialog(
                    requireContext(),
                    (timeView, hourOfDay, minute) -> {
                        selectedEndDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedEndDateTime.set(Calendar.MINUTE, minute);
                        selectedEndDateTime.set(Calendar.SECOND, 0);

                        String time = String.format("%02d:%02d", hourOfDay, minute);
                        endTimeInput.setText(time);
                    },
                    selectedEndDateTime.get(Calendar.HOUR_OF_DAY),
                    selectedEndDateTime.get(Calendar.MINUTE),
                    true
            );
            timePicker.show();
        });

        saveButton.setOnClickListener(v -> {
            Date eventStart = selectedStartDateTime.getTime();
            Date eventEnd = selectedEndDateTime.getTime();
            if (eventEnd.compareTo(eventStart) < 0) {
                endDateInput.setError("End is before start");
                endTimeInput.setError("End is before start");
            } else if (repeatNbDaysInput.getText().toString().isEmpty()) {
                repeatNbDaysInput.setError("Required");
            } else {
                eventItem.setTitle(titleEdit.getText().toString().trim());
                eventItem.setOptional(optionalCheck.isChecked());
                eventItem.setStartDate(eventStart);
                eventItem.setEndDate(eventEnd);
                eventItem.setRepeat(repeatCheck.isChecked());
                int nbDaysRepeatInt;
                try {
                    nbDaysRepeatInt = Integer.parseInt(repeatNbDaysInput.getText().toString());
                    eventItem.setNbDaysRepeat(nbDaysRepeatInt);
                }
                catch (NumberFormatException e) {
                    repeatNbDaysInput.setError("Error");
                }
                parentFragment.getEventItemDao().update(eventItem);
                parentFragment.refreshEventItems();
                dismiss();
            }
        });

        cancelButton.setOnClickListener(v -> {
            dismiss();
        });

        deleteButton.setOnClickListener(v -> {
            showDeleteConfirmation(eventItem);
        });
    }

    private void showDeleteConfirmation(EventItem item) {
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
        dialogDescription.setText("Are you sure you want to delete this event?");
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

    private void deleteItem(EventItem item) {
        // Delete from database
        parentFragment.getEventItemDao().delete(item);

        // Delete from Event list
        int index = parentFragment.getEventItems().indexOf(item);
        if (index != -1) {
            parentFragment.getEventItems().remove(index);
            parentFragment.getAdapter().notifyItemRemoved(
            index + 1
                    + parentFragment.getDailyItems().size() + 1
                    + parentFragment.getTimedItems().size() + parentFragment.getPostponedItems().size() + 1
            );
        }
    }
}
