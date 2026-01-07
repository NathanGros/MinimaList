package com.bignat.toutdoux.timeless_lists.timeless_list;

import android.graphics.Paint;
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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bignat.toutdoux.AppDatabase;
import com.bignat.toutdoux.R;
import com.bignat.toutdoux.timeless_lists.timeless_list.timeless_item.TimelessItem;

import java.util.List;

public class TimelessListAdapter extends RecyclerView.Adapter<TimelessListAdapter.TimelessViewHolder> {

    private List<TimelessItem> timelessList;
    private OnItemSettingsClickListener settingsListener;
    private TimelessListDao timelessListDao;
    private boolean isEditMode;
    private ItemTouchHelper itemTouchHelper;

    public interface OnItemSettingsClickListener {
        void onItemSettingsClick(TimelessItem timelessItem);
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
        return new TimelessViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimelessViewHolder holder, int position) {
        TimelessItem item = timelessList.get(position);

        holder.textTitle.setText(item.getTitle());
        if (item.isOptional()) {
            holder.textTitle.setTypeface(null, Typeface.ITALIC);
        } else {
            holder.textTitle.setTypeface(null, Typeface.NORMAL);
        }

        updateCheck(item, holder);
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(item.isCompleted());
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setCompleted(isChecked);
            updateCheck(item, holder);
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
            settingsListener.onItemSettingsClick(item);
        });
    }

    private void updateCheck(TimelessItem item, TimelessViewHolder holder) {
        if (item.isCompleted()) {
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

        TimelessViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBox);
            textTitle = itemView.findViewById(R.id.textTitle);
            dragHandle = itemView.findViewById(R.id.dragHandle);
            settingsButton = itemView.findViewById(R.id.settingsButton);
        }
    }
}
