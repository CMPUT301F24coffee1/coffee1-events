package com.example.eventapp.ui.events;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eventapp.models.Event;
import com.example.eventapp.R;

import java.util.ArrayList;

/**
 * EventAdapter is a RecyclerView adapter for displaying a list of {@link Event} objects in a RecyclerView.
 * Each item in the list is represented by an {@link EventAdapter.ViewHolder} that displays the event's
 * name and poster image. The adapter supports item click events through the {@link OnEventClickListener} interface.
 * <p>
 * The layout and functionality of this adapter rely on Android's RecyclerView API, as well as the Glide
 * library for image loading.
 * </p>
 *
 * <p>References:</p>
 * <ul>
 *     <li><a href="https://developer.android.com/develop/ui/views/layout/recyclerview">RecyclerView Documentation</a></li>
 * </ul>
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {
    private final ArrayList<Event> eventList;
    private final OnEventClickListener onEventClickListener;

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView eventName;
        private final ImageView eventPoster;

        public ViewHolder(View view){
            super(view);
            eventName = view.findViewById(R.id.event_name_text);
            eventPoster = view.findViewById(R.id.event_poster_image);
        }

        public TextView getTextView() {
            return eventName;
        }

        public ImageView getPosterView() {
            return eventPoster;
        }
    }

    public EventAdapter(ArrayList<Event> events, OnEventClickListener onEventClickListener) {
        eventList = events;
        this.onEventClickListener = onEventClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.event_card, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Event event = eventList.get(position);
        viewHolder.getTextView().setText(event.getEventName());

        if (event.hasPoster()) {
            Glide.with(viewHolder.itemView.getContext())
                    .load(event.getPosterUri())
                    .into(viewHolder.getPosterView());
        } else {
            viewHolder.getPosterView().setImageResource(R.drawable.default_event_poster);
        }
        viewHolder.itemView.setOnClickListener(v -> onEventClickListener.onEventClick(event));
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

}
