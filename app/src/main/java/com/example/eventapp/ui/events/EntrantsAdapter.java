package com.example.eventapp.ui.events;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eventapp.R;
import com.example.eventapp.models.User;
import com.example.eventapp.repositories.DTOs.UserSignupEntry;
import com.example.eventapp.services.photos.PhotoManager;

import java.util.ArrayList;

public class EntrantsAdapter extends RecyclerView.Adapter<EntrantsAdapter.ViewHolder> {

    private final ArrayList<UserSignupEntry> entrantList;

    public EntrantsAdapter(ArrayList<UserSignupEntry> entrants) {
        entrantList = entrants;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView entrantName;
        private final ImageView entrantPhoto;
        private final TextView status;
        private final CheckBox selectionCheckBox;

        public ViewHolder(View view) {
            super(view);
            entrantName = view.findViewById(R.id.entrant_name_card_text);
            entrantPhoto = view.findViewById(R.id.entrant_card_photo);
            status = view.findViewById(R.id.entrant_card_status);
            selectionCheckBox = view.findViewById(R.id.entrant_card_checkbox);
        }

        public TextView getNameView() {
            return entrantName;
        }

        public ImageView getPhotoView() {
            return entrantPhoto;
        }

        public TextView getStatusView() {
            return status;
        }

        public CheckBox getSelectionCheckBox() {
            return selectionCheckBox;
        }
    }

    @NonNull
    @Override
    public EntrantsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.entrant_card, parent, false);
        return new EntrantsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EntrantsAdapter.ViewHolder viewHolder, int position) {
        UserSignupEntry entry = entrantList.get(position);
        User user = entry.getUser();

        viewHolder.getNameView().setText(user.getName());

        if (user.hasPhoto()) {
            Glide.with(viewHolder.itemView.getContext())
                    .load(user.getPhotoUri())
                    .into(viewHolder.getPhotoView());
        } else {
            viewHolder.getPhotoView().setImageBitmap(PhotoManager.generateDefaultProfilePicture(user.getName(), user.getUserId()));
        }

        viewHolder.getSelectionCheckBox().setChecked(entry.isSelected());
        viewHolder.getStatusView().setText(entry.getAttendanceStatus());

        viewHolder.itemView.setOnClickListener(v -> {
            boolean newSelectedState = !entry.isSelected();
            entry.setSelected(newSelectedState);
            viewHolder.getSelectionCheckBox().setChecked(newSelectedState);
        });

        viewHolder.getSelectionCheckBox().setOnClickListener(v -> {
            boolean newSelectedState = viewHolder.getSelectionCheckBox().isChecked();
            entry.setSelected(newSelectedState);
        });
    }

    @Override
    public int getItemCount() {
        return entrantList.size();
    }
}
