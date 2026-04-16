package com.example.reportify.activities;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.reportify.R;
import com.example.reportify.databinding.ActivityProviderDashboardBinding;
import com.example.reportify.fragments.ProviderDashboardFragment;
import com.example.reportify.fragments.ProviderHistoryFragment;
import com.example.reportify.fragments.ProviderProfileFragment;
import com.example.reportify.services.ComplaintSyncService;

public class ProviderDashboardActivity extends AppCompatActivity {

    private ActivityProviderDashboardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityProviderDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadFragment(new ProviderDashboardFragment());

        binding.providerBottomNavigation.setOnItemSelectedListener(item -> {

            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.nav_provider_dashboard) {
                selectedFragment = new ProviderDashboardFragment();
            } else if (item.getItemId() == R.id.nav_provider_history) {
                selectedFragment = new ProviderHistoryFragment();
            } else if (item.getItemId() == R.id.nav_provider_profile) {
                selectedFragment = new ProviderProfileFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }

            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isInternetAvailable()) {

            Intent serviceIntent =
                    new Intent(this, ComplaintSyncService.class);

            startService(serviceIntent);
        }
    }

    private boolean isInternetAvailable() {

        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }

        return false;
    }
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.providerFragmentContainer, fragment)
                .commit();
    }
}
