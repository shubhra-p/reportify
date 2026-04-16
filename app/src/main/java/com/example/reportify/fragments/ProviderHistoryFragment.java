package com.example.reportify.fragments;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.reportify.adapters.ComplaintAdapter;
import com.example.reportify.databinding.FragmentProviderHistoryBinding;
import com.example.reportify.models.Complaint;
import com.example.reportify.utils.FirebaseManager;

import java.util.ArrayList;
import java.util.List;

public class ProviderHistoryFragment extends Fragment {

    private FragmentProviderHistoryBinding binding;

    private List<Complaint> complaintList;   // filtered list (shown)
    private List<Complaint> fullList;        // original list (all data)

    private ComplaintAdapter adapter;
    private ListenerRegistration listenerRegistration;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentProviderHistoryBinding.inflate(inflater, container, false);

        complaintList = new ArrayList<>();
        fullList = new ArrayList<>();

        adapter = new ComplaintAdapter(requireContext(), complaintList, true);

        binding.recyclerHistory.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );
        binding.recyclerHistory.setAdapter(adapter);

        setupSearch();
        fetchResolvedComplaints();

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

    // 🔥 FILTER LOGIC
    private void filterList(String text) {

        complaintList.clear();

        if (TextUtils.isEmpty(text)) {
            complaintList.addAll(fullList);
        } else {

            for (Complaint c : fullList) {


                String title = c.getTitle();

                if (title != null &&
                        title.toLowerCase().contains(text.toLowerCase())) {

                    complaintList.add(c);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    // 🔥 FIREBASE FETCH
    private void fetchResolvedComplaints() {

        String providerId = FirebaseManager.getAuth().getCurrentUser().getUid();

        listenerRegistration = FirebaseManager.getFirestore()
                .collection("complaints")
                .whereEqualTo("providerId", providerId)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {

                    if (e != null || queryDocumentSnapshots == null) return;

                    complaintList.clear();
                    fullList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {

                        Complaint complaint = doc.toObject(Complaint.class);

                        if ("RESOLVED".equals(complaint.getStatus())) {

                            complaintList.add(complaint);
                            fullList.add(complaint); // 🔥 keep original copy
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

        binding = null;
    }
}