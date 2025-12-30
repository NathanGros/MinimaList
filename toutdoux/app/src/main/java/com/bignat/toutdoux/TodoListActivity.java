package com.bignat.toutdoux;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import android.text.InputType;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class TodoListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TodoAdapter adapter;
    int listId;
    List<TodoItem> todoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_list);

        // Set list title
        TextView listTitle = findViewById(R.id.listTitle);
        String listName = getIntent().getStringExtra("LIST_NAME");
        if (listName != null) {
            listTitle.setText(listName);
        }

        recyclerView = findViewById(R.id.recyclerView);

        AppDatabase db = AppDatabase.getDatabase(this);
        TodoItemDao todoItemDao = db.todoDao();
        listId = getIntent().getIntExtra("LIST_ID", -1);
        todoList = todoItemDao.getByList(listId);

        adapter = new TodoAdapter(todoList, todoItemDao);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(position -> showRemoveTodoDialog(position, todoItemDao));

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> showAddTodoDialog(todoItemDao));

        ItemTouchHelper.Callback callback = new TodoItemTouchHelper(adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void showAddTodoDialog(TodoItemDao todoDao) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Todo");

        // Input field
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Add button
        builder.setPositiveButton("Add", (dialog, which) -> {
            String title = input.getText().toString().trim();
            if (!title.isEmpty()) {
                // Add to list
                TodoItem newItem = new TodoItem(title, listId);
                newItem.setOrderIndex(todoList.size());
                long id = todoDao.insert(newItem); // Save to database
                newItem.id = (int) id;
                todoList.add(newItem); // Update list
                adapter.notifyItemInserted(todoList.size() - 1);
                recyclerView.scrollToPosition(todoList.size() - 1);
            }
        });

        // Cancel button
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showRemoveTodoDialog(int position, TodoItemDao todoDao) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Remove " + todoList.get(position).getTitle() + " ?");
        builder.setMessage("Are you sure you want to remove this todo?");

        builder.setPositiveButton("Remove", (dialog, which) -> {
            todoDao.delete(todoList.get(position));  // remove from DB
            todoList.remove(position);               // remove from list
            adapter.notifyItemRemoved(position);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}
