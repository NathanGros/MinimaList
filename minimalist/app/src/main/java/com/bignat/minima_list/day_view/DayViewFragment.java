package com.bignat.minima_list.day_view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bignat.minima_list.AppDatabase;
import com.bignat.minima_list.R;
import com.bignat.minima_list.day_view.day_sections.daily_section.DailyItem;
import com.bignat.minima_list.day_view.day_sections.daily_section.DailyItemDao;
import com.bignat.minima_list.day_view.day_sections.daily_section.EditDailyItemBottomSheet;
import com.bignat.minima_list.day_view.day_sections.timed_section.EditTimedItemBottomSheet;
import com.bignat.minima_list.day_view.day_sections.timed_section.TimedItem;
import com.bignat.minima_list.day_view.day_sections.timed_section.TimedItemDao;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Fragment used inside the day view
 */
public class DayViewFragment extends Fragment {
    private RecyclerView recyclerView;
    private Calendar viewDate;
    private Calendar viewDateStart;
    private Calendar viewDateEnd;
    private DayViewAdapter adapter;
    private List<DailyItem> dailyItems;
    private List<TimedItem> timedItems;
    private DailyItemDao dailyItemDao;
    private TimedItemDao timedItemDao;
    private FloatingActionButton addItemButton;
    private FloatingActionButton editModeButton;

    public DayViewFragment() {}

    public DayViewAdapter getAdapter() {
        return adapter;
    }

    public List<DailyItem> getDailyItems() {
        return dailyItems;
    }

    public List<TimedItem> getTimedItems() {
        return timedItems;
    }

    public DailyItemDao getDailyItemDao() {
        return dailyItemDao;
    }

    public TimedItemDao getTimedItemDao() {
        return timedItemDao;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Create
        View view = inflater.inflate(
                R.layout.fragment_day_view,
                container,
                false
        );

        // Date
        viewDate = Calendar.getInstance();

        refreshDate();

        TextView dateTitle = view.findViewById(R.id.viewDate);
        dateTitle.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(viewDate.getTime()));
        dateTitle.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            DatePickerDialog datePicker = new DatePickerDialog(
                    requireContext(),
                    (datePickerView, year, month, dayOfMonth) -> {
                        viewDate.set(Calendar.YEAR, year);
                        viewDate.set(Calendar.MONTH, month);
                        viewDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        dateTitle.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(viewDate.getTime()));

                        refreshDate();
                        refreshTimedItems();
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            );
            datePicker.show();
        });

        // Date arrows
        ImageButton datePreviousButton = view.findViewById(R.id.datePreviousButton);
        ImageButton dateNextButton = view.findViewById(R.id.dateNextButton);
        datePreviousButton.setOnClickListener(v -> {
            viewDate.add(Calendar.DAY_OF_MONTH, -1);
            refreshDate();
            dateTitle.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(viewDate.getTime()));
            refreshTimedItems();
        });
        dateNextButton.setOnClickListener(v -> {
            viewDate.add(Calendar.DAY_OF_MONTH, 1);
            refreshDate();
            dateTitle.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(viewDate.getTime()));
            refreshTimedItems();
        });

        // Read database
        AppDatabase db = AppDatabase.getDatabase(requireContext());
        dailyItemDao = db.dailyItemDao();
        dailyItems = dailyItemDao.getAll();
        timedItemDao = db.timedItemDao();
        timedItems = timedItemDao.getAllByDate(viewDateStart.getTime(), viewDateEnd.getTime());
        adapter = new DayViewAdapter(dailyItems, timedItems);

        // RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Open daily item settings
        adapter.setOnDailyItemSettingsClickListener(this::openDailyItemSettings);

        // Open timed item settings
        adapter.setOnTimedItemSettingsClickListener(this::openTimedItemSettings);

        // Add item button
        addItemButton = view.findViewById(R.id.addItemButton);
        addItemButton.setOnClickListener(v -> showAddDayItemDialog());

        // Toggle edit mode button
        editModeButton = view.findViewById(R.id.editModeButton);
        editModeButton.setOnClickListener(v -> setEditMode(!adapter.isEditMode()));

        // Drag items
