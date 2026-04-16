package com.example.reportify.activities;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.reportify.R;
import com.example.reportify.databinding.ActivityUserDashboardBinding;
import com.example.reportify.fragments.HomeFragment;
import com.example.reportify.fragments.ProfileFragment;
import com.example.reportify.fragments.UserComplaintsFragment;
import com.example.reportify.services.ComplaintSyncService;
import com.example.reportify.utils.PreferenceManager;

public class UserDashboardActivity extends AppCompatActivity {

    private PreferenceManager preferenceManager;
    private ActivityUserDashboardBinding binding;
    private String notificationComplaintId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(this);

        binding = ActivityUserDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        /* OVerKill For Now
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });
        */


        //Notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        101
                );
            }
        }

        boolean openFromNotification = getIntent().getBooleanExtra("open_complaints", false);
        notificationComplaintId = getIntent().getStringExtra("complaint_id");
        // Load default fragment
        int selectedTab;

        if (openFromNotification) {
            selectedTab = R.id.nav_complaints;
        } else {
            int savedTab = preferenceManager.getSelectedTab();
            selectedTab = (savedTab != -1) ? savedTab : R.id.nav_home;
        }

        // Set selected tab
        binding.bottomNavigation.setSelectedItemId(selectedTab);

        // FORCE LOAD fragment (this is the missing piece)
        if (getSupportFragmentManager().findFragmentById(R.id.fragmentContainer) == null) {
            loadFragmentById(selectedTab);
        }

        //Saves selected tab on change.
        binding.bottomNavigation.setOnItemSelectedListener(item -> {

            preferenceManager.saveSelectedTab(item.getItemId());
            loadFragmentById(item.getItemId());
            return true;
        });
    }

    private void loadFragmentById(int itemId) {

        Fragment selectedFragment = null;

        if (itemId == R.id.nav_home) {
            selectedFragment = new HomeFragment();
        } else if (itemId == R.id.nav_complaints) {
            selectedFragment = new UserComplaintsFragment();

            // 🔥 pass notification complaintId if exists
            if (notificationComplaintId != null) {
                Bundle bundle = new Bundle();
                bundle.putString("complaint_id", notificationComplaintId);
                selectedFragment.setArguments(bundle);
                notificationComplaintId = null;
            }

        } else if (itemId == R.id.nav_profile) {
            selectedFragment = new ProfileFragment();
        }

        if (selectedFragment != null) {
            loadFragment(selectedFragment);
        }
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

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
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
}
