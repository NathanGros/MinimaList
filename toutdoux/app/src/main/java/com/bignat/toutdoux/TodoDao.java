package com.bignat.toutdoux;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface TodoDao {

    @Insert
    long insert(TodoItem todo);

    @Update
    void update(TodoItem todo);

    @Delete
    void delete(TodoItem todo);

    @Query("SELECT * FROM todos ORDER BY orderIndex ASC")
    List<TodoItem> getAllTodos();
}
