package com.example.eventapp.ui.profiles;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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

        public ViewHolder(View view){
            super(view);
            facilityName = view.findViewById(R.id.facility_name_card_text);
            facilityDesc = view.findViewById(R.id.facility_desc_card_text);
        }

        public TextView getTextTitleView() {
            return facilityName;
        }

        public TextView getTextDescView() {
            return facilityDesc;
        }
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
    }

    @Override
    public int getItemCount() {
        return facilityList.size();
    }
}
