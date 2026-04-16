package com.example.reportify.activities;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;

import com.example.reportify.adapters.ProviderAdapter;
import com.example.reportify.databinding.ActivityProviderListBinding;
import com.example.reportify.models.Provider;
import com.example.reportify.utils.FirebaseManager;
import com.example.reportify.utils.PreferenceManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;


import java.util.ArrayList;
import java.util.List;

public class ProviderListActivity extends AppCompatActivity {

    private ActivityProviderListBinding binding;
    private List<Provider> providerList;
    private ProviderAdapter adapter;

    //User Location
    private FusedLocationProviderClient fusedLocationClient;
    private double userLatitude;
    private double userLongitude;
    private double maxDistanceKm;

    private ListenerRegistration providerListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityProviderListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        String serviceName = getIntent().getStringExtra("serviceName");
        if (serviceName == null) serviceName = "Providers";
        setSupportActionBar(binding.toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(serviceName + " Providers");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        PreferenceManager preferenceManager = new PreferenceManager(this);
        maxDistanceKm = preferenceManager.getRadius();


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getUserLocation(serviceName);

        providerList = new ArrayList<>();
        adapter = new ProviderAdapter(this, providerList, provider ->{
            Intent intent = new Intent(this, ComplaintFormActivity.class);
            intent.putExtra("providerId", provider.getUid());
            intent.putExtra("serviceType", provider.getServiceType());
            startActivity(intent);
        });

        binding.recyclerProviders.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerProviders.setAdapter(adapter);

        //fetchProviders(serviceName); No use now
    }
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void getUserLocation(String serviceName) {

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    2001);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {

                    if (location != null) {
                        userLatitude = location.getLatitude();
                        userLongitude = location.getLongitude();
                    }

                    fetchProviders(serviceName);
                });
    }

//Trying to make them offline
//    private void fetchProviders(String serviceName) {
//
//        FirebaseManager.getFirestore()
//                .collection("providers")
//                .whereEqualTo("serviceType", serviceName)
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//
//                    providerList.clear();
//
//                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
//
//                        Provider provider = doc.toObject(Provider.class);
//
//                        if (provider.getLatitude() == 0.0 &&
//                                provider.getLongitude() == 0.0) {
//                            continue; // Skip invalid location
//                        }
//
//                        double distance = calculateDistance(
//                                userLatitude,
//                                userLongitude,
//                                provider.getLatitude(),
//                                provider.getLongitude()
//                        );
//
//                        if (distance <= maxDistanceKm) {
//                            provider.setDistanceKm(distance);
//                            providerList.add(provider);
//                        }
//                    }
//
//                    providerList.sort((p1, p2) ->
//                            Double.compare(p1.getDistanceKm(), p2.getDistanceKm()));
//
//                    adapter.notifyDataSetChanged();
//                });
//    }

    private void fetchProviders(String serviceName) {

        providerListener = FirebaseManager.getFirestore()
                .collection("providers")
                .whereEqualTo("serviceType", serviceName)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {

                    if (e != null) {
                        return;
                    }

                    if (queryDocumentSnapshots == null) {
                        return;
                    }

                    providerList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {

                        Provider provider = doc.toObject(Provider.class);

                        if (provider.getLatitude() == 0.0 &&
                                provider.getLongitude() == 0.0) {
                            continue;
                        }

                        double distance = calculateDistance(
                                userLatitude,
                                userLongitude,
                                provider.getLatitude(),
                                provider.getLongitude()
                        );

                        if (distance <= maxDistanceKm) {
                            provider.setDistanceKm(distance);
                            providerList.add(provider);
                        }
                    }

                    providerList.sort((p1, p2) ->
                            Double.compare(p1.getDistanceKm(), p2.getDistanceKm()));

                    adapter.notifyDataSetChanged();
                });
    }
    private double calculateDistance(double lat1, double lon1,
                                     double lat2, double lon2) {

        final int EARTH_RADIUS = 6371; // km

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) *
                        Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) *
                        Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (providerListener != null) {
            providerListener.remove();
        }
    }

}
