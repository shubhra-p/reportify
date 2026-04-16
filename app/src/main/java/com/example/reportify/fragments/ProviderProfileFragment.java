package com.example.reportify.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.reportify.activities.LoginActivity;
import com.example.reportify.activities.ProviderDashboardActivity;
import com.example.reportify.activities.ProviderEditProfileActivity;
import com.example.reportify.databinding.FragmentProviderProfileBinding;
import com.example.reportify.models.Provider;
import com.example.reportify.utils.FirebaseManager;

public class ProviderProfileFragment extends Fragment {

    private FragmentProviderProfileBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentProviderProfileBinding.inflate(inflater, container, false);

        loadProviderData();

        binding.btnEditProfile.setOnClickListener(v->{
            startActivity(new Intent(requireContext(), ProviderEditProfileActivity.class));
        });
        binding.btnLogout.setOnClickListener(v -> {
            FirebaseManager.getAuth().signOut();
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
        });

        return binding.getRoot();
    }

    private void loadProviderData() {

        String uid = FirebaseManager.getAuth().getCurrentUser().getUid();

        FirebaseManager.getFirestore()
                .collection("providers")
                .document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (snapshot.exists()) {

                        Provider provider = snapshot.toObject(Provider.class);

                        if (provider != null) {

                            // Name
                            binding.tvName.setText(provider.getName());

                            // Email (from auth)
                            binding.tvEmail.setText(
                                    FirebaseManager.getAuth().getCurrentUser().getEmail()
                            );

                            // Service Type
                            binding.tvServiceType.setText(provider.getServiceType());

                            // Address
                            binding.tvAddress.setText(provider.getAddress());

                            // Availability
                            if (provider.isAvailable()) {
                                binding.tvAvailability.setText("Currently Available");
                                binding.llAvailability.setBackgroundResource(
                                        com.example.reportify.R.drawable.bg_chip_priority
                                );
                            } else {
                                binding.tvAvailability.setText("Currently Unavailable");
                                binding.llAvailability.setBackgroundResource(
                                        com.example.reportify.R.drawable.bg_card_secondary
                                );
                            }

                            // Rating
                            binding.tvRating.setText(String.valueOf(provider.getRating()));

                            // Rating Count
                            binding.tvRatingCount.setText(
                                    String.valueOf(provider.getRatingCount())
                            );

                            // (Optional) You can store lat/lng if needed later
                            double lat = provider.getLatitude();
                            double lng = provider.getLongitude();
                        }
                    }
                });
    }
}