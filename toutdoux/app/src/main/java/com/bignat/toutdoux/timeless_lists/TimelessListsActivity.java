package com.bignat.toutdoux.timeless_lists;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.bignat.toutdoux.AppDatabase;
import com.bignat.toutdoux.R;
import com.bignat.toutdoux.timeless_lists.timeless_list.EditTimelessListBottomSheet;
import com.bignat.toutdoux.timeless_lists.timeless_list.TimelessList;
import com.bignat.toutdoux.timeless_lists.timeless_list.TimelessListActivity;
import com.bignat.toutdoux.timeless_lists.timeless_list.TimelessListDao;
import com.bignat.toutdoux.timeless_lists.timeless_list.TimelessListTouchHelper;
import com.bignat.toutdoux.timeless_lists.timeless_list.timeless_item.TimelessItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Activity used to manage all timeless lists
 */
public class TimelessListsActivity extends AppCompatActivity {
    private RecyclerView recyclerView; // List view
    private TimelessListsAdapter adapter; // View adapter
    private List<TimelessList> items; // Timeless lists

    private TimelessListsDao timelessListsDao;
    private TimelessListDao timelessListDao;

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
    protected void onCreate(Bundle savedInstanceState) {
        // Create
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeless_lists);

        // Read database
        AppDatabase db = AppDatabase.getDatabase(this);
        timelessListsDao = db.timelessListsDao();
        timelessListDao = db.timelessListDao();
        items = timelessListsDao.getAll();
        adapter = new TimelessListsAdapter(items, timelessListsDao);

        // RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Open timeless list
        adapter.setOnListClickListener(this::openTimelessList);

        // Open timeless list settings
        adapter.setOnSettingsClickListener(this::openTimelessListSettings);

        // Add timeless list button
        FloatingActionButton addItemButton = findViewById(R.id.addItemButton);
        addItemButton.setOnClickListener(v -> showAddTimelessListDialog(timelessListsDao));

        // Toggle edit mode button
        FloatingActionButton editModeButton = findViewById(R.id.editModeButton);
        editModeButton.setOnClickListener(v -> setEditMode(!adapter.isEditMode(), editModeButton, addItemButton));

        // Drag items
        ItemTouchHelper.Callback callback = new TimelessListTouchHelper(adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        adapter.setItemTouchHelper(itemTouchHelper);
    }

    @Override
    protected void onResume() {
        super.onResume();
        FloatingActionButton editModeButton = findViewById(R.id.editModeButton);
        FloatingActionButton addItemButton = findViewById(R.id.addItemButton);
        setEditMode(false, editModeButton, addItemButton);
    }

    /**
     * Opens the selected {@link TimelessList} activity
     * @param timelessList
     */
    private void openTimelessList(TimelessList timelessList) {
        Intent intent = new Intent(this, TimelessListActivity.class);
        intent.putExtra("LIST_ID", timelessList.id);
        intent.putExtra("LIST_NAME", timelessList.getTimelessListTitle());
        startActivity(intent);
    }

    /**
     * Builds and shows the "remove list" dialog
     * @param position
     */
    private void openTimelessListSettings(int position) {
        EditTimelessListBottomSheet sheet = EditTimelessListBottomSheet.newInstance(position, this);
        sheet.show(getSupportFragmentManager(), "edit_todo");
    }

    /**
     * Builds and shows the "add list" dialog
     * @param timelessListsDao
     */
    private void showAddTimelessListDialog(TimelessListsDao timelessListsDao) {
        AlertDialog dialog = new AlertDialog.Builder(this).create();

        LayoutInflater inflater = LayoutInflater.from(this);
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

    private void setEditMode(boolean newEditMode, FloatingActionButton editModeButton, FloatingActionButton addItemButton) {
        adapter.setEditMode(newEditMode);

        addItemButton.setVisibility(adapter.isEditMode() ? View.VISIBLE : View.GONE);

        editModeButton.setImageResource(
                adapter.isEditMode() ? R.drawable.outline_edit_off_24 : R.drawable.outline_edit_24
        );
        editModeButton.setBackgroundTintList(
            ColorStateList.valueOf(
                ContextCompat.getColor(this, adapter.isEditMode() ? R.color.fab_on_background : R.color.fab_off_background)
            )
        );
        editModeButton.setImageTintList(
            ColorStateList.valueOf(
                ContextCompat.getColor(this, adapter.isEditMode() ? R.color.fab_on_foreground : R.color.fab_off_foreground)
            )
        );
    }
}
