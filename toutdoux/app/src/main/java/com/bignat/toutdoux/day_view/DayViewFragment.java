package com.bignat.toutdoux.day_view;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bignat.toutdoux.AppDatabase;
import com.bignat.toutdoux.R;
import com.bignat.toutdoux.day_view.day_sections.DayRow;
import com.bignat.toutdoux.day_view.day_sections.daily_section.DailyItem;
import com.bignat.toutdoux.day_view.day_sections.daily_section.DailyItemDao;
import com.bignat.toutdoux.timeless_lists.timeless_list.TimelessListAdapter;
import com.bignat.toutdoux.timeless_lists.timeless_list.timeless_item.TimelessItemTouchHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * Fragment used inside the day view
 */
public class DayViewFragment extends Fragment {
    private RecyclerView recyclerView;
    private DayViewAdapter adapter;
    private List<DayRow> rows;
    private List<DailyItem> dailyItems;
    private DailyItemDao dailyItemDao;
    private FloatingActionButton addItemButton;
    private FloatingActionButton editModeButton;

    public DayViewFragment() {}

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Create
        View view = inflater.inflate(
                R.layout.fragment_day_view,
                container,
                false
        );

        // Read database
        AppDatabase db = AppDatabase.getDatabase(requireContext());
        dailyItemDao = db.dailyItemDao();
        dailyItems = dailyItemDao.getAll();
        adapter = new DayViewAdapter(dailyItems);

        // RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Open item settings
//        adapter.setOnItemSettingsClickListener(this::openTimelessItemSettings);

        // Add item button
        addItemButton = view.findViewById(R.id.addItemButton);
//        addItemButton.setOnClickListener(v -> showAddTimelessItemDialog(timelessListDao));

        // Toggle edit mode button
        editModeButton = view.findViewById(R.id.editModeButton);
        editModeButton.setOnClickListener(v -> setEditMode(!adapter.isEditMode()));

        // Drag items
//        ItemTouchHelper.Callback callback = new TimelessItemTouchHelper(adapter);
//        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
//        itemTouchHelper.attachToRecyclerView(recyclerView);
//        adapter.setItemTouchHelper(itemTouchHelper);

        setEditMode(false);

        return view;
    }

    private void setEditMode(boolean newEditMode) {
        adapter.setEditMode(newEditMode);

        addItemButton.setVisibility(adapter.isEditMode() ? View.VISIBLE : View.GONE);

        editModeButton.setImageResource(
                adapter.isEditMode() ? R.drawable.outline_edit_off_24 : R.drawable.outline_edit_24
        );
        editModeButton.setBackgroundTintList(
                ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), adapter.isEditMode() ? R.color.fab_on_background : R.color.fab_off_background)
                )
        );
        editModeButton.setImageTintList(
                ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), adapter.isEditMode() ? R.color.fab_on_foreground : R.color.fab_off_foreground)
                )
        );
    }
}
