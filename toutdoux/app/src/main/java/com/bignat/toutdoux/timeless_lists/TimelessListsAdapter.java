package com.bignat.toutdoux.timeless_lists;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
    private TimelessListsDao timelessListsDao;

    public interface OnListClickListener {
        void onListClick(TimelessList timelessList);
    }

    public void setOnListClickListener(OnListClickListener listener) {
        this.listener = listener;
    }

    public TimelessListsAdapter(List<TimelessList> timelessLists, TimelessListsDao timelessListsDao) {
        this.timelessLists = timelessLists;
        this.timelessListsDao = timelessListsDao;
    }

    @NonNull
    @Override
    public TimelessListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.timeless_list_item, parent, false);
        return new TimelessListViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull TimelessListViewHolder holder, int position) {
        TimelessList timelessList = timelessLists.get(position);

        holder.timelessListTitle.setText(timelessList.getTimelessListTitle());

        holder.itemView.setOnClickListener(v ->
                listener.onListClick(timelessList)
        );
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

        TimelessListViewHolder(View itemView, OnListClickListener listener) {
            super(itemView);
            timelessListTitle = itemView.findViewById(R.id.TimelessListName);
        }
    }
}
