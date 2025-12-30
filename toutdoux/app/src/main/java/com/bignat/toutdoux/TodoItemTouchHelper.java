package com.bignat.toutdoux;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class TodoItemTouchHelper extends ItemTouchHelper.Callback {

    private final TodoAdapter adapter;

    public TodoItemTouchHelper(TodoAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true; // long press to drag
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return false; // disable swipe (optional)
    }

    @Override
    public int getMovementFlags(
            @NonNull RecyclerView recyclerView,
            @NonNull RecyclerView.ViewHolder viewHolder
    ) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        return makeMovementFlags(dragFlags, 0);
    }

    @Override
    public boolean onMove(
            @NonNull RecyclerView recyclerView,
            @NonNull RecyclerView.ViewHolder viewHolder,
            @NonNull RecyclerView.ViewHolder target
    ) {
        int from = viewHolder.getAdapterPosition();
        int to = target.getAdapterPosition();

        adapter.onItemMove(from, to);
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        // not used
    }

    @Override
    public void clearView(
            @NonNull RecyclerView recyclerView,
            @NonNull RecyclerView.ViewHolder viewHolder
    ) {
        super.clearView(recyclerView, viewHolder);

        viewHolder.itemView.setBackgroundResource(R.drawable.todo_item_bg);
        viewHolder.itemView.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(150)
                .start();

        viewHolder.itemView.setElevation(0f);

        adapter.onDragFinished();
    }

    @Override
    public void onSelectedChanged(
            RecyclerView.ViewHolder viewHolder,
            int actionState
    ) {
        super.onSelectedChanged(viewHolder, actionState);

        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG && viewHolder != null) {
            viewHolder.itemView.setBackgroundResource(R.drawable.todo_item_bg_drag);
            viewHolder.itemView.animate()
                    .scaleX(1.05f)
                    .scaleY(1.05f)
                    .setDuration(150)
                    .start();

            viewHolder.itemView.setElevation(16f);
        }
    }
}
