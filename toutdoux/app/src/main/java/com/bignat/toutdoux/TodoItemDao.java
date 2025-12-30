package com.bignat.toutdoux;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface TodoItemDao {

    @Query("SELECT * FROM TodoItem WHERE listId = :listId ORDER BY orderIndex ASC")
    List<TodoItem> getByList(int listId);

    @Insert
    long insert(TodoItem todo);

    @Update
    void update(TodoItem todo);

    @Delete
    void delete(TodoItem todo);
}
