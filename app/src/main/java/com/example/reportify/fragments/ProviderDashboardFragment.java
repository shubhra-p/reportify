package com.example.reportify.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.reportify.adapters.ComplaintAdapter;
import com.example.reportify.databinding.FragmentProviderDashboardBinding;
import com.example.reportify.models.Complaint;
import com.example.reportify.utils.FirebaseManager;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProviderDashboardFragment extends Fragment {

    private FragmentProviderDashboardBinding binding;
    private List<Complaint> complaintList;
    private ComplaintAdapter adapter;
    private ListenerRegistration listenerRegistration;
    private ListenerRegistration providerAnalyticsListener;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentProviderDashboardBinding.inflate(inflater, container, false);

        complaintList = new ArrayList<>();
        adapter = new ComplaintAdapter(requireContext(), complaintList, true);

        binding.recyclerComplaints.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );
        binding.recyclerComplaints.setAdapter(adapter);
        loadProviderAnalytics();
        fetchAssignedComplaints();


        return binding.getRoot();
    }

    /*
    * TODO:How to filter at firebase level (having Indexing Issue)
    *  */

    private void loadProviderAnalytics() {

        String providerId = FirebaseManager.getAuth().getCurrentUser().getUid();

        providerAnalyticsListener = FirebaseManager.getFirestore()
                .collection("complaints")
                .whereEqualTo("providerId", providerId)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {

                    if (e != null || queryDocumentSnapshots == null) return;

                    int total = 0;
                    int pending = 0;
                    int inProgress = 0;
                    int resolved = 0;

                    float totalRating = 0;
                    int ratingCount = 0;

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {

                        Complaint complaint = doc.toObject(Complaint.class);
                        if (complaint == null) continue;

                        total++;

                        String status = complaint.getStatus();

                        if ("PENDING".equals(status)) {
                            pending++;
                        } else if ("IN_PROGRESS".equals(status)) {
                            inProgress++;
                        } else if ("RESOLVED".equals(status)) {
                            resolved++;
                        }

                        if (complaint.getRating() > 0) {
                            totalRating += complaint.getRating();
                            ratingCount++;
                        }
                    }

                    float avgRating = ratingCount == 0 ? 0 : totalRating / ratingCount;

                    // Update UI
                    binding.tvTotalProvider.setText(String.valueOf(total));
                    binding.tvPendingProvider.setText(String.valueOf(pending));
                    binding.tvInProgressProvider.setText(String.valueOf(inProgress));
                    binding.tvResolvedProvider.setText(String.valueOf(resolved));
                    binding.tvAvgRatingProvider.setText(String.format("%.1f ⭐", avgRating));
                });
    }
    private void fetchAssignedComplaints() {

        String providerId = FirebaseManager.getAuth().getCurrentUser().getUid();

        listenerRegistration = FirebaseManager.getFirestore()
                .collection("complaints")
                .whereEqualTo("providerId", providerId)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {

                    if (e != null) return;

                    complaintList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {

                        Complaint complaint = doc.toObject(Complaint.class);

                        if (!"RESOLVED".equals(complaint.getStatus())) {
                            complaintList.add(complaint);
                        }
                    }

                    adapter.notifyDataSetChanged();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
        if (providerAnalyticsListener != null) {
            providerAnalyticsListener.remove();
        }
    }
}
