package com.example.videogamesstore.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.videogamesstore.R;
import com.example.videogamesstore.activities.SignInActivity;
import com.example.videogamesstore.adapters.OrderAdapter;
import com.example.videogamesstore.databinding.FragmentAccountBinding;
import com.example.videogamesstore.models.Order;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

public class AccountFragment extends Fragment {
    private OrderAdapter orderAdapter;
    private FirebaseAuth auth;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        com.example.videogamesstore.databinding.FragmentAccountBinding binding = FragmentAccountBinding.inflate(inflater, container, false);
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user !=null) {
            binding.userTextview.setText(user.getEmail());
        }

        binding.signOutBtn.setOnClickListener(v -> {
            signOut();
            Toast.makeText(getContext(), "You are signed out", Toast.LENGTH_SHORT).show();
        });

        binding.resetPasswordBtn.setOnClickListener(v -> {
            String email = auth.getCurrentUser().getEmail();

            if (email!=null) {
                auth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Password reset email sent.", Toast.LENGTH_SHORT).show();
                        signOut();
                    } else {
                        Toast.makeText(getContext(), "Failed to reset password.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        });

        binding.myOrdersBtn.setOnClickListener(v -> showOrders());

        assert user != null;
        FirebaseRecyclerOptions<Order> options =
                new FirebaseRecyclerOptions.Builder<Order>()
                        .setQuery(FirebaseDatabase.getInstance()
                                .getReference().child("Orders")
                                .orderByChild("userId")
                                .equalTo(user.getUid()), Order.class).build();


        orderAdapter = new OrderAdapter(options);
        return binding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getActivity(), SignInActivity.class);
        startActivity(intent);
        if (getActivity()!=null){
            getActivity().finish();
        }
    }

    private void showOrders() {
        if (getContext()!= null) {

            final DialogPlus dialogPlus = DialogPlus.newDialog(getContext())
                    .setContentHolder(new ViewHolder(R.layout.my_orders_popup))
                    .create();

            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

            int screenHeight = displayMetrics.heightPixels;
            int dialogHeight = (int) (screenHeight * (2.0 / 3.0));

            View contentView = dialogPlus.getHolderView();
            ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
            layoutParams.height = dialogHeight;

            contentView.setLayoutParams(layoutParams);

            RecyclerView recyclerView = contentView.findViewById(R.id.myOrdersRecyclerView);
            recyclerView.setAdapter(orderAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

            dialogPlus.show();

            View view = dialogPlus.getHolderView();
            view.setPadding(26, 16, 26, 16);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        orderAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        orderAdapter.stopListening();
    }
}