package com.bignat.toutdoux;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import android.text.InputType;
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

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> showAddTodoDialog());

        recyclerView = findViewById(R.id.recyclerView);

        todoList = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            todoList.add(new TodoItem("Item " + i));
        }

        adapter = new TodoAdapter(todoList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(position -> showRemoveTodoDialog(position));
    }

    private void showAddTodoDialog() {
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
                todoList.add(newItem);
                adapter.notifyItemInserted(todoList.size() - 1);
                recyclerView.scrollToPosition(todoList.size() - 1);
            }
        });

        // Cancel button
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showRemoveTodoDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Remove " + todoList.get(position).getTitle() + " ?");
        builder.setMessage("Are you sure you want to remove this todo?");

        builder.setPositiveButton("Remove", (dialog, which) -> {
            todoList.remove(position);
            adapter.notifyItemRemoved(position);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}
