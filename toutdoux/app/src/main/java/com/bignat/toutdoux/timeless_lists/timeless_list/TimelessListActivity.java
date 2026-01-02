package com.bignat.toutdoux.timeless_lists.timeless_list;

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
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.bignat.toutdoux.AppDatabase;
import com.bignat.toutdoux.R;
import com.bignat.toutdoux.timeless_lists.timeless_list.timeless_item.EditTimelessItemBottomSheet;
import com.bignat.toutdoux.timeless_lists.timeless_list.timeless_item.TimelessItem;
import com.bignat.toutdoux.timeless_lists.timeless_list.timeless_item.TimelessItemTouchHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Activity used inside a timeless list
 */
public class TimelessListActivity extends AppCompatActivity {
    private int id; // List id
    private String title; // List name
    private RecyclerView recyclerView; // List view
    private TimelessListAdapter adapter; // View adapter
    private List<TimelessItem> items; // timeless items

    private TimelessListDao timelessListDao;

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
    protected void onCreate(Bundle savedInstanceState) {
        // Create
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeless_list);

        // Retrieve intent
        Intent intent = getIntent();
        title = intent.getStringExtra("LIST_NAME");
        id = intent.getIntExtra("LIST_ID", -1);

        // Set list name
        TextView listTitleView = findViewById(R.id.listTitle);
        if (title != null) {
            listTitleView.setText(title);
        }

        // Read database
        AppDatabase db = AppDatabase.getDatabase(this);
        timelessListDao = db.timelessListDao();
        items = timelessListDao.getByList(id);
        adapter = new TimelessListAdapter(items, timelessListDao);

        // RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Open item settings
        adapter.setOnItemSettingsClickListener(this::openTimelessItemSettings);

        // Add item button
        FloatingActionButton addItemButton = findViewById(R.id.addItemButton);
        addItemButton.setOnClickListener(v -> showAddTimelessItemDialog(timelessListDao));

        // Toggle edit mode button
        FloatingActionButton editModeButton = findViewById(R.id.editModeButton);
        editModeButton.setOnClickListener(v -> toggleEditMode(editModeButton, addItemButton));

        // Drag items
        ItemTouchHelper.Callback callback = new TimelessItemTouchHelper(adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        adapter.setItemTouchHelper(itemTouchHelper);
    }

    /**
     * Builds and shows the "add item" dialog
     * @param timelessListDao
     */
    private void showAddTimelessItemDialog(TimelessListDao timelessListDao) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add new item");

        // Input field
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Add button
        builder.setPositiveButton("Add", (dialog, which) -> {
            String title = input.getText().toString().trim();
            if (!title.isEmpty()) {
                // Add to list
                TimelessItem newItem = new TimelessItem(title, id);
                newItem.setOrderIndex(items.size());
                long id = timelessListDao.insert(newItem); // Save to database
                newItem.id = (int) id;
                items.add(newItem); // Update list
                adapter.notifyItemInserted(items.size() - 1);
                recyclerView.scrollToPosition(items.size() - 1);
            }
        });

        // Cancel button
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    /**
     * Builds and shows the "remove item" dialog
     * @param position
     */
    private void openTimelessItemSettings(int position) {
        EditTimelessItemBottomSheet sheet = EditTimelessItemBottomSheet.newInstance(position, this);
        sheet.show(getSupportFragmentManager(), "edit_todo");
    }

    private void toggleEditMode(FloatingActionButton editModeButton, FloatingActionButton addItemButton) {
        adapter.setEditMode(!adapter.isEditMode());

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
