package com.bignat.toutdoux.day_view;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bignat.toutdoux.AppDatabase;
import com.bignat.toutdoux.R;
import com.bignat.toutdoux.day_view.day_sections.DayRow;
import com.bignat.toutdoux.day_view.day_sections.DaySectionTitle;
import com.bignat.toutdoux.day_view.day_sections.daily_section.DailyItem;
import com.bignat.toutdoux.day_view.day_sections.daily_section.DailyItemDao;
import com.bignat.toutdoux.day_view.day_sections.daily_section.EditDailyItemBottomSheet;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment used inside the day view
 */
public class DayViewFragment extends Fragment {
    private RecyclerView recyclerView;
    private DayViewAdapter adapter;
    private List<DayRow> rows;
    private List<DailyItem> dailyItems;
    private DailyItemDao dailyItemDao;
    private FloatingActionButton addItemButton;
    private FloatingActionButton editModeButton;

    public DayViewFragment() {}

    public DayViewAdapter getAdapter() {
        return adapter;
    }

    public List<DayRow> getRows() {
        return rows;
    }

    public List<DailyItem> getDailyItems() {
        return dailyItems;
    }

    public DailyItemDao getDailyItemDao() {
        return dailyItemDao;
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

        // Read database
        AppDatabase db = AppDatabase.getDatabase(requireContext());
        dailyItemDao = db.dailyItemDao();
        dailyItems = dailyItemDao.getAll();
        rows = new ArrayList<>();
        rows.add(new DaySectionTitle("Daily"));
        rows.addAll(dailyItems);
        adapter = new DayViewAdapter(rows);

        // RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Open item settings
        adapter.setOnDailyItemSettingsClickListener(this::openDailyItemSettings);

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
        View dialogView = inflater.inflate(R.layout.alert_dialog, null);

        // Get elements
        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        TextView dialogDescription = dialogView.findViewById(R.id.dialogDescription);
        EditText input = dialogView.findViewById(R.id.etItemTitle);
        Button positiveButton = dialogView.findViewById(R.id.buttonPositive);
        Button negativeButton = dialogView.findViewById(R.id.buttonNegative);

        // Set values
        dialogTitle.setText("Add new item");
        dialogDescription.setVisibility(View.GONE);
        input.setHint("Item name");
        positiveButton.setText("Add");
        negativeButton.setText("Cancel");
        dialog.setView(dialogView);

        // Add button
        positiveButton.setOnClickListener(v -> {
            String title = input.getText().toString().trim();
            if (!title.isEmpty()) {
                // Add to list
                DailyItem newItem = new DailyItem(title);
                newItem.setOrderIndex(rows.size());

                long newId = dailyItemDao.insert(newItem);
                newItem.id = (int) newId;

                dailyItems.add(newItem);
                rows.add(newItem);
                adapter.notifyItemInserted(rows.size() - 1);
                recyclerView.scrollToPosition(rows.size() - 1);

                dialog.dismiss();
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
    private void openDailyItemSettings(int position) {
        EditDailyItemBottomSheet sheet = new EditDailyItemBottomSheet(position, this);
        sheet.show(getParentFragmentManager(), "edit_daily");
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
