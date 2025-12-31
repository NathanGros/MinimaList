package com.bignat.toutdoux.timeless_lists;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import android.text.InputType;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;

import com.bignat.toutdoux.AppDatabase;
import com.bignat.toutdoux.R;
import com.bignat.toutdoux.timeless_lists.timeless_list.TimelessList;
import com.bignat.toutdoux.timeless_lists.timeless_list.TimelessListActivity;
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
        items = timelessListsDao.getAll();
        adapter = new TimelessListsAdapter(items, timelessListsDao);

        // RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Open timeless list
        adapter.setOnListClickListener(timelessList -> openTimelessList(timelessList));

        // Add item button
        FloatingActionButton addButton = findViewById(R.id.fabAdd);
        addButton.setOnClickListener(v -> showAddTimelessListDialog(timelessListsDao));

        // Drag items
        ItemTouchHelper.Callback callback = new TimelessListTouchHelper(adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
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
}
