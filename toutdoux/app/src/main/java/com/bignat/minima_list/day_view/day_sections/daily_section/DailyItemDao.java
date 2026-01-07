package com.bignat.toutdoux.day_view.day_sections.daily_section;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DailyItemDao {

    @Query("SELECT * FROM DailyItem ORDER BY orderIndex ASC")
    List<DailyItem> getAll();

    @Insert
    long insert(DailyItem dailyItem);

    @Update
    void update(DailyItem dailyItem);

    @Delete
    void delete(DailyItem dailyItem);
}
