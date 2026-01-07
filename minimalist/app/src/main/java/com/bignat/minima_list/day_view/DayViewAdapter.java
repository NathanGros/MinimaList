package com.bignat.minima_list.day_view;

import android.graphics.Typeface;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bignat.minima_list.AppDatabase;
import com.bignat.minima_list.R;
import com.bignat.minima_list.day_view.day_sections.DaySectionTitle;
import com.bignat.minima_list.day_view.day_sections.daily_section.DailyItem;
import com.bignat.minima_list.day_view.day_sections.timed_section.TimedItem;

import java.util.List;

public class DayViewAdapter extends RecyclerView.Adapter<DayViewAdapter.DayRowViewHolder> {
    DaySectionTitle dailySectionTitle;
    DaySectionTitle timedSectionTitle;
    private List<DailyItem> dailyItems;
    private List<TimedItem> timedItems;
    private OnDailyItemSettingsClickListener dailyItemSettingsClickListener;
    private OnTimedItemSettingsClickListener timedItemSettingsClickListener;
    private boolean isEditMode;

    public DayViewAdapter(
            List<DailyItem> dailyItems,
            List<TimedItem> timedItems
    ) {
        dailySectionTitle = new DaySectionTitle("Daily");
        timedSectionTitle = new DaySectionTitle("To do");
        this.dailyItems = dailyItems;
        this.timedItems = timedItems;
        this.isEditMode = false;
    }

    public void setEditMode(boolean editMode) {
        isEditMode = editMode;
        notifyDataSetChanged();
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    public interface OnDailyItemSettingsClickListener {
        void onDailyItemSettingsClick(DailyItem dailyItem);
    }

    public void setOnDailyItemSettingsClickListener(OnDailyItemSettingsClickListener dailyItemSettingsClickListener) {
        this.dailyItemSettingsClickListener = dailyItemSettingsClickListener;
    }

    public interface OnTimedItemSettingsClickListener {
        void onTimedItemSettingsClick(TimedItem timedItem);
    }

    public void setOnTimedItemSettingsClickListener(OnTimedItemSettingsClickListener timedItemSettingsClickListener) {
        this.timedItemSettingsClickListener = timedItemSettingsClickListener;
    }

    @NonNull
    @Override
    public DayRowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.day_row_item, parent, false);
        return new DayViewAdapter.DayRowViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayRowViewHolder holder, int position) {
        Log.d("TESTPRINT", "pos: " + position);
        if (position == 0) {
            bindViewSectionTitle(holder, dailySectionTitle);
        } else if (position < 1 + dailyItems.size()) {
            DailyItem item = dailyItems.get(position - 1);
            bindViewDailyItem(holder, item);
        } else if (position == 1 + dailyItems.size()) {
            bindViewSectionTitle(holder, timedSectionTitle);
        } else {
            TimedItem item = timedItems.get(position - 1 - dailyItems.size() - 1);
            bindViewTimedItem(holder, item);
        }
    }

    private void bindViewSectionTitle(DayRowViewHolder holder, DaySectionTitle sectionTitle) {
        holder.title.setText(sectionTitle.getSectionTitle());
        holder.title.setAlpha(1f);
        holder.title.setTextSize(22f);
        holder.title.setGravity(Gravity.CENTER);
        holder.title.setTypeface(null, Typeface.NORMAL);
        holder.dragHandle.setVisibility(View.GONE);
        holder.settingsButton.setVisibility(View.GONE);
        holder.checkBox.setVisibility(View.GONE);
    }

    private void bindViewDailyItem(DayRowViewHolder holder, DailyItem dailyItem) {
        holder.title.setText(dailyItem.getTitle());
        holder.title.setTextSize(18f);
        holder.title.setGravity(Gravity.CENTER_VERTICAL);

        if (dailyItem.isOptional()) {
            holder.title.setTypeface(null, Typeface.ITALIC);
        } else {
            holder.title.setTypeface(null, Typeface.NORMAL);
        }

        updateCheckDailyItem(dailyItem, holder);
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(dailyItem.isCompleted());
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dailyItem.setCompleted(isChecked);
            updateCheckDailyItem(dailyItem, holder);
            AppDatabase db = AppDatabase.getDatabase(holder.itemView.getContext());
            db.dailyItemDao().update(dailyItem);   // update in database
        });

        if (isEditMode) {
            holder.dragHandle.setVisibility(View.VISIBLE);
            holder.settingsButton.setVisibility(View.VISIBLE);
            holder.checkBox.setVisibility(View.GONE);
        } else {
            holder.dragHandle.setVisibility(View.GONE);
            holder.settingsButton.setVisibility(View.GONE);
            holder.checkBox.setVisibility(View.VISIBLE);
        }

//        holder.dragHandle.setOnTouchListener((v, event) -> {
//            if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                itemTouchHelper.startDrag(holder);
//            }
//            return false;
//        });

        holder.settingsButton.setOnClickListener(v -> {
            dailyItemSettingsClickListener.onDailyItemSettingsClick(dailyItem);
        });
    }

    private void bindViewTimedItem(DayRowViewHolder holder, TimedItem timedItem) {
        holder.title.setText(timedItem.getTitle());
        holder.title.setTextSize(18f);
        holder.title.setGravity(Gravity.CENTER_VERTICAL);

        if (timedItem.isOptional()) {
            holder.title.setTypeface(null, Typeface.ITALIC);
        } else {
            holder.title.setTypeface(null, Typeface.NORMAL);
        }

        updateCheckTimedItem(timedItem, holder);
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(timedItem.isCompleted());
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            timedItem.setCompleted(isChecked);
            updateCheckTimedItem(timedItem, holder);
            AppDatabase db = AppDatabase.getDatabase(holder.itemView.getContext());
            db.timedItemDao().update(timedItem);   // update in database
        });

        holder.dragHandle.setVisibility(View.GONE);

        if (isEditMode) {
            holder.settingsButton.setVisibility(View.VISIBLE);
            holder.checkBox.setVisibility(View.GONE);
        } else {
            holder.settingsButton.setVisibility(View.GONE);
            holder.checkBox.setVisibility(View.VISIBLE);
        }

        holder.settingsButton.setOnClickListener(v -> {
            timedItemSettingsClickListener.onTimedItemSettingsClick(timedItem);
        });
    }

    private void updateCheckDailyItem(DailyItem dailyItem, DayRowViewHolder holder) {
        if (dailyItem.isCompleted()) {
            holder.title.setAlpha(0.5f);
            holder.checkBox.setAlpha(0.5f);
        } else {
            holder.title.setAlpha(1f);
            holder.checkBox.setAlpha(1f);
        }
    }

    private void updateCheckTimedItem(TimedItem timedItem, DayRowViewHolder holder) {
        if (timedItem.isCompleted()) {
            holder.title.setAlpha(0.5f);
            holder.checkBox.setAlpha(0.5f);
        } else {
            holder.title.setAlpha(1f);
            holder.checkBox.setAlpha(1f);
        }
    }

    @Override
    public int getItemCount() {
        return 1 + dailyItems.size() + 1 + timedItems.size();
    }

    static class DayRowViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView title;
        ImageView dragHandle;
        ImageButton settingsButton;

        DayRowViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBox);
            title = itemView.findViewById(R.id.textTitle);
            dragHandle = itemView.findViewById(R.id.dragHandle);
            settingsButton = itemView.findViewById(R.id.settingsButton);
        }
    }
}
