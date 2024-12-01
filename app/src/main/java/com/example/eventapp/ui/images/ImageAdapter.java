package com.example.eventapp.ui.images;

import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eventapp.R;

import java.util.ArrayList;

/**
 * ImageAdapter is a RecyclerView adapter for displaying a list of {@link Image} objects in a RecyclerView.
 * Each item in the list is represented by an {@link ImageAdapter.ViewHolder} that displays the relevant
 * image, whether it be event, profile, or facility. The adapter supports item click events
 * through the {@link OnImageClickListener} interface.
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
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    private final ArrayList<String> imageUriStringList;
    private final OnImageClickListener onImageClickListener;

    public interface OnImageClickListener {
        void onImageClick(String image);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final ImageView image;

        public ViewHolder(View view){
            super(view);
            image = view.findViewById(R.id.displayed_image);
        }

        public ImageView getImageView() {
            return image;
        }
    }

    public ImageAdapter(ArrayList<String> imageUriStrings, OnImageClickListener onEventClickListener) {
        imageUriStringList = imageUriStrings;
        this.onImageClickListener = onEventClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.image_card, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        String imageUriString = imageUriStringList.get(position);
        Glide.with(viewHolder.itemView.getContext())
                .load(imageUriString)
                .into(viewHolder.getImageView());
        viewHolder.itemView.setOnClickListener(v -> onImageClickListener.onImageClick(imageUriString));
    }

    @Override
    public int getItemCount() {
        return imageUriStringList.size();
    }

}
