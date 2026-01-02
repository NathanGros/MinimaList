package com.bignat.toutdoux.timeless_lists.timeless_list;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bignat.toutdoux.AppDatabase;
import com.bignat.toutdoux.R;
import com.bignat.toutdoux.timeless_lists.timeless_list.timeless_item.TimelessItem;

import java.util.List;

public class TimelessListAdapter extends RecyclerView.Adapter<TimelessListAdapter.TimelessViewHolder> {

    private List<TimelessItem> timelessList;
    private OnItemClickListener listener;
    private OnItemSettingsClickListener settingsListener;
    private TimelessListDao timelessListDao;
    private boolean isEditMode;
    private ItemTouchHelper itemTouchHelper;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnItemSettingsClickListener {
        void onItemSettingsClick(int position);
    }

    public void setOnItemSettingsClickListener(OnItemSettingsClickListener settingsListener) {
        this.settingsListener = settingsListener;
    }

    public TimelessListAdapter(List<TimelessItem> timelessItems, TimelessListDao timelessListDao) {
        this.timelessList = timelessItems;
        this.timelessListDao = timelessListDao;
        this.isEditMode = false;
    }

    @NonNull
    @Override
    public TimelessViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.timeless_item, parent, false);
        return new TimelessViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull TimelessViewHolder holder, int position) {
        TimelessItem item = timelessList.get(position);

        holder.textTitle.setText(item.getTitle());
        holder.checkBox.setChecked(item.isCompleted());

        updateCheck(item, item.isCompleted(), holder);

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateCheck(item, isChecked, holder);
            AppDatabase db = AppDatabase.getDatabase(holder.itemView.getContext());
            db.timelessListDao().update(item);   // update in database
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

        holder.dragHandle.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                itemTouchHelper.startDrag(holder);
            }
            return false;
        });

        holder.settingsButton.setOnClickListener(v -> {
            settingsListener.onItemSettingsClick(position);
        });
    }

    private void updateCheck(TimelessItem item, boolean isChecked, TimelessViewHolder holder) {
        item.setCompleted(isChecked);
        if (item.completed) {
            holder.textTitle.setAlpha(0.5f);
            holder.checkBox.setAlpha(0.5f);
        } else {
            holder.textTitle.setAlpha(1f);
            holder.checkBox.setAlpha(1f);
        }
    }

    @Override
    public int getItemCount() {
        return timelessList.size();
    }

    public void onItemMove(int fromPosition, int toPosition) {
        TimelessItem movedItem = timelessList.remove(fromPosition);
        timelessList.add(toPosition, movedItem);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void onDragFinished() {
        for (int i = 0; i < timelessList.size(); i++) {
            TimelessItem item = timelessList.get(i);
            item.orderIndex = i;
            timelessListDao.update(item);
        }
    }

    public void setEditMode(boolean editMode) {
        isEditMode = editMode;
        notifyDataSetChanged();
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    public void setItemTouchHelper(ItemTouchHelper helper) {
        this.itemTouchHelper = helper;
    }

    static class TimelessViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView textTitle;
        ImageView dragHandle;
        ImageButton settingsButton;

        TimelessViewHolder(View itemView, OnItemClickListener listener) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBox);
            textTitle = itemView.findViewById(R.id.textTitle);
            dragHandle = itemView.findViewById(R.id.dragHandle);
            settingsButton = itemView.findViewById(R.id.settingsButton);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });
        }
    }
}
