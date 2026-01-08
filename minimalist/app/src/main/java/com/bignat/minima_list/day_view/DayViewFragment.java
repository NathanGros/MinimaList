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
import com.bignat.minima_list.day_view.day_sections.event_section.EditEventItemBottomSheet;
import com.bignat.minima_list.day_view.day_sections.event_section.EventItem;
import com.bignat.minima_list.day_view.day_sections.event_section.EventItemDao;
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
    private List<TimedItem> postponedItems;
    private List<EventItem> eventItems;
    private DailyItemDao dailyItemDao;
    private TimedItemDao timedItemDao;
    private EventItemDao eventItemDao;
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

    public List<TimedItem> getPostponedItems() {
        return postponedItems;
    }

    public List<EventItem> getEventItems() {
        return eventItems;
    }

    public DailyItemDao getDailyItemDao() {
        return dailyItemDao;
    }

    public TimedItemDao getTimedItemDao() {
        return timedItemDao;
    }

    public EventItemDao getEventItemDao() {
        return eventItemDao;
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
                        refreshEventItems();
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
            refreshEventItems();
        });
        dateNextButton.setOnClickListener(v -> {
            viewDate.add(Calendar.DAY_OF_MONTH, 1);
            refreshDate();
            dateTitle.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(viewDate.getTime()));
            refreshTimedItems();
            refreshEventItems();
        });

        // Read database
        AppDatabase db = AppDatabase.getDatabase(requireContext());
        dailyItemDao = db.dailyItemDao();
        dailyItems = dailyItemDao.getAll();
        timedItemDao = db.timedItemDao();
        timedItems = timedItemDao.getAllByDay(viewDateStart.getTime(), viewDateEnd.getTime());
        postponedItems = timedItemDao.getAllPostponed(viewDateStart.getTime());
        eventItemDao = db.eventItemDao();
        eventItems = eventItemDao.getAllByDay(viewDateStart.getTime(), viewDateEnd.getTime());
        addRepeatingEvents();
        adapter = new DayViewAdapter(dailyItems, timedItems, postponedItems, eventItems);

        // Uncheck daily items on day change
        for (DailyItem item: dailyItems) {
            if (!item.isCompleted())
                continue;
            Calendar lastCompleted = Calendar.getInstance();
            lastCompleted.setTime(item.getLastTimeCompleted());
            if (
                    lastCompleted.get(Calendar.YEAR) == (viewDate.get(Calendar.YEAR))
                    && lastCompleted.get(Calendar.MONTH) == (viewDate.get(Calendar.MONTH))
                    && lastCompleted.get(Calendar.DAY_OF_MONTH) == (viewDate.get(Calendar.DAY_OF_MONTH))
            )
                continue;
            item.setCompleted(false);
        }

        // RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Open daily item settings
        adapter.setOnDailyItemSettingsClickListener(this::openDailyItemSettings);

        // Open timed item settings
        adapter.setOnTimedItemSettingsClickListener(this::openTimedItemSettings);

        // Open event item settings
        adapter.setOnEventItemSettingsClickListener(this::openEventItemSettings);

        // Refresh timed items hook
        adapter.setRefreshTimedItemsHook(this::refreshTimedItems);

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
        // Daily item
        View layoutDaily = dialogView.findViewById(R.id.layoutDaily);
        // Timed item
        View layoutTimed = dialogView.findViewById(R.id.layoutTimed);
        EditText deadlineDateInput = dialogView.findViewById(R.id.deadlineDate);
        EditText deadlineTimeInput = dialogView.findViewById(R.id.deadlineTime);
        // Event item
        View layoutEvent = dialogView.findViewById(R.id.layoutEvent);
        EditText eventStartDateInput = dialogView.findViewById(R.id.eventStartDate);
        EditText eventStartTimeInput = dialogView.findViewById(R.id.eventStartTime);
        EditText eventEndDateInput = dialogView.findViewById(R.id.eventEndDate);
        EditText eventEndTimeInput = dialogView.findViewById(R.id.eventEndTime);
        Button positiveButton = dialogView.findViewById(R.id.buttonPositive);
        Button negativeButton = dialogView.findViewById(R.id.buttonNegative);

        // Set values
        dialogTitle.setText("Add new item");
        String[] itemTypes = {"Daily", "Timed", "Event"};
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                itemTypes
        );
        dropdown.setAdapter(arrayAdapter);
        dropdown.setText(itemTypes[0], false);
        input.setHint("Item name");
        layoutDaily.setVisibility(View.VISIBLE);
        layoutTimed.setVisibility(View.GONE);
        layoutEvent.setVisibility(View.GONE);
        positiveButton.setText("Add");
        negativeButton.setText("Cancel");
        dialog.setView(dialogView);
        // Set date input values
        Calendar selectedDeadlineDateTime = Calendar.getInstance();
        Calendar selectedEventStartDateTime = Calendar.getInstance();
        Calendar selectedEventEndDateTime = Calendar.getInstance();
        selectedDeadlineDateTime.setTime(viewDate.getTime());
        selectedEventStartDateTime.setTime(viewDate.getTime());
        selectedEventEndDateTime.setTime(viewDate.getTime());
        selectedEventEndDateTime.add(Calendar.HOUR_OF_DAY, 1);
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
        DateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        deadlineDateInput.setText(dateFormat.format(selectedDeadlineDateTime.getTime()));
        deadlineTimeInput.setText(timeFormat.format(selectedDeadlineDateTime.getTime()));
        eventStartDateInput.setText(dateFormat.format(selectedEventStartDateTime.getTime()));
        eventStartTimeInput.setText(timeFormat.format(selectedEventStartDateTime.getTime()));
        eventEndDateInput.setText(dateFormat.format(selectedEventEndDateTime.getTime()));
        eventEndTimeInput.setText(timeFormat.format(selectedEventEndDateTime.getTime()));

        // Dropdown menu
        dropdown.setOnItemClickListener((parent, view, position, id) -> {
            switch (position) {
                case 0: // Daily
                    layoutDaily.setVisibility(View.VISIBLE);
                    layoutTimed.setVisibility(View.GONE);
                    layoutEvent.setVisibility(View.GONE);
                    break;
                case 1: // Timed
                    layoutDaily.setVisibility(View.GONE);
                    layoutTimed.setVisibility(View.VISIBLE);
                    layoutEvent.setVisibility(View.GONE);
                    break;
                case 2: // Event
                    layoutDaily.setVisibility(View.GONE);
                    layoutTimed.setVisibility(View.GONE);
                    layoutEvent.setVisibility(View.VISIBLE);
                    break;
            }
        });

        // Date inputs click listeners
        deadlineDateInput.setOnClickListener(v -> {
            DatePickerDialog datePicker = new DatePickerDialog(
                    requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        selectedDeadlineDateTime.set(Calendar.YEAR, year);
                        selectedDeadlineDateTime.set(Calendar.MONTH, month);
                        selectedDeadlineDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        Date date = selectedDeadlineDateTime.getTime();
                        deadlineDateInput.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(date));
                    },
                    selectedDeadlineDateTime.get(Calendar.YEAR),
                    selectedDeadlineDateTime.get(Calendar.MONTH),
                    selectedDeadlineDateTime.get(Calendar.DAY_OF_MONTH)
            );
            datePicker.show();
        });

        deadlineTimeInput.setOnClickListener(v -> {
            TimePickerDialog timePicker = new TimePickerDialog(
                    requireContext(),
                    (view, hourOfDay, minute) -> {
                        selectedDeadlineDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedDeadlineDateTime.set(Calendar.MINUTE, minute);
                        selectedDeadlineDateTime.set(Calendar.SECOND, 0);

                        String time = String.format("%02d:%02d", hourOfDay, minute);
                        deadlineTimeInput.setText(time);
                    },
                    selectedDeadlineDateTime.get(Calendar.HOUR_OF_DAY),
                    selectedDeadlineDateTime.get(Calendar.MINUTE),
                    true
            );
            timePicker.show();
        });

        eventStartDateInput.setOnClickListener(v -> {
            DatePickerDialog datePicker = new DatePickerDialog(
                    requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        selectedEventStartDateTime.set(Calendar.YEAR, year);
                        selectedEventStartDateTime.set(Calendar.MONTH, month);
                        selectedEventStartDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        Date date = selectedEventStartDateTime.getTime();
                        eventStartDateInput.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(date));
                    },
                    selectedEventStartDateTime.get(Calendar.YEAR),
                    selectedEventStartDateTime.get(Calendar.MONTH),
                    selectedEventStartDateTime.get(Calendar.DAY_OF_MONTH)
            );
            datePicker.show();
        });

        eventStartTimeInput.setOnClickListener(v -> {
            TimePickerDialog timePicker = new TimePickerDialog(
                    requireContext(),
                    (view, hourOfDay, minute) -> {
                        selectedEventStartDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedEventStartDateTime.set(Calendar.MINUTE, minute);
                        selectedEventStartDateTime.set(Calendar.SECOND, 0);

                        String time = String.format("%02d:%02d", hourOfDay, minute);
                        eventStartTimeInput.setText(time);
                    },
                    selectedEventStartDateTime.get(Calendar.HOUR_OF_DAY),
                    selectedEventStartDateTime.get(Calendar.MINUTE),
                    true
            );
            timePicker.show();
        });

        eventEndDateInput.setOnClickListener(v -> {
            DatePickerDialog datePicker = new DatePickerDialog(
                    requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        selectedEventEndDateTime.set(Calendar.YEAR, year);
                        selectedEventEndDateTime.set(Calendar.MONTH, month);
                        selectedEventEndDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        Date date = selectedEventEndDateTime.getTime();
                        eventEndDateInput.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(date));
                    },
                    selectedEventEndDateTime.get(Calendar.YEAR),
                    selectedEventEndDateTime.get(Calendar.MONTH),
                    selectedEventEndDateTime.get(Calendar.DAY_OF_MONTH)
            );
            datePicker.show();
        });

        eventEndTimeInput.setOnClickListener(v -> {
            TimePickerDialog timePicker = new TimePickerDialog(
                    requireContext(),
                    (view, hourOfDay, minute) -> {
                        selectedEventEndDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedEventEndDateTime.set(Calendar.MINUTE, minute);
                        selectedEventEndDateTime.set(Calendar.SECOND, 0);

                        String time = String.format("%02d:%02d", hourOfDay, minute);
                        eventEndTimeInput.setText(time);
                    },
                    selectedEventEndDateTime.get(Calendar.HOUR_OF_DAY),
                    selectedEventEndDateTime.get(Calendar.MINUTE),
                    true
            );
            timePicker.show();
        });

        // Add button
        positiveButton.setOnClickListener(v -> {
            String title = input.getText().toString().trim();
            if (title.isEmpty()) {
                input.setError("Required");
            } else {
                String selectedItemType = dropdown.getText().toString();
                switch (selectedItemType) {
                    case "Daily": {
                        // Add to list
                        DailyItem newItem = new DailyItem(title);
                        newItem.setOrderIndex(dailyItems.size());

                        long newId = dailyItemDao.insert(newItem);
                        newItem.id = (int) newId;

                        dailyItems.add(newItem);
                        adapter.notifyItemInserted(dailyItems.size());
                        recyclerView.scrollToPosition(dailyItems.size());

                        dialog.dismiss();
                        break;
                    }
                    case "Timed": {
                        // Add to list
                        Date deadline = selectedDeadlineDateTime.getTime();
                        TimedItem newItem = new TimedItem(title, deadline);

                        long newId = timedItemDao.insert(newItem);
                        newItem.id = (int) newId;

                        refreshTimedItems();
                        int index = timedItems.indexOf(newItem);
                        if (index != -1)
                            recyclerView.scrollToPosition(1 + dailyItems.size() + index);

                        dialog.dismiss();
                        break;
                    }
                    case "Event": {
                        // Add to list
                        Date eventStart = selectedEventStartDateTime.getTime();
                        Date eventEnd = selectedEventEndDateTime.getTime();
                        if (eventEnd.compareTo(eventStart) < 0) {
                            eventEndDateInput.setError("End is before start");
                            eventEndTimeInput.setError("End is before start");
                        } else {
                            EventItem newItem = new EventItem(title, eventStart, eventEnd);

                            long newId = eventItemDao.insert(newItem);
                            newItem.id = (int) newId;

                            refreshEventItems();
                            int index = eventItems.indexOf(newItem);
                            if (index != -1)
                                recyclerView.scrollToPosition(1 + dailyItems.size() + 1 + timedItems.size() + postponedItems.size() + index);

                            dialog.dismiss();
                        }
                        break;
                    }
                }
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

    /**
     * Opens the event item settings bottomSheet
     */
    private void openEventItemSettings(EventItem eventItem) {
        EditEventItemBottomSheet sheet = new EditEventItemBottomSheet(eventItem, this);
        sheet.show(getParentFragmentManager(), "edit_event");
    }

    public void addRepeatingEvents() {
        List<EventItem> repeatingEvents = eventItemDao.getRepeatCandidates(viewDateEnd.getTime());
        for (EventItem repeatingEvent: repeatingEvents) {
            Calendar repeatingEventDayStart = Calendar.getInstance();
            repeatingEventDayStart.setTime(repeatingEvent.startDate);
            repeatingEventDayStart.set(Calendar.HOUR_OF_DAY, 0);
            repeatingEventDayStart.set(Calendar.MINUTE, 0);
            repeatingEventDayStart.set(Calendar.SECOND, 0);
            repeatingEventDayStart.set(Calendar.MILLISECOND, 0);
            long diffDays = (viewDateStart.getTimeInMillis() - repeatingEventDayStart.getTime().getTime()) / 86400000;
            if (diffDays % repeatingEvent.nbDaysRepeat == 0)
                eventItems.add(repeatingEvent);
        }
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
        timedItems.addAll(timedItemDao.getAllByDay(viewDateStart.getTime(), viewDateEnd.getTime()));
        postponedItems.clear();
        postponedItems.addAll(timedItemDao.getAllPostponed(viewDateStart.getTime()));
        adapter.notifyDataSetChanged();
    }

    public void refreshEventItems() {
        eventItems.clear();
        eventItems.addAll(eventItemDao.getAllByDay(viewDateStart.getTime(), viewDateEnd.getTime()));
        addRepeatingEvents();
        adapter.notifyDataSetChanged();
    }

    private void setEditMode(boolean newEditMode) {
        adapter.setEditMode(newEditMode);

        addItemButton.setVisibility(adapter.isEditMode() ? View.VISIBLE : View.GONE);

        editModeButton.setImageResource(
                adapter.isEditMode() ? R.drawable.ic_edit_off : R.drawable.ic_edit
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
