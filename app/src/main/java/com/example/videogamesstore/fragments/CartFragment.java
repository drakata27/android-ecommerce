package com.example.videogamesstore.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.braintreepayments.cardform.view.CardForm;
import com.example.videogamesstore.R;
import com.example.videogamesstore.adapters.CartAdapter;
import com.example.videogamesstore.databinding.FragmentCartBinding;
import com.example.videogamesstore.interfaces.CartTotalListener;
import com.example.videogamesstore.models.Game;
import com.example.videogamesstore.models.Order;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class CartFragment extends Fragment implements CartTotalListener {

    private FragmentCartBinding binding;
    private CartAdapter cartAdapter;
    private FirebaseUser user;
    private String currentDateTime;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentCartBinding.inflate(inflater, container, false);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();
        currentDateTime = currentDate.toString();

        FirebaseRecyclerOptions<Game> options =
                new FirebaseRecyclerOptions.Builder<Game>()
                        .setQuery(FirebaseDatabase.getInstance()
                        .getReference().child("AddToCart")
                        .orderByChild("userId")
                        .equalTo(user.getUid()), Game.class)
                        .build();

        cartAdapter = new CartAdapter(options, this);
        binding.recyclerView.setAdapter(cartAdapter);

        binding.totalTxt.postDelayed(() -> binding.totalTxt.setText("Total: £"
                + String.format(Locale.UK, "%.2f", cartAdapter.getTotal())), 300);

        binding.checkoutBtn.setOnClickListener(v -> {
            if(user == null ) {
                Toast.makeText(requireContext(), "Sign in to proceed", Toast.LENGTH_SHORT).show();
            } else {
                if(cartAdapter.getTotal() == 0) {
                    Toast.makeText(requireContext(), "You don't have any items in your cart", Toast.LENGTH_SHORT).show();
                    binding.totalTxt.setText("Total: £" + String.format(Locale.UK, "%.2f", cartAdapter.getTotal()));
                } else {
                    binding.totalTxt.setText("Total: £" + String.format(Locale.UK, "%.2f", cartAdapter.getTotal()));
                    showCheckout();
                }
            }
        });
        return binding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    private void showCheckout() {
        if (getContext() != null) {

            final DialogPlus dialogPlus = createCheckoutDialog();

            View view = dialogPlus.getHolderView();
            view.setPadding(26, 16, 26, 16);

            CardForm cardForm = view.findViewById(R.id.card_form);
            Button payBtn = view.findViewById(R.id.pay_btn);

            cardForm.cardRequired(true)
                    .expirationRequired(true)
                    .cvvRequired(true)
                    .postalCodeRequired(true)
                    .setup(getActivity());

            cardForm.getCvvEditText().setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);

            payBtn.setText(String.format(Locale.UK, "Pay £%.2f", cartAdapter.getTotal()));

            dialogPlus.show();

            payBtn.setOnClickListener(v -> processPayment(cardForm, dialogPlus));
        }
    }

    private DialogPlus createCheckoutDialog() {
        DialogPlus dialogPlus = DialogPlus.newDialog(getContext())
                .setContentHolder(new ViewHolder(R.layout.checkout_popup))
                .create();

        // Calculate 2/3 of the screen height
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenHeight = displayMetrics.heightPixels;
        int dialogHeight = (int) (screenHeight * (2.0 / 3.0));

        View contentView = dialogPlus.getHolderView();
        ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
        layoutParams.height = dialogHeight;

        contentView.setLayoutParams(layoutParams);
        dialogPlus.show();

        return dialogPlus;
    }

    private void processPayment(CardForm cardForm, DialogPlus dialogPlus) {
        if (cardForm.isValid() && getContext() != null) {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getContext());
            alertBuilder.setTitle("Confirm before purchase");
            alertBuilder.setMessage("Card number: " + cardForm.getCardNumber() + "\n" +
                    "Card expiry date: " + Objects.requireNonNull(cardForm.getExpirationDateEditText().getText()) + "\n" +
                    "Card CVV: " + cardForm.getCvv() + "\n" +
                    "Postal code: " + cardForm.getPostalCode());

            alertBuilder.setPositiveButton("Confirm", (dialogInterface, i) -> {
                dialogInterface.dismiss();
                Toast.makeText(getContext(), "Your order was placed", Toast.LENGTH_LONG).show();
                processOrder(cardForm.getPostalCode());
                clearCart();
                dialogPlus.dismiss();
            });
            alertBuilder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());
            AlertDialog alertDialog = alertBuilder.create();
            alertDialog.show();
        } else {
            Toast.makeText(getContext(), "Please complete the form", Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("SetTextI18n")
    private void clearCart() {
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference cartReference = FirebaseDatabase.getInstance().getReference().child("AddToCart");

            Query userCartQuery = cartReference.orderByChild("userId").equalTo(userId);

            userCartQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot cartItemSnapshot : dataSnapshot.getChildren()) {
                        cartItemSnapshot.getRef().removeValue();
                    }
                    Log.d("Clear cart","Cart items removed for the current user");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("Firebase", "Error removing cart items: " + databaseError.getMessage());
                }
            });
        }

        binding.totalTxt.setText("Total: £0.00");
    }

    private void processOrder(String postCode) {
        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("AddToCart");
        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("Orders");
        DatabaseReference videogamesRef = FirebaseDatabase.getInstance().getReference("videogames");

        Query userCartQuery = cartRef.orderByChild("userId").equalTo(user.getUid());
        userCartQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot cartItemSnapshot : snapshot.getChildren()) {
                    String orderId = cartRef.push().getKey();
                    String userId = user.getUid();
                    String gameId = cartItemSnapshot.child("gameId").getValue(String.class);
                    String userEmail = user.getEmail();
                    String name = cartItemSnapshot.child("name").getValue(String.class);
                    String platform = cartItemSnapshot.child("platform").getValue(String.class);
                    String imgurl = cartItemSnapshot.child("imgurl").getValue(String.class);
                    int currQty = cartItemSnapshot.child("currQty").getValue(Integer.class);
                    double price = cartItemSnapshot.child("price").getValue(Double.class);

                    Order order = new Order(userId, userEmail, gameId, name, currQty, price,
                            platform, postCode, currentDateTime, imgurl);

                    if (orderId != null) {
                        orderRef.child(orderId).setValue(order);
                        updateQty(videogamesRef, currQty, gameId);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error processing order", Toast.LENGTH_SHORT).show();
                Log.d("Process Order", "Error processing order: " + error.getMessage());
            }
        });
    }

    private void updateQty(DatabaseReference reference, int currQty, String gameId) {
        reference.child(gameId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int totalQty = snapshot.child("qty").getValue(Integer.class);
                    int newQty = totalQty - currQty;
                    snapshot.child("qty").getRef().setValue(newQty);
                } else {
                    Log.d("updateQty", "Game with ID " + gameId + " not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("updateQty", "Error updatingQty: " + error.getMessage());
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        cartAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        cartAdapter.stopListening();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onCartTotalUpdated(double total) {
        binding.totalTxt.setText("Total: £" + String.format(Locale.UK, "%.2f", total));
    }
}
