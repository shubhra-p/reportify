package com.example.reportify.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.reportify.databinding.ActivityProviderSetupBinding;
import com.example.reportify.models.Provider;
import com.example.reportify.utils.FirebaseManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;


public class ProviderSetupActivity extends AppCompatActivity {

    private ActivityProviderSetupBinding binding;

    //For Location tracking
    private FusedLocationProviderClient fusedLocationClient;
    private double latitude = 0.0;
    private double longitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityProviderSetupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getCurrentLocation();


        String[] services = {
                "Housekeeping",
                "Electrician",
                "Plumber",
                "Carpenter",
                "Internet Support"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_dropdown_item,
                        services);

        binding.spServiceType.setAdapter(adapter);

        binding.btnSave.setOnClickListener(v -> saveProvider());
    }

    private void saveProvider() {

        String uid = FirebaseManager.getAuth().getCurrentUser().getUid();
        String serviceType = binding.spServiceType.getSelectedItem().toString();
        String address = binding.etAddress.getText().toString().trim();
        boolean available = binding.switchAvailable.isChecked();

        // 🔥 Fetch name from users collection
        FirebaseManager.getFirestore()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    String name = documentSnapshot.getString("name");

                    // fallback (important safety)
                    if (name == null || name.isEmpty()) {
                        name = FirebaseManager.getAuth().getCurrentUser().getEmail();
                    }

                    Provider provider = new Provider(
                            uid,
                            name,
                            serviceType,
                            address,
                            latitude,
                            longitude,
                            available,
                            0.0
                    );

                    FirebaseManager.getFirestore()
                            .collection("providers")
                            .document(uid)
                            .set(provider)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Profile Saved", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, ProviderDashboardActivity.class));
                                finish();
                            });
                });
    }
    private void getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1001);
            return;
        }

        fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                null
        ).addOnSuccessListener(location -> {

            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            } else {
                Toast.makeText(this,
                        "Unable to fetch location. Turn on GPS.",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1001 &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            getCurrentLocation();
        }
    }



}
