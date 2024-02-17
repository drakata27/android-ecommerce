package com.example.videogamesstore.activities;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.videogamesstore.databinding.ActivityRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance();

        binding.backArrow.setOnClickListener(v -> finish());
        binding.signInTextview.setOnClickListener(v -> startActivity(new Intent(RegisterActivity.this, SignInActivity.class)));

        binding.btnRegister.setOnClickListener(v -> {
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

            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                binding.progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "Account created.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                    Toast.makeText(RegisterActivity.this, "Error creating a user.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}