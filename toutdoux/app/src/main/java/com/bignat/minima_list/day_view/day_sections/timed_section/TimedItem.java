package com.bignat.toutdoux.day_view.day_sections.timed_section;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity
public class TimedItem {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String title;
    public Date deadline;
    public boolean completed;
    public boolean optional;
    public boolean postponed;

    public TimedItem(String title, Date deadline) {
        this.title = title;
        this.deadline = deadline;
        this.completed = false;
        this.optional = false;
        this.postponed = false;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
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

    public boolean isPostponed() {
        return postponed;
    }

    public void setPostponed(boolean postponed) {
        this.postponed = postponed;
    }
}
