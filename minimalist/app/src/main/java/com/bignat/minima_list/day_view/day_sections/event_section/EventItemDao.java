package com.bignat.minima_list.day_view.day_sections.event_section;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.Date;
import java.util.List;

@Dao
public interface EventItemDao {
    @Query("SELECT * FROM EventItem ORDER BY startDate ASC")
    List<EventItem> getAll();

    @Query("SELECT * FROM EventItem WHERE startDate < :dayEnd AND endDate >= :dayStart ORDER BY startDate ASC")
    List<EventItem> getAllByDay(Date dayStart, Date dayEnd);

    @Insert
    long insert(EventItem eventItem);

    @Update
    void update(EventItem eventItem);

    @Delete
    void delete(EventItem eventItem);
}
