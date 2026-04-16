package com.example.reportify.adapters;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reportify.databinding.ItemProviderBinding;
import com.example.reportify.models.Provider;

import java.util.List;

public class ProviderAdapter extends RecyclerView.Adapter<ProviderAdapter.ProviderViewHolder> {

    private Context context;
    private List<Provider> providerList;
    private OnProviderClickListener listener;
    public interface OnProviderClickListener {
        void onProviderClick(Provider provider);
    }

    public ProviderAdapter(Context context, List<Provider> providerList,OnProviderClickListener listener) {
        this.context = context;
        this.providerList = providerList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProviderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProviderBinding binding = ItemProviderBinding.inflate(
                LayoutInflater.from(context), parent, false);
        return new ProviderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProviderViewHolder holder, int position) {

        Provider provider = providerList.get(position);

        holder.binding.tvProviderName.setText(provider.getName());
        holder.binding.tvAddress.setText("Address: " + provider.getAddress());
        holder.binding.tvAvailability.setText(
                provider.isAvailable() ? "Available" : "Not Available"
        );
        holder.binding.tvRating.setText("Rating: " + provider.getRating());
        holder.binding.tvDistance.setText(String.format("Distance: %.2f km", provider.getDistanceKm()));


        holder.itemView.setOnClickListener(v -> {
            Log.d("CLICK_TEST", "Provider clicked: " + provider.getName());

            if (listener != null) {
                Toast.makeText(context, "Listner is ok", Toast.LENGTH_SHORT).show();
                listener.onProviderClick(provider);
            }
        });

        /* HACK: Latest Update on App Integration
            When user clicks: “View on Map”
            It opens:
                Google Maps app
                Shows marker at provider location
         */
        holder.binding.btnViewOnMap.setOnClickListener(v -> {

            double lat = provider.getLatitude();
            double lon = provider.getLongitude();

            if (lat == 0.0 && lon == 0.0) {
                Toast.makeText(context, "Location not available",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // NOTE: for directly starting direction: "google.navigation:q=" + lat + "," + lon
            Uri gmmIntentUri = Uri.parse(
                    "geo:" + lat + "," + lon + "?q=" + lat + "," + lon

            );

            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            try {
                context.startActivity(mapIntent);
            } catch (Exception e) {
                Toast.makeText(context,
                        "Google Maps not installed",
                        Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return providerList.size();
    }

    static class ProviderViewHolder extends RecyclerView.ViewHolder {
        ItemProviderBinding binding;

        public ProviderViewHolder(ItemProviderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
