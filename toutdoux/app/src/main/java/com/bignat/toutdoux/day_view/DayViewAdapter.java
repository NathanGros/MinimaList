package com.bignat.toutdoux.day_view;

import android.graphics.Typeface;
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
import com.bignat.toutdoux.day_view.day_sections.daily_section.DailyItem;

import java.util.List;

public class DayViewAdapter extends RecyclerView.Adapter<DayViewAdapter.DayViewHolder> {
    private List<DailyItem> dailyItems;
    private boolean isEditMode;

    public DayViewAdapter(
            List<DailyItem> dailyItems
    ) {
        this.dailyItems = dailyItems;
        this.isEditMode = false;
    }

    public void setEditMode(boolean editMode) {
        isEditMode = editMode;
        notifyDataSetChanged();
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.day_row_item, parent, false);
        return new DayViewAdapter.DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        DailyItem item = dailyItems.get(position);

        holder.title.setText(item.getTitle());
        if (item.isOptional()) {
            holder.title.setTypeface(null, Typeface.ITALIC);
        } else {
            holder.title.setTypeface(null, Typeface.NORMAL);
        }

        holder.checkBox.setChecked(item.isCompleted());

        updateCheck(item, item.isCompleted(), holder);

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateCheck(item, isChecked, holder);
            AppDatabase db = AppDatabase.getDatabase(holder.itemView.getContext());
            db.dailyItemDao().update(item);   // update in database
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
//
//        holder.settingsButton.setOnClickListener(v -> {
//            settingsListener.onItemSettingsClick(position);
//        });
    }

    @Override
    public int getItemCount() {
        return dailyItems.size();
    }

    private void updateCheck(DailyItem item, boolean isChecked, DayViewAdapter.DayViewHolder holder) {
        item.setCompleted(isChecked);
        if (item.isCompleted()) {
            holder.title.setAlpha(0.5f);
            holder.checkBox.setAlpha(0.5f);
        } else {
            holder.title.setAlpha(1f);
            holder.checkBox.setAlpha(1f);
        }
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView title;
        ImageView dragHandle;
        ImageButton settingsButton;

        DayViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBox);
            title = itemView.findViewById(R.id.textTitle);
            dragHandle = itemView.findViewById(R.id.dragHandle);
            settingsButton = itemView.findViewById(R.id.settingsButton);
        }
    }
}
