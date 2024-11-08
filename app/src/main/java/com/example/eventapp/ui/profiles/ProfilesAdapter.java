package com.example.eventapp.ui.profiles;

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
import com.example.eventapp.photos.PhotoManager;
import com.example.eventapp.repositories.UserRepository;

import java.util.ArrayList;
import java.util.Objects;

public class ProfilesAdapter extends RecyclerView.Adapter<ProfilesAdapter.ViewHolder> {
    private final ArrayList<User> profileList;
    private final ProfilesAdapter.OnProfileClickListener onProfileClickListener;

    /**
     * Sets the function onProfileClick to run when the on profile click listener activates
     */
    public interface OnProfileClickListener {
        void onProfileClick(User user);
    }

    /**
     * Initializes the views to be managed by the Adapter
     */
    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView profileName;
        private final ImageView profilePhoto;
        private final TextView you;

        public ViewHolder(View view){
            super(view);
            profileName = view.findViewById(R.id.profile_name_card_text);
            profilePhoto = view.findViewById(R.id.profile_card_photo);
            you = view.findViewById(R.id.profile_card_you);
        }

        public TextView getTextTitleView() {
            return profileName;
        }

        public ImageView getPhotoView() { return profilePhoto; }

        public TextView getYouView() { return you; }
    }

    /**
     * Initializes the ProfileAdapter
     * @param profiles Array List of all profiles to be managed
     * @param onProfileClickListener onClickListener that runs everytime a profile is tapped in the list
     */
    public ProfilesAdapter(ArrayList<User> profiles, ProfilesAdapter.OnProfileClickListener onProfileClickListener) {
        profileList = profiles;
        this.onProfileClickListener = onProfileClickListener;
    }

    /**
     * Inflates the view using the profile card layout
     * @param viewGroup The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return The ViewHolder
     */
    @NonNull
    @Override
    public ProfilesAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.profile_card, viewGroup, false);
        return new ProfilesAdapter.ViewHolder(view);
    }

    /**
     * When ViewHolder is bound, sets the on click listener, and also updates the photos from the database
     * @param viewHolder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(ProfilesAdapter.ViewHolder viewHolder, final int position) {
        User user = profileList.get(position);
        User actualUser = UserRepository.getInstance().getCurrentUserLiveData().getValue();

        viewHolder.getTextTitleView().setText(user.getName());

        viewHolder.itemView.setOnClickListener(v -> onProfileClickListener.onProfileClick(user));

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

    /**
     * Gets the size of the entire list of profiles
     * @return The list of profiles
     */
    @Override
    public int getItemCount() {
        return profileList.size();
    }
}
