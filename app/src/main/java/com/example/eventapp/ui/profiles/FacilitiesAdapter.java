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
import com.example.eventapp.models.Facility;

import java.util.ArrayList;

public class FacilitiesAdapter extends RecyclerView.Adapter<FacilitiesAdapter.ViewHolder> {
    private final ArrayList<Facility> facilityList;
    private final FacilitiesAdapter.OnFacilityClickListener onFacilityClickListener;

    /**
     * Sets the function onFacilityClick to run when the on facility click listener activates
     */
    public interface OnFacilityClickListener {
        void onFacilityClick(Facility facility);
    }

    /**
     * Initializes the views to be managed by the Adapter
     */
    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView facilityName;
        private final TextView facilityDesc;
        private final ImageView facilityPhoto;

        public ViewHolder(View view){
            super(view);
            facilityName = view.findViewById(R.id.facility_name_card_text);
            facilityDesc = view.findViewById(R.id.facility_desc_card_text);
            facilityPhoto = view.findViewById(R.id.facility_card_photo);
        }

        public TextView getTextTitleView() {
            return facilityName;
        }

        public TextView getTextDescView() {
            return facilityDesc;
        }

        public ImageView getPhotoView() { return facilityPhoto; }
    }

    /**
     * Initializes the FacilitiesAdapter
     * @param facilities Array List of all facilities to be managed
     * @param onFacilityClickListener onClickListener that runs everytime a facility is tapped in the list
     */
    public FacilitiesAdapter(ArrayList<Facility> facilities, FacilitiesAdapter.OnFacilityClickListener onFacilityClickListener) {
        facilityList = facilities;
        this.onFacilityClickListener = onFacilityClickListener;
    }

    /**
     * Inflates the view using the facility card layout
     * @param viewGroup The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return The ViewHolder
     */
    @NonNull
    @Override
    public FacilitiesAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.facility_card, viewGroup, false);
        return new FacilitiesAdapter.ViewHolder(view);
    }

    /**
     * When ViewHolder is bound, sets the on click listener, and also updates the photos from the database
     * @param viewHolder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(FacilitiesAdapter.ViewHolder viewHolder, final int position) {
        Facility facility = facilityList.get(position);
        viewHolder.getTextTitleView().setText(facility.getFacilityName());
        viewHolder.getTextDescView().setText(facility.getFacilityDescription());

        viewHolder.itemView.setOnClickListener(v -> onFacilityClickListener.onFacilityClick(facility));

        if (facility.hasPhoto()) {
            Glide.with(viewHolder.itemView.getContext())
                    .load(facility.getPhotoUri())
                    .into(viewHolder.getPhotoView());
        } else {
            viewHolder.getPhotoView().setImageResource(R.drawable.ic_facility_24dp);
        }
    }

    /**
     * Gets the size of the entire list of facilities
     * @return The list of facilities
     */
    @Override
    public int getItemCount() {
        return facilityList.size();
    }
}
