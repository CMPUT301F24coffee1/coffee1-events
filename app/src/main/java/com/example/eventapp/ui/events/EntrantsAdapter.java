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

/**
 * Adapter class for displaying a list of entrants in a RecyclerView.
 * Each entrant is represented by a `UserSignupEntry` object containing user details and attendance status.
 */
public class EntrantsAdapter extends RecyclerView.Adapter<EntrantsAdapter.ViewHolder> {

    private final ArrayList<UserSignupEntry> entrantList;

    /**
     * Constructs an `EntrantsAdapter` with the given list of entrants.
     *
     * @param entrants The list of `UserSignupEntry` objects to display.
     */
    public EntrantsAdapter(ArrayList<UserSignupEntry> entrants) {
        entrantList = entrants;
    }

    /**
     * ViewHolder class to hold and manage views for individual entrant items.
     */
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

        /**
         * Returns the `TextView` displaying the entrant's name.
         *
         * @return The `TextView` for the entrant's name.
         */
        public TextView getNameView() {
            return entrantName;
        }

        /**
         * Returns the `ImageView` displaying the entrant's photo.
         *
         * @return The `ImageView` for the entrant's photo.
         */
        public ImageView getPhotoView() {
            return entrantPhoto;
        }

        /**
         * Returns the `TextView` displaying the entrant's attendance status.
         *
         * @return The `TextView` for the attendance status.
         */
        public TextView getStatusView() {
            return status;
        }

        /**
         * Returns the `CheckBox` for selecting the entrant.
         *
         * @return The `CheckBox` for selection.
         */
        public CheckBox getSelectionCheckBox() {
            return selectionCheckBox;
        }
    }

    /**
     * Creates a new ViewHolder for an entrant item.
     */
    @NonNull
    @Override
    public EntrantsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.entrant_card, parent, false);
        return new EntrantsAdapter.ViewHolder(view);
    }

    /**
     * Binds data from a `UserSignupEntry` to the views in the ViewHolder.
     */
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

    /**
     * Returns the total number of entrants in the list.
     *
     * @return The size of the entrant list.
     */
    @Override
    public int getItemCount() {
        return entrantList.size();
    }
}
