package com.bignat.toutdoux.timeless_lists;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.bignat.toutdoux.timeless_lists.timeless_list.TimelessList;

import java.util.List;

/**
 * Data Access Object for the {@link TimelessList}s table
 */
@Dao
public interface TimelessListsDao {
    /**
     * @return all {@link TimelessList}s
     */
    @Query("SELECT * FROM TimelessList ORDER BY orderIndex ASC")
    List<TimelessList> getAll();

    /**
     * Adds a {@link TimelessList} to the database
     * @param list
     * @return the id of the new {@link TimelessList}
     */
    @Insert
    long insert(TimelessList list);

    @Update
    void update(TimelessList list);

    /**
     * Deletes the {@link TimelessList} from the database
     * @param list
     */
    @Delete
    void delete(TimelessList list);
}
