package com.bignat.minima_list.day_view;

import android.content.res.ColorStateList;
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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bignat.minima_list.AppDatabase;
import com.bignat.minima_list.R;
import com.bignat.minima_list.day_view.day_sections.DaySectionTitle;
import com.bignat.minima_list.day_view.day_sections.daily_section.DailyItem;
import com.bignat.minima_list.day_view.day_sections.event_section.EventItem;
import com.bignat.minima_list.day_view.day_sections.timed_section.TimedItem;

import java.util.Calendar;
import java.util.List;

public class DayViewAdapter extends RecyclerView.Adapter<DayViewAdapter.DayRowViewHolder> {
    DaySectionTitle dailySectionTitle;
    DaySectionTitle timedSectionTitle;
    DaySectionTitle eventSectionTitle;
    private List<DailyItem> dailyItems;
    private List<TimedItem> timedItems;
    private List<TimedItem> postponedItems;
    private List<EventItem> eventItems;
    private OnDailyItemSettingsClickListener dailyItemSettingsClickListener;
    private OnTimedItemSettingsClickListener timedItemSettingsClickListener;
    private OnEventItemSettingsClickListener eventItemSettingsClickListener;
    private RefreshTimedItemsHook refreshTimedItemsHook;
    private boolean isEditMode;

    public DayViewAdapter(
            List<DailyItem> dailyItems,
            List<TimedItem> timedItems,
            List<TimedItem> postponedItems,
            List<EventItem> eventItems
    ) {
        dailySectionTitle = new DaySectionTitle("Daily");
        timedSectionTitle = new DaySectionTitle("To do");
        eventSectionTitle = new DaySectionTitle("Events");
        this.dailyItems = dailyItems;
        this.timedItems = timedItems;
        this.postponedItems = postponedItems;
        this.eventItems = eventItems;
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

    public interface OnEventItemSettingsClickListener {
        void onEventItemSettingsClick(EventItem eventItem);
    }

    public void setOnEventItemSettingsClickListener(OnEventItemSettingsClickListener eventItemSettingsClickListener) {
        this.eventItemSettingsClickListener = eventItemSettingsClickListener;
    }

    public interface RefreshTimedItemsHook {
        void refreshTimedItems();
    }

    public void setRefreshTimedItemsHook(RefreshTimedItemsHook refreshTimedItemsHook) {
        this.refreshTimedItemsHook = refreshTimedItemsHook;
    }

    @NonNull
    @Override
    public DayRowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.day_row_item, parent, false);
        return new DayViewAdapter.DayRowViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayRowViewHolder holder, int position) {
        if (position < 1) {
            bindViewSectionTitle(holder, dailySectionTitle);
        } else if (position < 1 + dailyItems.size()) {
            DailyItem item = dailyItems.get(position - 1);
            bindViewDailyItem(holder, item);
        } else if (position < 1 + dailyItems.size() + 1) {
            bindViewSectionTitle(holder, timedSectionTitle);
        } else if (position < 1 + dailyItems.size() + 1 + timedItems.size()) {
            TimedItem item = timedItems.get(position - 1 - dailyItems.size() - 1);
            bindViewTimedItem(holder, item);
        } else if (position < 1 + dailyItems.size() + 1 + timedItems.size() + postponedItems.size()) {
            TimedItem item = postponedItems.get(position - 1 - dailyItems.size() - 1 - timedItems.size());
            bindViewTimedItem(holder, item);
            holder.title.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_postponed_item));
        } else if (position < 1 + dailyItems.size() + 1 + timedItems.size() + postponedItems.size() + 1) {
            bindViewSectionTitle(holder, eventSectionTitle);
        } else {
            EventItem item = eventItems.get(position - 1 - dailyItems.size() - 1 - timedItems.size() - postponedItems.size() - 1);
            bindViewEventItem(holder, item);
        }
    }

    private void bindViewSectionTitle(DayRowViewHolder holder, DaySectionTitle sectionTitle) {
        holder.title.setText(sectionTitle.getSectionTitle());
        holder.title.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_title));
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
        holder.title.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_normal));
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
            Calendar now = Calendar.getInstance();
            dailyItem.setLastTimeCompleted(now.getTime());
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
        holder.title.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_normal));
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
            AppDatabase db = AppDatabase.getDatabase(holder.itemView.getContext());
            db.timedItemDao().update(timedItem);   // update in database
            updateCheckTimedItem(timedItem, holder);
            refreshTimedItemsHook.refreshTimedItems();
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

    private void bindViewEventItem(DayRowViewHolder holder, EventItem eventItem) {
        holder.title.setText(eventItem.getTitle());
        holder.title.setTextSize(18f);
        holder.title.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_normal));
        holder.title.setGravity(Gravity.CENTER_VERTICAL);
        Calendar now = Calendar.getInstance();
        if (eventItem.getEndDate().compareTo(now.getTime()) < 0) {
            holder.title.setAlpha(0.5f);
        } else {
            holder.title.setAlpha(1f);
        }

        if (eventItem.isOptional()) {
            holder.title.setTypeface(null, Typeface.ITALIC);
        } else {
            holder.title.setTypeface(null, Typeface.NORMAL);
        }

        holder.checkBox.setVisibility(View.GONE);

        holder.dragHandle.setVisibility(View.GONE);

        if (isEditMode) {
            holder.settingsButton.setVisibility(View.VISIBLE);
        } else {
            holder.settingsButton.setVisibility(View.GONE);
        }

        holder.settingsButton.setOnClickListener(v -> {
            eventItemSettingsClickListener.onEventItemSettingsClick(eventItem);
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
        return 1 + dailyItems.size() + 1 + timedItems.size() + postponedItems.size() + 1 + eventItems.size();
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
