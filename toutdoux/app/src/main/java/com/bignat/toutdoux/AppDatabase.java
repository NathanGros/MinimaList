package com.bignat.toutdoux;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import android.content.Context;

import com.bignat.toutdoux.day_view.day_sections.daily_section.DailyItem;
import com.bignat.toutdoux.day_view.day_sections.daily_section.DailyItemDao;
import com.bignat.toutdoux.day_view.day_sections.timed_section.DateConverter;
import com.bignat.toutdoux.day_view.day_sections.timed_section.TimedItem;
import com.bignat.toutdoux.day_view.day_sections.timed_section.TimedItemDao;
import com.bignat.toutdoux.timeless_lists.TimelessListsDao;
import com.bignat.toutdoux.timeless_lists.timeless_list.TimelessList;
import com.bignat.toutdoux.timeless_lists.timeless_list.TimelessListDao;
import com.bignat.toutdoux.timeless_lists.timeless_list.timeless_item.TimelessItem;

@Database(entities = {TimelessItem.class, TimelessList.class, DailyItem.class, TimedItem.class}, version = 4)
@TypeConverters({DateConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract TimelessListDao timelessListDao();
    public abstract TimelessListsDao timelessListsDao();
    public abstract DailyItemDao dailyItemDao();
    public abstract TimedItemDao timedItemDao();

    private static AppDatabase INSTANCE;

    public static AppDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                AppDatabase.class, "todo_db")
                .allowMainThreadQueries() // only for simple apps, remove later
                .fallbackToDestructiveMigration()
                .build();
        }
        return INSTANCE;
    }
}
