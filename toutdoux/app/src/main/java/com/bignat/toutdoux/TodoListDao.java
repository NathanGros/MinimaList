package com.bignat.toutdoux;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TodoListDao {

    @Query("SELECT * FROM TodoList")
    List<TodoList> getAll();

    @Insert
    long insert(TodoList list);

    @Update
    void update(TodoList list);

    @Delete
    void delete(TodoList list);
}
