package com.bignat.toutdoux.timeless_lists.timeless_list.timeless_item;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class TimelessItem {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int listId;

    public String title;
    public boolean completed;

    public int orderIndex;

    public TimelessItem(String title, int listId) {
        this.title = title;
        this.listId = listId;
        this.completed = false;
    }

    public String getTitle() {
        return title;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }
}
