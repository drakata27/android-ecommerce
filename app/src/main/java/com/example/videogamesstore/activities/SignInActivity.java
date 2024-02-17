package com.example.videogamesstore.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.videogamesstore.databinding.ActivitySignInBinding;
import com.google.firebase.auth.FirebaseAuth;

public class SignInActivity extends AppCompatActivity {
    private ActivitySignInBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance(); // Initialise an instance of the FirebaseAuth class

        binding.registerTextview.setOnClickListener(v -> startActivity(new Intent(SignInActivity.this, RegisterActivity.class)));
        binding.resetPasswordTextview.setOnClickListener(v -> startActivity(new Intent(SignInActivity.this, ResetPasswordActivity.class)));

        binding.signInBtn.setOnClickListener(v -> {
            binding.progressBar.setVisibility(View.VISIBLE);
            String email, password;
            email = String.valueOf(binding.email.getText());
            password = String.valueOf(binding.password.getText());

            if (TextUtils.isEmpty(email)) {
                binding.emailTextInpLay.setError("Email cannot be empty");
                binding.progressBar.setVisibility(View.GONE);
                return;
            }

            if (TextUtils.isEmpty(password)) {
                binding.passwordTextInpLay.setError("Password cannot be empty");
                binding.progressBar.setVisibility(View.GONE);
                return;
            }

            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                binding.progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    Toast.makeText(SignInActivity.this, "Login successful.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(SignInActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}