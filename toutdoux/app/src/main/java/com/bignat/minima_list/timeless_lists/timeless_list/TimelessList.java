package com.bignat.toutdoux.timeless_lists.timeless_list;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class TimelessList {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String timelessListTitle;
    public int orderIndex;

    public TimelessList(String timelessListTitle) {
        this.timelessListTitle = timelessListTitle;
    }

    public String getTimelessListTitle() {
        return timelessListTitle;
    }

    public void setTimelessListTitle(String timelessListTitle) {
        this.timelessListTitle = timelessListTitle;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }
}
