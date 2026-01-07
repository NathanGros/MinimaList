package com.bignat.minima_list.timeless_lists.timeless_list;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.bignat.minima_list.timeless_lists.timeless_list.timeless_item.TimelessItem;

import java.util.List;

@Dao
public interface TimelessListDao {

    @Query("SELECT * FROM TimelessItem WHERE listId = :listId ORDER BY orderIndex ASC")
    List<TimelessItem> getByList(int listId);

    @Insert
    long insert(TimelessItem timelessItem);

    @Update
    void update(TimelessItem timelessItem);

    @Delete
    void delete(TimelessItem timelessItem);

    @Query("DELETE FROM timelessitem WHERE listId = :listId")
    void deleteByList(int listId);
}
