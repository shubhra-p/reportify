package com.example.reportify.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.reportify.databinding.ActivityLoginBinding;
import com.example.reportify.utils.FirebaseManager;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Already Logged-IN
        if (FirebaseManager.getAuth().getCurrentUser() != null) {

            String uid = FirebaseManager.getAuth().getCurrentUser().getUid();

            FirebaseManager.getFirestore()
                    .collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String role = documentSnapshot.getString("role");
                        redirectUser(role);
                    });
        }

        binding.btnLogin.setOnClickListener(v -> loginUser());

        binding.tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );
        binding.btnSignup.setOnClickListener(v->
                startActivity(new Intent(this, RegisterActivity.class))
        );
    }

    private void loginUser() {

        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);

        FirebaseManager.getAuth()
                .signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    binding.progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {

                        String uid = FirebaseManager.getAuth().getCurrentUser().getUid();

                        FirebaseManager.getFirestore()
                                .collection("users")
                                .document(uid)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {

                                    String role = documentSnapshot.getString("role");

                                    redirectUser(role);
                                });

                    } else {
                        Toast.makeText(this,
                                task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void redirectUser(String role) {

        String uid = FirebaseManager.getAuth().getCurrentUser().getUid();

        if ("PROVIDER".equals(role)) {

            FirebaseManager.getFirestore()
                    .collection("providers")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {

                        if (documentSnapshot.exists()) {
                            // Provider profile already created
                            startActivity(new Intent(this, ProviderDashboardActivity.class));
                        } else {
                            // Provider profile not created
                            startActivity(new Intent(this, ProviderSetupActivity.class));
                        }

                        finish();
                    });

        } else {
            // Normal user
            startActivity(new Intent(this, UserDashboardActivity.class));
            finish();
        }
    }

}

