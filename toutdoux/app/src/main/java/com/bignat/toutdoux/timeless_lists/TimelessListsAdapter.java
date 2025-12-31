package com.bignat.toutdoux.timeless_lists;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.bignat.toutdoux.R;
import com.bignat.toutdoux.timeless_lists.timeless_list.TimelessList;

import java.util.List;

/**
 * Adapter used for the {@link RecyclerView} of {@link TimelessList}s
 */
public class TimelessListsAdapter extends RecyclerView.Adapter<TimelessListsAdapter.TimelessListViewHolder> {

    private List<TimelessList> timelessLists;
    private OnListClickListener listener;
    private OnSettingsClickListener settingsClickListener;
    private TimelessListsDao timelessListsDao;
    private View anchor;

    public interface OnListClickListener {
        void onListClick(TimelessList timelessList);
    }

    public interface OnSettingsClickListener {
        void onSettingsClick(TimelessList timelessList, View anchor);
    }

    public void setOnListClickListener(OnListClickListener listener) {
        this.listener = listener;
    }

    public void setOnSettingsClickListener(OnSettingsClickListener settingsClickListener) {
        this.settingsClickListener = settingsClickListener;
    }

    public TimelessListsAdapter(
            List<TimelessList> timelessLists,
            TimelessListsDao timelessListsDao
    ) {
        this.timelessLists = timelessLists;
        this.timelessListsDao = timelessListsDao;
    }

    @NonNull
    @Override
    public TimelessListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.timeless_list_item, parent, false);
        return new TimelessListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimelessListViewHolder holder, int position) {
        TimelessList timelessList = timelessLists.get(position);

        holder.timelessListTitle.setText(timelessList.getTimelessListTitle());

        // Open list
        holder.itemView.setOnClickListener(v ->
                listener.onListClick(timelessList)
        );

        // Open list settings
        holder.settingsButton.setOnClickListener(v -> {
            settingsClickListener.onSettingsClick(timelessList, v);
        });
    }

    @Override
    public int getItemCount() {
        return timelessLists.size();
    }

    public void onItemMove(int fromPosition, int toPosition) {
        TimelessList movedList = timelessLists.remove(fromPosition);
        timelessLists.add(toPosition, movedList);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void onDragFinished() {
        for (int i = 0; i < timelessLists.size(); i++) {
            TimelessList timelessList = timelessLists.get(i);
            timelessList.orderIndex = i;
            timelessListsDao.update(timelessList);
        }
    }

    static class TimelessListViewHolder extends RecyclerView.ViewHolder {
        TextView timelessListTitle;
        ImageButton settingsButton;

        TimelessListViewHolder(View itemView) {
            super(itemView);
            timelessListTitle = itemView.findViewById(R.id.TimelessListName);
            settingsButton = itemView.findViewById(R.id.settingsButton);
        }
    }
}
