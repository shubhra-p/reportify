package com.example.reportify.activities;


import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.example.reportify.R;
import com.example.reportify.database.ComplaintDbHelper;
import com.google.firebase.firestore.DocumentReference;
import com.example.reportify.databinding.ActivityComplaintFormBinding;
import com.example.reportify.models.Complaint;
import com.example.reportify.utils.FirebaseManager;

public class ComplaintFormActivity extends AppCompatActivity {

    private ActivityComplaintFormBinding binding;
    private ComplaintDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityComplaintFormBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Complaint Form");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbHelper = new ComplaintDbHelper(this);

        binding.btnSubmit.setOnClickListener(v -> submitComplaint());
    }
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void submitComplaint() {

        String title = binding.etTitle.getText().toString().trim();
        String description = binding.etDescription.getText().toString().trim();
        int selectedId = binding.rgUrgency.getCheckedRadioButtonId();


        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show();
            return;
        }
        String urgency;

        if (selectedId == R.id.rbLow) {
            urgency = "LOW";
        } else if (selectedId == R.id.rbMedium) {
            urgency = "MEDIUM";
        } else if (selectedId == R.id.rbHigh) {
            urgency = "HIGH";
        } else {
            Toast.makeText(this, "Select urgency level", Toast.LENGTH_SHORT).show();
            return;
        }

        String providerId = getIntent().getStringExtra("providerId");
        String serviceType = getIntent().getStringExtra("serviceType");
        String userId = FirebaseManager.getAuth().getCurrentUser().getUid();


        DocumentReference ref = FirebaseManager.getFirestore()
                .collection("complaints").document();

        String complaintId = ref.getId();

        Complaint complaint = new Complaint(
            complaintId,
            userId,
            providerId,
            serviceType,
            title,
            description,
            "PENDING",
            urgency,
            System.currentTimeMillis()
        );

        dbHelper.insertComplaint(complaint);

        if (isInternetAvailable()) {

            ref.set(complaint)
                    .addOnSuccessListener(unused -> {
                        dbHelper.markAsSynced(complaintId);
                        Toast.makeText(this,
                                "Complaint Submitted",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }).addOnFailureListener(e -> {
                        // If the upload fails for some reason, leave it as unsynced
                        Toast.makeText(this,
                                "Saved offline. Will sync later.",
                                Toast.LENGTH_LONG).show();
                        finish();
                    });

        } else {

            Toast.makeText(this,
                    "Saved offline. Will sync when internet is available.",
                    Toast.LENGTH_LONG).show();

            finish();
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

}
