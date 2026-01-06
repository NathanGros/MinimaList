package com.bignat.toutdoux.day_view;

import android.graphics.Typeface;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bignat.toutdoux.AppDatabase;
import com.bignat.toutdoux.R;
import com.bignat.toutdoux.day_view.day_sections.DayRow;
import com.bignat.toutdoux.day_view.day_sections.DaySectionTitle;
import com.bignat.toutdoux.day_view.day_sections.daily_section.DailyItem;
import com.bignat.toutdoux.day_view.day_sections.timed_section.TimedItem;

import java.util.List;

public class DayViewAdapter extends RecyclerView.Adapter<DayViewAdapter.DayRowViewHolder> {
    private List<DayRow> rows;
    private OnDailyItemSettingsClickListener dailyItemSettingsClickListener;
    private OnTimedItemSettingsClickListener timedItemSettingsClickListener;
    private boolean isEditMode;

    public DayViewAdapter(
            List<DayRow> rows
    ) {
        this.rows = rows;
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
        void onDailyItemSettingsClick(int position);
    }

    public void setOnDailyItemSettingsClickListener(OnDailyItemSettingsClickListener dailyItemSettingsClickListener) {
        this.dailyItemSettingsClickListener = dailyItemSettingsClickListener;
    }

    public interface OnTimedItemSettingsClickListener {
        void onTimedItemSettingsClick(int position);
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
        DayRow item = rows.get(position);

        if (item instanceof DaySectionTitle) {
            DaySectionTitle sectionTitle = (DaySectionTitle) item;
            holder.title.setText(sectionTitle.getSectionTitle());
            holder.title.setAlpha(1f);
            holder.title.setTextSize(22f);
            holder.title.setGravity(Gravity.CENTER);
            holder.title.setTypeface(null, Typeface.NORMAL);
            holder.dragHandle.setVisibility(View.GONE);
            holder.settingsButton.setVisibility(View.GONE);
            holder.checkBox.setVisibility(View.GONE);
        }
        else if (item instanceof DailyItem) {
            DailyItem dailyItem = (DailyItem) item;
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
                dailyItemSettingsClickListener.onDailyItemSettingsClick(position);
            });
        }
        else if (item instanceof TimedItem) {
            TimedItem timedItem = (TimedItem) item;

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
                timedItemSettingsClickListener.onTimedItemSettingsClick(position);
            });
        }
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
        return rows.size();
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
