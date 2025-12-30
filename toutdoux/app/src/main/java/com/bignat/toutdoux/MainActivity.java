package com.bignat.toutdoux;

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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TodoListAdapter adapter;
    List<TodoList> todoLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);

        AppDatabase db = AppDatabase.getDatabase(this);
        TodoListDao todoListDao = db.todoListDao();
        todoLists = todoListDao.getAll();

        adapter = new TodoListAdapter(todoLists, todoListDao);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.setOnListClickListener(todoList -> {
            Intent intent = new Intent(this, TodoListActivity.class);
            intent.putExtra("LIST_ID", todoList.id);
            intent.putExtra("LIST_NAME", todoList.getTodoListTitle());
            startActivity(intent);
        });

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> showAddTodoListDialog(todoListDao));

//        ItemTouchHelper.Callback callback = new TodoItemTouchHelper(adapter);
//        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
//        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void showAddTodoListDialog(TodoListDao todoListDao) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Todo List");

        // Input field
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Add button
        builder.setPositiveButton("Add", (dialog, which) -> {
            String title = input.getText().toString().trim();
            if (!title.isEmpty()) {
                // Add to list
                TodoList newList = new TodoList(title);
                newList.setOrderIndex(todoLists.size());
                long id = todoListDao.insert(newList); // Save to database
                newList.id = (int) id;
                todoLists.add(newList); // Update list
                adapter.notifyItemInserted(todoLists.size() - 1);
                recyclerView.scrollToPosition(todoLists.size() - 1);
            }
        });

        // Cancel button
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

//    private void showRemoveTodoListDialog(int position, TodoListDao todoListDao) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Remove " + todoList.get(position).getTitle() + " ?");
//        builder.setMessage("Are you sure you want to remove this todo?");
//
//        builder.setPositiveButton("Remove", (dialog, which) -> {
//            todoDao.delete(todoList.get(position));  // remove from DB
//            todoList.remove(position);               // remove from list
//            adapter.notifyItemRemoved(position);
//        });
//
//        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
//
//        builder.show();
//    }
}
