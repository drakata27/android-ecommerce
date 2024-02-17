package com.example.videogamesstore.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.videogamesstore.R;
import com.example.videogamesstore.fragments.CartFragment;
import com.example.videogamesstore.models.Game;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class MainAdapter extends FirebaseRecyclerAdapter<Game, MainAdapter.myViewHolder> {
    private FirebaseUser user;
    private final FragmentManager fragmentManager;
    private final BottomNavigationView bottomNavigationView;


    public MainAdapter(@NonNull FirebaseRecyclerOptions<Game> options, FragmentManager fragmentManager, BottomNavigationView bottomNavigationView) {
        super(options);
        this.fragmentManager = fragmentManager;
        this.bottomNavigationView = bottomNavigationView;
    }

    @Override
    protected void onBindViewHolder(@NonNull myViewHolder holder, int position, @NonNull Game model) {
        holder.name.setText(model.getName());
        holder.platform.setText(model.getPlatform());
        holder.price.setText(String.valueOf(model.getPrice()));
        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("AddToCart");
        DatabaseReference videoGamesRef = FirebaseDatabase.getInstance().getReference("videogames");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        Glide.with(holder.img.getContext())
                .load(model.getImgurl())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .into(holder.img);

        holder.addToCartBtn.setOnClickListener(v -> {
            String userId;

            if (user == null) {
                userId = "anonymousUser";
            } else {
                user.getUid();
                userId = user.getUid();
            }

            Query query = cartRef.orderByChild("name").equalTo(model.getName());

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean itemAlreadyInCart = false;

                    for (DataSnapshot cartItemSnapshot : snapshot.getChildren()) {
                        String userId = cartItemSnapshot.child("userId").getValue(String.class);
                        String itemName = cartItemSnapshot.child("name").getValue(String.class);

                        if (userId != null && itemName != null &&
                                userId.equals(user.getUid()) && itemName.equals(model.getName())) {
                            itemAlreadyInCart = true;
                            break;
                        }
                    }

                    if (itemAlreadyInCart) {
                        Toast.makeText(holder.name.getContext(), model.getName() + " is already in the cart", Toast.LENGTH_SHORT).show();
                    } else {
                        videoGamesRef.orderByChild("name").equalTo(model.getName()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot gameSnapshot : snapshot.getChildren()) {
                                    model.setUserId(userId);
                                    model.setGameId(gameSnapshot.getKey());
                                    String cartId = cartRef.push().getKey();

                                    HashMap<String, Object> cartItems = new HashMap<>();
                                    cartItems.put("gameId", model.getGameId());
                                    cartItems.put("userId", model.getUserId());
                                    cartItems.put("name", model.getName());
                                    cartItems.put("platform", model.getPlatform());
                                    cartItems.put("price", model.getPrice());
                                    cartItems.put("imgurl", String.valueOf(model.getImgurl()));
                                    cartItems.put("qty", model.getQty());
                                    cartItems.put("currQty", model.getCurrQty());

                                    if (cartId != null)
                                        cartRef.child(cartId).setValue(cartItems);

                                    showAlert(holder, model);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.d("videoGames", "" + error.getMessage());
                            }
                        });
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("Firebase", "Error checking cart: " + error.getMessage());
                }
            });
        });
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_layout, parent, false);
        return new myViewHolder(view);
    }

    static class myViewHolder extends RecyclerView.ViewHolder{
        ImageView img;
        TextView name, platform, price, qty;

        ImageButton addToCartBtn;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);

            img = itemView.findViewById(R.id.imageUrl);
            name = itemView.findViewById(R.id.nameText);
            platform = itemView.findViewById(R.id.platformText);
            price = itemView.findViewById(R.id.priceText);
            qty = itemView.findViewById(R.id.qtyText);

            addToCartBtn = itemView.findViewById(R.id.addToCartBtn);
        }
    }

    private void showAlert(@NonNull myViewHolder holder, @NonNull Game model) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(holder.name.getContext());
        alertBuilder.setTitle(model.getName());
        alertBuilder.setMessage(model.getName()+ " was added to cart");
        alertBuilder.setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss());

        alertBuilder.setNeutralButton("Go to Cart", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            goToCart();
        });

        alertBuilder.setIcon(R.drawable.add_to_cart);
        AlertDialog alertDialog = alertBuilder.create();
        alertDialog.show();
    }

    private void goToCart() {
        CartFragment cartFragment = new CartFragment();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout_start, cartFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        bottomNavigationView.setSelectedItemId(R.id.cart);
    }
}
