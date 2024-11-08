package com.example.eventapp.ui.events;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventapp.R;

public class EventsListAdapter extends RecyclerView.Adapter<EventsListAdapter.ViewHolder> {

    private final EventAdapter organizedEventsAdapter;
    private final EventAdapter signedUpEventsAdapter;
    private int itemCount = 2;

    /**
     * Initializes the EventsListAdapter, which handles the views for both organized and signed up events
     * @param organizedEventsAdapter The EventAdapter of the organized events
     * @param signedUpEventsAdapter The EventAdapter of the signed up events
     */
    public EventsListAdapter(EventAdapter organizedEventsAdapter, EventAdapter signedUpEventsAdapter) {
        this.organizedEventsAdapter = organizedEventsAdapter;
        this.signedUpEventsAdapter = signedUpEventsAdapter;
    }

    /**
     * Initializes the ViewHolder, so that the grid can be populated
     */
    public static class ViewHolder extends RecyclerView.ViewHolder{
        RecyclerView eventsGrid;

        public ViewHolder(View view){
            super(view);
            eventsGrid = view.findViewById(R.id.events_grid);
        }

        public RecyclerView getEventsGrid() {
            return eventsGrid;
        }
    }

    /**
     * Inflates the view for later use
     * @param viewGroup The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return The ViewHolder of the now inflated view
     */
    @NonNull
    @Override
    public EventsListAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.events_list, viewGroup, false);
        return new EventsListAdapter.ViewHolder(view);
    }

    /**
     * Fills in the frid with the event adapter that is representative of the page (first page for
     * Signed Up Events, second page for Organized Events)
     * @param viewHolder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(EventsListAdapter.ViewHolder viewHolder, final int position) {
        viewHolder.getEventsGrid().setLayoutManager(new GridLayoutManager(viewHolder.itemView.getContext(), 2));
        if (position == 0) { // Signed-Up Events
            viewHolder.getEventsGrid().setAdapter(signedUpEventsAdapter);
        } else { // Organized Events
            viewHolder.getEventsGrid().setAdapter(organizedEventsAdapter);
        }
    }

    /**
     * Sets the itemCount of the adapter, which is setting the amount of pages
     * @param itemCount Integer of how many pages there should be
     */
    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    /**
     * Gets the item count, which is the amount of pages
     * @return The item count, which is the amount of pages
     */
    @Override
    public int getItemCount() {
        return itemCount;
    }
}
