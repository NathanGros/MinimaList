package com.bignat.toutdoux;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class TodoList {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String todoListTitle;
    public int orderIndex;

    public TodoList(String todoListTitle) {
        this.todoListTitle = todoListTitle;
    }

    public String getTodoListTitle() {
        return todoListTitle;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }
}