//        ItemTouchHelper.Callback callback = new TimelessItemTouchHelper(adapter);
//        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
//        itemTouchHelper.attachToRecyclerView(recyclerView);
//        adapter.setItemTouchHelper(itemTouchHelper);

        setEditMode(false);

        return view;
    }

    private void showAddDayItemDialog() {
        AlertDialog dialog = new AlertDialog.Builder(requireContext()).create();

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.add_day_item_dialog, null);

        // Get elements
        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        AutoCompleteTextView dropdown = dialogView.findViewById(R.id.itemTypeDropdown);
        EditText input = dialogView.findViewById(R.id.itemTitleBox);
        View layoutDaily = dialogView.findViewById(R.id.layoutDaily);
        View layoutTimed = dialogView.findViewById(R.id.layoutTimed);
        EditText dateInput = dialogView.findViewById(R.id.deadlineDate);
        EditText timeInput = dialogView.findViewById(R.id.deadlineTime);
        Button positiveButton = dialogView.findViewById(R.id.buttonPositive);
        Button negativeButton = dialogView.findViewById(R.id.buttonNegative);

        // Set values
        dialogTitle.setText("Add new item");
        String[] itemTypes = {"Daily", "Timed"};
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                itemTypes
        );
        dropdown.setAdapter(arrayAdapter);
        dropdown.setText(itemTypes[0], false);
        input.setHint("Item name");
        layoutDaily.setVisibility(View.GONE);
        layoutTimed.setVisibility(View.GONE);
        positiveButton.setText("Add");
        negativeButton.setText("Cancel");
        dialog.setView(dialogView);

        Calendar selectedDateTime = Calendar.getInstance();
        selectedDateTime.setTime(viewDate.getTime());
        dateInput.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(viewDate.getTime()));
        DateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        timeInput.setText(timeFormat.format(viewDate.getTime()));

        // Dropdown menu
        dropdown.setOnItemClickListener((parent, view, position, id) -> {
            switch (position) {
                case 0: // Daily
                    layoutDaily.setVisibility(View.VISIBLE);
                    layoutTimed.setVisibility(View.GONE);
                    break;
                case 1: // Timed
                    layoutDaily.setVisibility(View.GONE);
                    layoutTimed.setVisibility(View.VISIBLE);
                    break;
                default:
                    layoutDaily.setVisibility(View.GONE);
                    layoutTimed.setVisibility(View.GONE);
                    break;
            }
        });

        dateInput.setOnClickListener(v -> {
            DatePickerDialog datePicker = new DatePickerDialog(
                    requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        selectedDateTime.set(Calendar.YEAR, year);
                        selectedDateTime.set(Calendar.MONTH, month);
                        selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        Date date = selectedDateTime.getTime();
                        dateInput.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(date));
                    },
                    viewDate.get(Calendar.YEAR),
                    viewDate.get(Calendar.MONTH),
                    viewDate.get(Calendar.DAY_OF_MONTH)
            );
            datePicker.show();
        });

        timeInput.setOnClickListener(v -> {
            TimePickerDialog timePicker = new TimePickerDialog(
                    requireContext(),
                    (view, hourOfDay, minute) -> {
                        selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedDateTime.set(Calendar.MINUTE, minute);
                        selectedDateTime.set(Calendar.SECOND, 0);

                        String time = String.format("%02d:%02d", hourOfDay, minute);
                        timeInput.setText(time);
                    },
                    viewDate.get(Calendar.HOUR_OF_DAY),
                    viewDate.get(Calendar.MINUTE),
                    true
            );
            timePicker.show();
        });

        // Add button
        positiveButton.setOnClickListener(v -> {
            String title = input.getText().toString().trim();
            if (!title.isEmpty()) {
                String selectedItemType = dropdown.getText().toString();
                if (selectedItemType.equals("Daily")) {
                    // Add to list
                    DailyItem newItem = new DailyItem(title);
                    newItem.setOrderIndex(dailyItems.size());

                    long newId = dailyItemDao.insert(newItem);
                    newItem.id = (int) newId;

                    dailyItems.add(newItem);
                    adapter.notifyItemInserted(dailyItems.size());
                    recyclerView.scrollToPosition(dailyItems.size());

                    dialog.dismiss();
                } else if (selectedItemType.equals("Timed")) {
                    // Add to list
                    Date deadline = selectedDateTime.getTime();
                    TimedItem newItem = new TimedItem(title, deadline);

                    long newId = timedItemDao.insert(newItem);
                    newItem.id = (int) newId;

                    refreshTimedItems();
                    int index = timedItems.indexOf(newItem);
                    if (index != -1)
                        recyclerView.scrollToPosition(1 + dailyItems.size() + index);

                    dialog.dismiss();
                }
            } else {
                input.setError("Required");
            }
        });

        // Cancel button
        negativeButton.setOnClickListener(v -> dialog.cancel());

        // Show
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    /**
     * Opens the daily item settings bottomSheet
     */
    private void openDailyItemSettings(DailyItem dailyItem) {
        EditDailyItemBottomSheet sheet = new EditDailyItemBottomSheet(dailyItem, this);
        sheet.show(getParentFragmentManager(), "edit_daily");
    }

    /**
     * Opens the timed item settings bottomSheet
     */
    private void openTimedItemSettings(TimedItem timedItem) {
        EditTimedItemBottomSheet sheet = new EditTimedItemBottomSheet(timedItem, this);
        sheet.show(getParentFragmentManager(), "edit_timed");
    }

    public void refreshDate() {
        viewDateStart = Calendar.getInstance();
        viewDateStart.setTime(viewDate.getTime());
        viewDateStart.set(Calendar.HOUR_OF_DAY, 0);
        viewDateStart.set(Calendar.MINUTE, 0);
        viewDateStart.set(Calendar.SECOND, 0);
        viewDateStart.set(Calendar.MILLISECOND, 0);
        viewDateEnd = Calendar.getInstance();
        viewDateEnd.setTime(viewDateStart.getTime());
        viewDateEnd.add(Calendar.DAY_OF_MONTH, 1);
    }

    public void refreshTimedItems() {
        timedItems.clear();
        timedItems.addAll(timedItemDao.getAllByDate(viewDateStart.getTime(), viewDateEnd.getTime()));
        adapter.notifyDataSetChanged();
    }

    private void setEditMode(boolean newEditMode) {
        adapter.setEditMode(newEditMode);

        addItemButton.setVisibility(adapter.isEditMode() ? View.VISIBLE : View.GONE);

        editModeButton.setImageResource(
                adapter.isEditMode() ? R.drawable.outline_edit_off_24 : R.drawable.outline_edit_24
        );
        editModeButton.setBackgroundTintList(
                ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), adapter.isEditMode() ? R.color.fab_on_background : R.color.fab_off_background)
                )
        );
        editModeButton.setImageTintList(
                ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), adapter.isEditMode() ? R.color.fab_on_foreground : R.color.fab_off_foreground)
                )
        );
    }
}
