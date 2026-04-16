package com.example.reportify.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.DocumentChange;

import com.example.reportify.utils.NotificationHelper;
import com.example.reportify.adapters.ComplaintAdapter;
import com.example.reportify.databinding.FragmentUserComplaintsBinding;
import com.example.reportify.models.Complaint;
import com.example.reportify.utils.FirebaseManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UserComplaintsFragment extends Fragment {

    private FragmentUserComplaintsBinding binding;

    private List<Complaint> complaintList; // filtered
    private List<Complaint> fullList;      // original

    private ComplaintAdapter adapter;
    private ListenerRegistration listenerRegistration;

    private Map<String, String> complaintStatusMap = new HashMap<>();
    private String targetComplaintId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentUserComplaintsBinding.inflate(inflater, container, false);

        complaintList = new ArrayList<>();
        fullList = new ArrayList<>();

        adapter = new ComplaintAdapter(requireContext(), complaintList, false);

        binding.recyclerUserComplaints.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );
        binding.recyclerUserComplaints.setAdapter(adapter);

        setupSearch(); // 🔥 NEW

        if (getArguments() != null)
            targetComplaintId = getArguments().getString("complaint_id");

        fetchUserComplaints();

        return binding.getRoot();
    }

    // 🔍 SEARCH SETUP
    private void setupSearch() {

        binding.searchView.setOnQueryTextListener(
                new androidx.appcompat.widget.SearchView.OnQueryTextListener() {

                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        filterList(query);
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        filterList(newText);
                        return true;
                    }
                });
    }

    // 🔥 FILTER LOGIC (TITLE + STATUS)
    private void filterList(String text) {

        complaintList.clear();

        if (TextUtils.isEmpty(text)) {
            complaintList.addAll(fullList);
        } else {

            for (Complaint c : fullList) {

                String title = c.getTitle();
                String status = c.getStatus();

                if (
                        (title != null && title.toLowerCase().contains(text.toLowerCase())) ||
                                (status != null && status.toLowerCase().contains(text.toLowerCase()))
                ) {
                    complaintList.add(c);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void fetchUserComplaints() {

        String userId = Objects.requireNonNull(
                FirebaseManager.getAuth().getCurrentUser()
        ).getUid();

        listenerRegistration = FirebaseManager.getFirestore()
                .collection("complaints")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {

                    if (e != null || queryDocumentSnapshots == null) return;

                    // 🔔 Handle updates (UNCHANGED)
                    for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {

                        Complaint complaint = dc.getDocument().toObject(Complaint.class);
                        String complaintId = complaint.getComplaintId();
                        String newStatus = complaint.getStatus();

                        if (dc.getType() == DocumentChange.Type.MODIFIED) {

                            String oldStatus = complaintStatusMap.get(complaintId);

                            if (oldStatus != null && !oldStatus.equals(newStatus)) {

                                String message;

                                switch (newStatus) {
                                    case "ACCEPTED":
                                        message = "Your complaint has been accepted";
                                        break;
                                    case "IN_PROGRESS":
                                        message = "Work has started on your complaint";
                                        break;
                                    case "RESOLVED":
                                        message = "Your complaint has been resolved 🎉";
                                        break;
                                    default:
                                        message = "Complaint updated: " + newStatus;
                                }

                                NotificationHelper.showNotification(
                                        getContext(),
                                        "Complaint Update",
                                        message,
                                        complaintId
                                );
                            }
                        }

                        complaintStatusMap.put(complaintId, newStatus);
                    }

                    // 🔄 Update lists
                    complaintList.clear();
                    fullList.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Complaint complaint = doc.toObject(Complaint.class);

                        complaintList.add(complaint);
                        fullList.add(complaint); // 🔥 important
                    }

                    adapter.notifyDataSetChanged();

                    // 🎯 Scroll to specific complaint (UNCHANGED)
                    if (targetComplaintId != null) {

                        for (int i = 0; i < complaintList.size(); i++) {

                            if (complaintList.get(i).getComplaintId().equals(targetComplaintId)) {

                                binding.recyclerUserComplaints.smoothScrollToPosition(i);

                                Toast.makeText(getContext(),
                                        "Opened updated complaint",
                                        Toast.LENGTH_SHORT).show();

                                break;
                            }
                        }

                        targetComplaintId = null;
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }

        binding = null;
    }
}