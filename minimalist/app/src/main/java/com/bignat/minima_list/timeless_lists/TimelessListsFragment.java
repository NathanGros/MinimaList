package com.bignat.minima_list.timeless_lists;

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
import com.bignat.minima_list.timeless_lists.timeless_list.EditTimelessListBottomSheet;
import com.bignat.minima_list.timeless_lists.timeless_list.TimelessList;
import com.bignat.minima_list.timeless_lists.timeless_list.TimelessListFragment;
import com.bignat.minima_list.timeless_lists.timeless_list.TimelessListDao;
import com.bignat.minima_list.timeless_lists.timeless_list.TimelessListTouchHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Fragment used to manage all timeless lists
 */
public class TimelessListsFragment extends Fragment {
    private RecyclerView recyclerView; // List view
    private TimelessListsAdapter adapter; // View adapter
    private List<TimelessList> items; // Timeless lists
    private TimelessListsDao timelessListsDao;
    private TimelessListDao timelessListDao;
    private FloatingActionButton addItemButton;
    private FloatingActionButton editModeButton;

    public TimelessListsFragment() {}

    public TimelessListsDao getTimelessListsDao() {
        return timelessListsDao;
    }

    public TimelessListDao getTimelessListDao() {
        return timelessListDao;
    }

    public TimelessListsAdapter getAdapter() {
        return adapter;
    }

    public List<TimelessList> getItems() {
        return items;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Create
        View view = inflater.inflate(
                R.layout.fragment_timeless_lists,
                container,
                false
        );

        // Read database
        AppDatabase db = AppDatabase.getDatabase(requireContext());
        timelessListsDao = db.timelessListsDao();
        timelessListDao = db.timelessListDao();
        items = timelessListsDao.getAll();
        adapter = new TimelessListsAdapter(items, timelessListsDao);

        // RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Open timeless list
        adapter.setOnListClickListener(this::openTimelessList);

        // Open timeless list settings
        adapter.setOnSettingsClickListener(this::openTimelessListSettings);

        // Add timeless list button
        addItemButton = view.findViewById(R.id.addItemButton);
        addItemButton.setOnClickListener(v -> showAddTimelessListDialog(timelessListsDao));

        // Toggle edit mode button
        editModeButton = view.findViewById(R.id.editModeButton);
        editModeButton.setOnClickListener(v -> setEditMode(!adapter.isEditMode()));

        // Drag items
        ItemTouchHelper.Callback callback = new TimelessListTouchHelper(adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        adapter.setItemTouchHelper(itemTouchHelper);

        setEditMode(false);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setEditMode(false);
    }

    /**
     * Opens the selected {@link TimelessList} activity
     * @param timelessList
     */
    private void openTimelessList(TimelessList timelessList) {
        TimelessListFragment fragment = new TimelessListFragment(timelessList.id, timelessList.getTimelessListTitle());

        getParentFragmentManager()
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit();
    }

    /**
     * Builds and shows the "remove list" dialog
     */
    private void openTimelessListSettings(TimelessList timelessList) {
        EditTimelessListBottomSheet sheet = new EditTimelessListBottomSheet(timelessList, this);
        sheet.show(getParentFragmentManager(), "edit_list");
    }

    /**
     * Builds and shows the "add list" dialog
     * @param timelessListsDao
     */
    private void showAddTimelessListDialog(TimelessListsDao timelessListsDao) {
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
        dialogTitle.setText("Add new list");
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
                TimelessList newList = new TimelessList(title);
                newList.setOrderIndex(items.size());

                long newId = timelessListsDao.insert(newList);
                newList.id = (int) newId;

                items.add(newList);
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
