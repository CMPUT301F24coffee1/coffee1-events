package com.example.eventapp.ui.events;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventapp.R;
import com.example.eventapp.models.User;

import java.util.ArrayList;

public class EntrantsAdapter extends RecyclerView.Adapter<EntrantsAdapter.ViewHolder>  {

    private ArrayList<User> entrantList;

    public EntrantsAdapter(ArrayList<User> entrants){
        entrantList = entrants;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView entrantName;
        private final ImageView entrantPhoto;

        public ViewHolder(View view){
            super(view);
            entrantName = view.findViewById(R.id.entrant_name_card_text);
            entrantPhoto = view.findViewById(R.id.entrant_card_photo);
        }

        public TextView getNameView() {
            return entrantName;
        }
        public ImageView getImageView() {
            return entrantPhoto;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.entrant_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
    }

    @Override
    public int getItemCount() {
        return entrantList.size();
    }
}
