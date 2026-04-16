package com.example.reportify.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.reportify.databinding.ActivityRegisterBinding;
import com.example.reportify.models.User;
import com.example.reportify.utils.FirebaseManager;
import com.example.reportify.R;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String[] roles = {"USER", "PROVIDER"};
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, R.layout.item_spinner_dropdown, roles);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        binding.spRole.setAdapter(adapter);

        binding.btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {

        String name = binding.etName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String role = binding.spRole.getSelectedItem().toString();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);

        FirebaseManager.getAuth()
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        String uid = FirebaseManager.getAuth().getCurrentUser().getUid();
                        User user = new User(uid, name, email, role);

                        FirebaseManager.getFirestore()
                                .collection("users")
                                .document(uid)
                                .set(user)
                                .addOnSuccessListener(unused -> {
                                    binding.progressBar.setVisibility(View.GONE);
                                    Toast.makeText(this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                                    finish();
                                });

                    } else {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(this,
                                task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
