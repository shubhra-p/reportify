package com.example.reportify.fragments;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.database.Cursor;
import android.widget.EditText;
import android.widget.Toast;
import android.app.AlertDialog;

import com.example.reportify.models.Complaint;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.auth.UserProfileChangeRequest;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;

import java.io.File;

import com.example.reportify.activities.LoginActivity;
import com.example.reportify.databinding.FragmentProfileBinding;
import com.example.reportify.utils.FirebaseManager;
import com.example.reportify.provider.ComplaintContract;


public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private Uri imageUri;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentProfileBinding.inflate(inflater, container, false);



        String email = FirebaseManager.getAuth().getCurrentUser().getEmail();
        binding.tvEmail.setText(email);


        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                result -> {
                    if (result) {
                        binding.ivProfile.setImageURI(imageUri);

                        requireContext()
                                .getSharedPreferences("profile_prefs", 0)
                                .edit()
                                .putString("profile_image", imageUri.toString())
                                .apply();
                    }
                }
        );


        binding.btnLogout.setOnClickListener(v -> {

            FirebaseManager.getAuth().signOut();

            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
        });
        binding.btnShareComplaint.setOnClickListener(v -> shareLatestComplaint());
        binding.btnChangePhoto.setOnClickListener(v -> openCamera());
        binding.tvName.setOnClickListener(v -> showEditNameDialog());

        //NameLoadingLogic
        FirebaseUser user = FirebaseManager.getAuth().getCurrentUser();

        if (user != null) {

            String uid = user.getUid();

            FirebaseManager.getFirestore()
                    .collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {

                        if (documentSnapshot.exists()) {

                            String name = documentSnapshot.getString("name");

                            if (name != null) {
                                binding.tvName.setText(name);

                                // Cache locally
                                requireContext()
                                        .getSharedPreferences("profile_prefs", 0)
                                        .edit()
                                        .putString("profile_name", name)
                                        .apply();
                            }
                        } else {
                            binding.tvName.setText("User");
                        }
                    });
        }

        // Loading saved profile image
        File file = new File(requireContext().getFilesDir(), "profile/profile.jpg");

        if (file.exists()) {
            binding.ivProfile.setImageURI(
                    Uri.fromFile(file)
            );
        }

        loadAnalytics();

        return binding.getRoot();
    }

    private void shareLatestComplaint() {

        /* Sharing Any User (Unsafe)
        Cursor cursor = requireContext().getContentResolver().query(
                ComplaintContract.ComplaintEntry.CONTENT_URI,
                null,
                null,
                null,
                "timestamp DESC"
        );*/
        Cursor cursor = requireContext().getContentResolver().query(
                ComplaintContract.ComplaintEntry.CONTENT_URI,
                null,
                "userId=?",
                new String[]{FirebaseManager.getAuth().getCurrentUser().getUid()},
                "timestamp DESC"
        );

        if (cursor == null || !cursor.moveToFirst()) {
            Toast.makeText(getContext(),
                    "No complaints to share",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String title = cursor.getString(
                cursor.getColumnIndexOrThrow("title"));

        String description = cursor.getString(
                cursor.getColumnIndexOrThrow("description"));

        String status = cursor.getString(
                cursor.getColumnIndexOrThrow("status"));

        String urgency = cursor.getString(
                cursor.getColumnIndexOrThrow("urgency"));

        cursor.close();

        String complaintText =
                "Complaint Details\n\n" +
                        "Title: " + title + "\n" +
                        "Description: " + description + "\n" +
                        "Status: " + status + "\n" +
                        "Urgency: " + urgency + "\n\n" +
                        "Shared via Smart Complaint App";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, complaintText);

        startActivity(Intent.createChooser(
                shareIntent,
                "Share Complaint"
        ));
    }

    private void openCamera() {

        try {
            // Create profile directory inside internal storage
            File directory = new File(requireContext().getFilesDir(), "profile");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Create file inside that directory
            File imageFile = new File(directory, "profile.jpg");

            imageUri = FileProvider.getUriForFile(
                    requireContext(),
                    "com.example.reportify.fileprovider",
                    imageFile
            );

            cameraLauncher.launch(imageUri);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showEditNameDialog() {

        final EditText editText = new EditText(requireContext());
        editText.setHint("Enter your name");
        editText.setPadding(40, 40, 40, 40);

        new AlertDialog.Builder(requireContext())
                .setTitle("Edit Name")
                .setView(editText)
                .setPositiveButton("Save", (dialog, which) -> {

                    String newName = editText.getText().toString().trim();

                    if (newName.isEmpty()) {
                        Toast.makeText(getContext(),
                                "Name cannot be empty",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    updateUserName(newName);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void updateUserName(String newName) {

        FirebaseUser user = FirebaseManager.getAuth().getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();

        // Update Firestore
        FirebaseManager.getFirestore()
                .collection("users")
                .document(uid)
                .update("name", newName)
                .addOnSuccessListener(unused -> {

                    binding.tvName.setText(newName);

                    requireContext()
                            .getSharedPreferences("profile_prefs", 0)
                            .edit()
                            .putString("profile_name", newName)
                            .apply();

                    Toast.makeText(getContext(),
                            "Name updated successfully",
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Update failed",
                                Toast.LENGTH_SHORT).show()
                );
    }
    private void loadAnalytics() {

        String userId = FirebaseManager.getAuth().getCurrentUser().getUid();

        FirebaseManager.getFirestore()
                .collection("complaints")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    int total = 0;
                    int pending = 0;
                    int inProgress = 0;
                    int resolved = 0;

                    float totalRating = 0;
                    int ratingCount = 0;

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {

                        Complaint complaint = doc.toObject(Complaint.class);
                        if (complaint == null) continue;

                        total++;

                        String status = complaint.getStatus();

                        if ("PENDING".equals(status)) {
                            pending++;
                        } else if ("IN_PROGRESS".equals(status)) {
                            inProgress++;
                        } else if ("RESOLVED".equals(status)) {
                            resolved++;
                        }

                        // rating (if exists)
                        if (complaint.getRating() > 0) {
                            totalRating += complaint.getRating();
                            ratingCount++;
                        }
                    }

                    float avgRating = ratingCount == 0 ? 0 : totalRating / ratingCount;

                    // Update UI
                    binding.tvTotal.setText(String.valueOf(total));
                    binding.tvPending.setText(String.valueOf(pending));
                    binding.tvInProgress.setText(String.valueOf(inProgress));
                    binding.tvResolved.setText(String.valueOf(resolved));
                    binding.tvAvgRating.setText(String.format("%.1f ⭐", avgRating));

                });
    }

}
