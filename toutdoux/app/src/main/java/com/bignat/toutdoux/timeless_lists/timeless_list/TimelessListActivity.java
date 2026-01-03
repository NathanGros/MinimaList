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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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
