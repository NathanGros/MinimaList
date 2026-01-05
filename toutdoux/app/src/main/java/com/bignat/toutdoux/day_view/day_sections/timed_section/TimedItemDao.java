package com.bignat.toutdoux.day_view.day_sections.timed_section;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TimedItemDao {
    @Query("SELECT * FROM TimedItem ORDER BY orderIndex ASC")
    List<TimedItem> getAll();

    @Insert
    long insert(TimedItem timedItem);

    @Update
    void update(TimedItem timedItem);

    @Delete
    void delete(TimedItem timedItem);
}
