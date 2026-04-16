package com.example.reportify.adapters;

/*
* Card View at Provider and User
* Provider side show Accept,Resolved,In Progress
* User side show nothing
* */

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reportify.databinding.ItemComplaintBinding;
import com.example.reportify.models.Complaint;
import com.example.reportify.utils.FirebaseManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ComplaintAdapter extends RecyclerView.Adapter<ComplaintAdapter.ComplaintViewHolder> {

    private Context context;
    private List<Complaint> complaintList;
    private boolean isProvider;


    public ComplaintAdapter(Context context,
                            List<Complaint> complaintList,
                            boolean isProvider) {
        this.context = context;
        this.complaintList = complaintList;
        this.isProvider = isProvider;
    }

    @NonNull
    @Override
    public ComplaintViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemComplaintBinding binding = ItemComplaintBinding.inflate(
                LayoutInflater.from(context), parent, false);
        return new ComplaintViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ComplaintViewHolder holder, int position) {

        Complaint complaint = complaintList.get(position);

        holder.binding.tvTitle.setText(complaint.getTitle());
        holder.binding.tvDescription.setText(complaint.getDescription());
        holder.binding.tvStatus.setText("Status: " + complaint.getStatus());
        holder.binding.tvUrgency.setText("Urgency: " + complaint.getUrgency());

        switch (complaint.getUrgency()) {
            case "HIGH":
                holder.binding.tvUrgency.setTextColor(Color.RED);
                break;
            case "MEDIUM":
                holder.binding.tvUrgency.setTextColor(Color.parseColor("#FFA500"));
                break;
            default:
                holder.binding.tvUrgency.setTextColor(Color.GREEN);
        }


        if (isProvider) {

            holder.binding.btnAccept.setVisibility(View.VISIBLE);
            holder.binding.btnInProgress.setVisibility(View.VISIBLE);
            holder.binding.btnResolve.setVisibility(View.VISIBLE);

            holder.binding.btnAccept.setOnClickListener(v ->
                    updateStatus(complaint.getComplaintId(), "ACCEPTED"));

            holder.binding.btnInProgress.setOnClickListener(v ->
                    updateStatus(complaint.getComplaintId(), "IN_PROGRESS"));

            holder.binding.btnResolve.setOnClickListener(v ->
                    updateStatus(complaint.getComplaintId(), "RESOLVED"));

        } else {

            holder.binding.btnAccept.setVisibility(View.GONE);
            holder.binding.btnInProgress.setVisibility(View.GONE);
            holder.binding.btnResolve.setVisibility(View.GONE);
        }

        if (!isProvider &&
                "RESOLVED".equals(complaint.getStatus()) &&
                !complaint.isRated()) {

            holder.binding.ratingBar.setVisibility(View.VISIBLE);
            holder.binding.btnSubmitRating.setVisibility(View.VISIBLE);

            holder.binding.btnSubmitRating.setOnClickListener(v -> {

                float ratingValue = holder.binding.ratingBar.getRating();

                if (ratingValue == 0) {
                    Toast.makeText(context,
                            "Please select rating",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                submitRating(complaint, ratingValue);
            });

        } else {
            holder.binding.ratingBar.setVisibility(View.GONE);
            holder.binding.btnSubmitRating.setVisibility(View.GONE);
        }
    }

    private void submitRating(Complaint complaint, float ratingValue) {

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Update complaint document
        firestore.collection("complaints")
                .document(complaint.getComplaintId())
                .update(
                        "rating", ratingValue,
                        "rated", true
                );

        // Update provider rating
        firestore.collection("providers")
                .document(complaint.getProviderId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    double currentRating =
                            documentSnapshot.getDouble("rating") != null
                                    ? documentSnapshot.getDouble("rating")
                                    : 0.0;

                    long ratingCount =
                            documentSnapshot.getLong("ratingCount") != null
                                    ? documentSnapshot.getLong("ratingCount")
                                    : 0;

                    double newAverage =
                            ((currentRating * ratingCount) + ratingValue)
                                    / (ratingCount + 1);

                    firestore.collection("providers")
                            .document(complaint.getProviderId())
                            .update(
                                    "rating", newAverage,
                                    "ratingCount", ratingCount + 1
                            );
                });

        Toast.makeText(context,
                "Thank you for rating!",
                Toast.LENGTH_SHORT).show();
    }
    private void updateStatus(String complaintId, String newStatus) {

        FirebaseManager.getFirestore()
                .collection("complaints")
                .document(complaintId)
                .update("status", newStatus);
    }
    @Override
    public int getItemCount() {
        return complaintList.size();
    }

    static class ComplaintViewHolder extends RecyclerView.ViewHolder {

        ItemComplaintBinding binding;

        public ComplaintViewHolder(ItemComplaintBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
