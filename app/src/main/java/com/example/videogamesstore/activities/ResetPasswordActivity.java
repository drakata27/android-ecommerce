package com.example.videogamesstore.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.videogamesstore.databinding.ActivityResetPasswordBinding;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {
    private ActivityResetPasswordBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityResetPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance();

        binding.backArrow.setOnClickListener(v -> finish());

        binding.btnReset.setOnClickListener(v -> {
            String email = String.valueOf(binding.email.getText());

            if (!TextUtils.isEmpty(email)) {
                resetPassword(email);
            } else {
                binding.emailTextInpLay.setError("Email can't be empty");
            }
        });

    }

    private void resetPassword(String email) {
        binding.btnReset.setVisibility(View.INVISIBLE);
        binding.progressBar.setVisibility(View.VISIBLE);

        auth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(ResetPasswordActivity.this, "Password reset email sent.", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(ResetPasswordActivity.this, "Failed to send reset email. Check your email address.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}