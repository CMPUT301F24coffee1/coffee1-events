package com.example.eventapp.ui.events;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eventapp.R;
import com.example.eventapp.models.User;
import com.example.eventapp.repositories.UserRepository;
import com.example.eventapp.services.photos.PhotoManager;

import java.util.ArrayList;
import java.util.Objects;

public class EntrantsAdapter extends RecyclerView.Adapter<EntrantsAdapter.ViewHolder>  {

    private ArrayList<User> entrantList;

    public EntrantsAdapter(ArrayList<User> entrants){
        entrantList = entrants;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView entrantName;
        private final ImageView entrantPhoto;
        private final TextView you;

        public ViewHolder(View view){
            super(view);
            entrantName = view.findViewById(R.id.entrant_name_card_text);
            entrantPhoto = view.findViewById(R.id.entrant_card_photo);
            you = view.findViewById(R.id.entrant_card_you);
        }

        public TextView getNameView() {
            return entrantName;
        }
        public ImageView getPhotoView() {
            return entrantPhoto;
        }

        public TextView getYouView() {
            return you;
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
        User user = entrantList.get(position);
        User actualUser = UserRepository.getInstance().getCurrentUserLiveData().getValue();
        viewHolder.getNameView().setText(user.getName());

        if (user.hasPhoto()) {
            Glide.with(viewHolder.itemView.getContext())
                    .load(user.getPhotoUri())
                    .into(viewHolder.getPhotoView());
        } else {
            viewHolder.getPhotoView().setImageBitmap(PhotoManager.generateDefaultProfilePicture(user.getName(), user.getUserId()));
        }

        assert actualUser != null;
        if (Objects.equals(user.getUserId(), actualUser.getUserId())) {
            viewHolder.getYouView().setVisibility(View.VISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return entrantList.size();
    }
}
