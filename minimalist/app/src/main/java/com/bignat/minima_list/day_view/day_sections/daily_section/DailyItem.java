package com.bignat.minima_list.day_view.day_sections.daily_section;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class DailyItem {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String title;
    public boolean completed;
    public boolean optional;
    public int orderIndex;

    public DailyItem(String title) {
        this.title = title;
        this.completed = false;
        this.optional = false;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }
}
