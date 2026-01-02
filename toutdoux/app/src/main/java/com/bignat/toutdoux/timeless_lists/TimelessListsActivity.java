package com.bignat.toutdoux.timeless_lists;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import com.bignat.toutdoux.AppDatabase;
import com.bignat.toutdoux.R;
import com.bignat.toutdoux.timeless_lists.timeless_list.TimelessList;
import com.bignat.toutdoux.timeless_lists.timeless_list.TimelessListActivity;
import com.bignat.toutdoux.timeless_lists.timeless_list.TimelessListDao;
import com.bignat.toutdoux.timeless_lists.timeless_list.TimelessListTouchHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Activity used to manage all timeless lists
 */
public class TimelessListsActivity extends AppCompatActivity {
    private RecyclerView recyclerView; // List view
    private TimelessListsAdapter adapter; // View adapter
    private List<TimelessList> items; // Timeless lists

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Create
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeless_lists);

        // Read database
        AppDatabase db = AppDatabase.getDatabase(this);
        TimelessListsDao timelessListsDao = db.timelessListsDao();
        TimelessListDao timelessListDao = db.timelessListDao();
        items = timelessListsDao.getAll();
        adapter = new TimelessListsAdapter(items, timelessListsDao);

        // RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Open timeless list
        adapter.setOnListClickListener(timelessList -> openTimelessList(timelessList));

        // Open timeless list settings
        adapter.setOnSettingsClickListener((list, anchor) -> openTimelessListSettings(list, anchor, timelessListsDao, timelessListDao));

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
     * @param timelessList
     * @param anchor
     */
    private void openTimelessListSettings(TimelessList timelessList, View anchor, TimelessListsDao timelessListsDao, TimelessListDao timelessListDao) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Remove " + timelessList.getTimelessListTitle() + " ?");
        builder.setMessage("Are you sure you want to remove this list?");

        // Remove button
        builder.setPositiveButton("Remove", (dialog, which) -> {
            deleteList(timelessList, timelessListsDao, timelessListDao);
        });

        // Cancel button
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void deleteList(TimelessList timelessList, TimelessListsDao timelessListsDao, TimelessListDao timelessListDao) {
        // Delete from database
        timelessListDao.deleteByList(timelessList.id);
        timelessListsDao.delete(timelessList);

        // Delete from list
        int index = items.indexOf(timelessList);
        if (index != -1) {
            items.remove(index);
            adapter.notifyItemRemoved(index);
        }
    }

    /**
     * Builds and shows the "add list" dialog
     * @param timelessListsDao
     */
    private void showAddTimelessListDialog(TimelessListsDao timelessListsDao) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add new Timeless List");

        // Input field
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Add button
        builder.setPositiveButton("Add", (dialog, which) -> {
            String title = input.getText().toString().trim();
            if (!title.isEmpty()) {
                // Add to list
                TimelessList newList = new TimelessList(title);
                newList.setOrderIndex(items.size());
                long id = timelessListsDao.insert(newList); // Save to database
                newList.id = (int) id;
                items.add(newList); // Update list
                adapter.notifyItemInserted(items.size() - 1);
                recyclerView.scrollToPosition(items.size() - 1);
            }
        });

        // Cancel button
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void setEditMode(boolean newEditMode, FloatingActionButton editModeButton, FloatingActionButton addItemButton) {
        adapter.setEditMode(newEditMode);

        addItemButton.setVisibility(adapter.isEditMode() ? View.VISIBLE : View.GONE);

        editModeButton.setImageResource(
                adapter.isEditMode() ? R.drawable.outline_edit_off_24 : R.drawable.outline_edit_24
        );
    }
}
