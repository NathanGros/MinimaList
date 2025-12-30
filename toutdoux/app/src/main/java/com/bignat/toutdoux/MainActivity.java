package com.bignat.toutdoux;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TodoAdapter adapter;
    List<TodoItem> todoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);

        AppDatabase db = AppDatabase.getDatabase(this);
        TodoDao todoDao = db.todoDao();
        todoList = todoDao.getAllTodos();

        adapter = new TodoAdapter(todoList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(position -> showRemoveTodoDialog(position, todoDao));

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> showAddTodoDialog(todoDao));
    }

    private void showAddTodoDialog(TodoDao todoDao) {
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
                TodoItem newItem = new TodoItem(title);
                long id = todoDao.insert(newItem);             // save to database
                newItem.id = (int) id;
                todoList.add(newItem);               // update list
                adapter.notifyItemInserted(todoList.size() - 1);
                recyclerView.scrollToPosition(todoList.size() - 1);
            }
        });

        // Cancel button
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showRemoveTodoDialog(int position, TodoDao todoDao) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Remove " + todoList.get(position).getTitle() + " ?");
        builder.setMessage("Are you sure you want to remove this todo?");

        builder.setPositiveButton("Remove", (dialog, which) -> {
            todoDao.delete(todoList.get(position));  // remove from DB
            Log.d("DELETE", "Deleting from DB: " + todoList.get(position).getTitle());
            todoList.remove(position);               // remove from list
            adapter.notifyItemRemoved(position);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}
