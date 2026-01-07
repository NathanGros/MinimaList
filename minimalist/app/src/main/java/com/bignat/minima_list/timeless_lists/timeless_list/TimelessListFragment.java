package com.bignat.minima_list.timeless_lists.timeless_list;

import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.bignat.minima_list.AppDatabase;
import com.bignat.minima_list.R;
import com.bignat.minima_list.timeless_lists.timeless_list.timeless_item.EditTimelessItemBottomSheet;
import com.bignat.minima_list.timeless_lists.timeless_list.timeless_item.TimelessItem;
import com.bignat.minima_list.timeless_lists.timeless_list.timeless_item.TimelessItemTouchHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Activity used inside a timeless list
 */
public class TimelessListFragment extends Fragment {
    private final int id; // List id
    private String title; // List name
    private RecyclerView recyclerView; // List view
    private TimelessListAdapter adapter; // View adapter
    private List<TimelessItem> items; // timeless items
    private TimelessListDao timelessListDao;
    private FloatingActionButton addItemButton;
    private FloatingActionButton editModeButton;

    public TimelessListFragment(int listId, String listName) {
        this.id = listId;
        this.title = listName;
    }

    public TimelessListDao getTimelessListDao() {
        return timelessListDao;
    }

    public List<TimelessItem> getItems() {
        return items;
    }

    public TimelessListAdapter getAdapter() {
        return adapter;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Create
        View view = inflater.inflate(
                R.layout.fragment_timeless_list,
                container,
                false
        );

        // Set list name
        TextView listTitleView = view.findViewById(R.id.listTitle);
        if (title != null) {
            listTitleView.setText(title);
        }

        // Read database
        AppDatabase db = AppDatabase.getDatabase(requireContext());
        timelessListDao = db.timelessListDao();
        items = timelessListDao.getByList(id);
        adapter = new TimelessListAdapter(items, timelessListDao);

        // RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Open item settings
        adapter.setOnItemSettingsClickListener(this::openTimelessItemSettings);

        // Add item button
        addItemButton = view.findViewById(R.id.addItemButton);
        addItemButton.setOnClickListener(v -> showAddTimelessItemDialog(timelessListDao));

        // Toggle edit mode button
        editModeButton = view.findViewById(R.id.editModeButton);
        editModeButton.setOnClickListener(v -> setEditMode(!adapter.isEditMode()));

        // Drag items
        ItemTouchHelper.Callback callback = new TimelessItemTouchHelper(adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        adapter.setItemTouchHelper(itemTouchHelper);

        setEditMode(false);

        return view;
    }

    /**
     * Builds and shows the "add item" dialog
     */
    private void showAddTimelessItemDialog(TimelessListDao timelessListDao) {
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
                TimelessItem newItem = new TimelessItem(title, id);
                newItem.setOrderIndex(items.size());

                long newId = timelessListDao.insert(newItem);
                newItem.id = (int) newId;

                items.add(newItem);
                adapter.notifyItemInserted(items.size() - 1);
                recyclerView.scrollToPosition(items.size() - 1);

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
     * Opens the item settings bottomSheet
     */
    private void openTimelessItemSettings(TimelessItem timelessItem) {
        EditTimelessItemBottomSheet sheet = new EditTimelessItemBottomSheet(timelessItem, this);
        sheet.show(getParentFragmentManager(), "edit_todo");
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
