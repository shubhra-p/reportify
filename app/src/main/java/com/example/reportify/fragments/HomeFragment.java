package com.example.reportify.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.helper.widget.Grid;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.reportify.R;

import com.example.reportify.activities.ProviderListActivity;
import com.example.reportify.adapters.ServiceAdapter;
import com.example.reportify.databinding.FragmentHomeBinding;
import com.example.reportify.models.Service;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private List<Service> serviceList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);

        setupServices();

        return binding.getRoot();
    }

    private void setupServices() {

        serviceList = new ArrayList<>();

        serviceList.add(new Service("Housekeeping", R.drawable.ic_housekeeping));
        serviceList.add(new Service("Electrician", R.drawable.ic_electrician));
        serviceList.add(new Service("Plumber", R.drawable.ic_plumbing));
        serviceList.add(new Service("Carpenter", R.drawable.ic_carpenter));
        serviceList.add(new Service("Internet Support", R.drawable.ic_internet));

        ServiceAdapter adapter = new ServiceAdapter(requireContext(), serviceList, service -> {

            Intent intent = new Intent(requireContext(), ProviderListActivity.class);
            intent.putExtra("serviceName", service.getName());
            startActivity(intent);

        });

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position == 4) {
                    return 2;
                }
                return 1;
            }
        });
        binding.recyclerServices.setLayoutManager(gridLayoutManager);
        binding.recyclerServices.setAdapter(adapter);
    }
}
