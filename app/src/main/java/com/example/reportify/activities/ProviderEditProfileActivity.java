package com.example.reportify.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.reportify.databinding.ActivityProviderEditProfileBinding;
import com.example.reportify.models.Provider;
import com.example.reportify.utils.FirebaseManager;

public class ProviderEditProfileActivity extends AppCompatActivity {

    private ActivityProviderEditProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityProviderEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadData();

        binding.btnSave.setOnClickListener(v -> saveData());
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void loadData() {

        String uid = FirebaseManager.getAuth().getCurrentUser().getUid();

        FirebaseManager.getFirestore()
                .collection("providers")
                .document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (snapshot.exists()) {

                        Provider provider = snapshot.toObject(Provider.class);

                        if (provider != null) {
                            binding.etName.setText(provider.getName());
                            binding.etAddress.setText(provider.getAddress());
                            binding.switchAvailable.setChecked(provider.isAvailable());
                        }
                    }
                });
    }

    private void saveData() {

        String uid = FirebaseManager.getAuth().getCurrentUser().getUid();

        String name = binding.etName.getText().toString().trim();
        String address = binding.etAddress.getText().toString().trim();
        boolean available = binding.switchAvailable.isChecked();

        FirebaseManager.getFirestore()
                .collection("providers")
                .document(uid)
                .update(
                        "name", name,
                        "address", address,
                        "available", available
                )
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
                );
    }
}