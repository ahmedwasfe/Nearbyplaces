package com.example.nearbyplaces.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nearbyplaces.Model.StoreLocation;
import com.example.nearbyplaces.R;

import java.util.List;

public class NewLocationAdapter extends RecyclerView.Adapter<NewLocationAdapter.LocationHolder> {

    private Context mContext;
    private List<StoreLocation> mListNewLocation;
    private LayoutInflater inflater;

    public NewLocationAdapter(Context mContext, List<StoreLocation> mListNewLocation) {
        this.mContext = mContext;
        this.mListNewLocation = mListNewLocation;

        inflater = LayoutInflater.from(mContext);
    }

    @NonNull
    @Override
    public LocationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View layoutView = inflater.inflate(R.layout.raw_new_location, parent, false);

        return new LocationHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationHolder holder, int position) {

        holder.mLocationName.setText(mListNewLocation.get(position).getName());
        holder.mLocationLat.setText(String.valueOf(mListNewLocation.get(position).getLat()));
        holder.mLocationLng.setText(String.valueOf(mListNewLocation.get(position).getLng()));
    }

    @Override
    public int getItemCount() {
        return mListNewLocation.size();
    }

    static class LocationHolder extends RecyclerView.ViewHolder{

        TextView mLocationName, mLocationLat, mLocationLng;

        public LocationHolder(@NonNull View itemView) {
            super(itemView);

            mLocationName = itemView.findViewById(R.id.txt_location_name);
            mLocationLat = itemView.findViewById(R.id.txt_location_lat);
            mLocationLng = itemView.findViewById(R.id.txt_location_lng);
        }
    }
}
