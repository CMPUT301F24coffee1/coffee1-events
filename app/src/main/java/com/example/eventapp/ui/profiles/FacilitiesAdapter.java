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

    public interface OnFacilityClickListener {
        void onFacilityClick(Facility facility);
    }

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

    public FacilitiesAdapter(ArrayList<Facility> facilities, FacilitiesAdapter.OnFacilityClickListener onFacilityClickListener) {
        facilityList = facilities;
        this.onFacilityClickListener = onFacilityClickListener;
    }

    @NonNull
    @Override
    public FacilitiesAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.facility_card, viewGroup, false);
        return new FacilitiesAdapter.ViewHolder(view);
    }

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

    @Override
    public int getItemCount() {
        return facilityList.size();
    }
}
