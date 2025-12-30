package com.bignat.toutdoux;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TodoListAdapter extends RecyclerView.Adapter<TodoListAdapter.TodoListViewHolder> {

    private List<TodoList> todoLists;
    private OnListClickListener listener;
    private TodoListDao todoListDao;

    public interface OnListClickListener {
        void onListClick(TodoList todoList);
    }

    public void setOnListClickListener(OnListClickListener listener) {
        this.listener = listener;
    }

    public TodoListAdapter(List<TodoList> todoLists, TodoListDao todoListDao) {
        this.todoLists = todoLists;
        this.todoListDao = todoListDao;
    }

    @NonNull
    @Override
    public TodoListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_todo_list, parent, false);
        return new TodoListViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoListViewHolder holder, int position) {
        TodoList todoList = todoLists.get(position);

        holder.todoListTitle.setText(todoList.getTodoListTitle());

        holder.itemView.setOnClickListener(v ->
                listener.onListClick(todoList)
        );
    }

    @Override
    public int getItemCount() {
        return todoLists.size();
    }

    public void onItemMove(int fromPosition, int toPosition) {
        TodoList movedList = todoLists.remove(fromPosition);
        todoLists.add(toPosition, movedList);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void onDragFinished() {
        for (int i = 0; i < todoLists.size(); i++) {
            TodoList todoList = todoLists.get(i);
            todoList.orderIndex = i;
            todoListDao.update(todoList);
        }
    }

    static class TodoListViewHolder extends RecyclerView.ViewHolder {
        TextView todoListTitle;

        TodoListViewHolder(View itemView, OnListClickListener listener) {
            super(itemView);
            todoListTitle = itemView.findViewById(R.id.todoListTitle);
        }
    }
}
