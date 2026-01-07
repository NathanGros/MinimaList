package com.bignat.minima_list.day_view.day_sections.timed_section;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.Date;
import java.util.List;

@Dao
public interface TimedItemDao {
    @Query("SELECT * FROM TimedItem ORDER BY deadline ASC")
    List<TimedItem> getAll();

    @Query("SELECT * FROM TimedItem WHERE deadline >= :start AND deadline < :end ORDER BY deadline ASC")
    List<TimedItem> getAllByDate(Date start, Date end);


    @Insert
    long insert(TimedItem timedItem);

    @Update
    void update(TimedItem timedItem);

    @Delete
    void delete(TimedItem timedItem);
}
