package com.bignat.toutdoux.timeless_lists.timeless_list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bignat.toutdoux.AppDatabase;
import com.bignat.toutdoux.R;
import com.bignat.toutdoux.timeless_lists.timeless_list.timeless_item.TimelessItem;

import java.util.List;

public class TimelessListAdapter extends RecyclerView.Adapter<TimelessListAdapter.TimelessViewHolder> {

    private List<TimelessItem> timelessList;
    private OnItemClickListener listener;
    private TimelessListDao timelessListDao;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public TimelessListAdapter(List<TimelessItem> timelessItems, TimelessListDao timelessListDao) {
        this.timelessList = timelessItems;
        this.timelessListDao = timelessListDao;
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

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setCompleted(isChecked);
            AppDatabase db = AppDatabase.getDatabase(holder.itemView.getContext());
            db.timelessListDao().update(item);   // update in database
        });
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

    static class TimelessViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView textTitle;

        TimelessViewHolder(View itemView, OnItemClickListener listener) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBox);
            textTitle = itemView.findViewById(R.id.textTitle);

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
