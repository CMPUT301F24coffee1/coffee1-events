package com.example.eventapp.ui.images;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventapp.R;

/**
 * ImageListAdapter is an adapter that populates the actual list of images, given from the main
 * {@link ImagesFragment}. It is necessary to interface with the ViewPager, which splits the list of
 * images into events, profile, and facility images.
 */
public class ImageListAdapter extends RecyclerView.Adapter<ImageListAdapter.ViewHolder> {

    private final ImageAdapter eventImagesAdapter;
    private final ImageAdapter profileImagesAdapter;
    private final ImageAdapter facilityImagesAdapter;
    private int itemCount;

    /**
     * Initializes the ImagesListAdapter, which handles the views for event, profile, and facility images
     * @param eventImagesAdapter The ImageAdapter of the all images relating to events
     * @param profileImagesAdapter The ImageAdapter of all images relating to profiles
     * @param facilityImagesAdapter The ImageAdapter of all images relating to facilities
     */
    public ImageListAdapter(ImageAdapter eventImagesAdapter, ImageAdapter profileImagesAdapter, ImageAdapter facilityImagesAdapter) {
        this.eventImagesAdapter = eventImagesAdapter;
        this.profileImagesAdapter = profileImagesAdapter;
        this.facilityImagesAdapter = facilityImagesAdapter;
        itemCount = 3;
    }

    /**
     * Initializes the ViewHolder, so that the grid can be populated
     */
    public static class ViewHolder extends RecyclerView.ViewHolder{
        RecyclerView imagesGrid;

        public ViewHolder(View view){
            super(view);
            imagesGrid = view.findViewById(R.id.images_grid);
        }

        public RecyclerView getImagesGrid() {
            return imagesGrid;
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
    public ImageListAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.images_list, viewGroup, false);
        return new ImageListAdapter.ViewHolder(view);
    }

    /**
     * Fills in the frid with the event adapter that is representative of the page (first page for
     * Signed Up Events, second page for Organized Events)
     * @param viewHolder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(ImageListAdapter.ViewHolder viewHolder, final int position) {
        viewHolder.getImagesGrid().setLayoutManager(new GridLayoutManager(viewHolder.itemView.getContext(), 3));
        if (position == 0) { // Signed-Up Events
            viewHolder.getImagesGrid().setAdapter(eventImagesAdapter);
        } else if (position == 1) { // Organized Events
            viewHolder.getImagesGrid().setAdapter(profileImagesAdapter);
        } else {
            viewHolder.getImagesGrid().setAdapter(facilityImagesAdapter);
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
