package com.example.videogamesstore.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.videogamesstore.R;
import com.example.videogamesstore.interfaces.CartTotalListener;
import com.example.videogamesstore.models.Game;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class CartAdapter extends FirebaseRecyclerAdapter <Game, CartAdapter.myViewHolder>{
    private double total;
    private int newQty;
    private final CartTotalListener cartTotalListener;
    private final ArrayList<Double> totalList = new ArrayList<>();

    public CartAdapter(@NonNull FirebaseRecyclerOptions<Game> options, CartTotalListener cartTotalListener  ) {
        super(options);
        this.cartTotalListener = cartTotalListener;
    }

    @Override
    protected void onBindViewHolder(@NonNull CartAdapter.myViewHolder holder, int position, @NonNull Game model) {
        holder.name.setText(model.getName());
        holder.platform.setText(model.getPlatform());
        holder.price.setText(String.valueOf(model.getPrice()));
        holder.qty.setText(String.valueOf(model.getCurrQty()));

        double itemTotal = model.getPrice() * model.getCurrQty();
        holder.price.setText(String.format(Locale.UK,"%.2f", itemTotal));
        newQty = Integer.parseInt(holder.qty.getText().toString());

        totalList.add(itemTotal);
        total = calculateTotal(totalList);
        updateTotal(total);

        DatabaseReference cartItems = FirebaseDatabase.getInstance().getReference().child("AddToCart")
                .child(Objects.requireNonNull(getRef(position).getKey()));


        Glide.with(holder.img.getContext())
                .load(model.getImgurl())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .into(holder.img);

        holder.removeItemBtn.setOnClickListener(v -> removeFromCart(cartItems, holder));

        holder.incrementQty.setOnClickListener(v -> {
            newQty = Integer.parseInt(holder.qty.getText().toString());
            if (model.getQty() > newQty) {
                totalList.remove(Double.parseDouble(holder.price.getText().toString()));
                newQty++;

                holder.qty.setText(String.valueOf(newQty));
                cartItems.child("currQty").setValue(newQty);

                total = calculateTotal(totalList);
            }
        });

        holder.decrementQty.setOnClickListener(v -> {
            newQty = Integer.parseInt(holder.qty.getText().toString());

            if (newQty > 1) {
                totalList.remove(Double.parseDouble(holder.price.getText().toString()));
                newQty--;

                holder.qty.setText(String.valueOf(newQty));
                cartItems.child("currQty").setValue(newQty);
                total = calculateTotal(totalList);

            } else
                removeFromCart(cartItems, holder);
        });
    }

    @NonNull
    @Override
    public CartAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_recycler_layout, parent, false);
        return new CartAdapter.myViewHolder(view);
    }

    static class myViewHolder extends RecyclerView.ViewHolder{
        ImageView img;
        TextView name, platform, price, qty;

        ImageButton removeItemBtn, decrementQty, incrementQty;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);

            img = itemView.findViewById(R.id.imageUrl);
            name = itemView.findViewById(R.id.nameText);
            platform = itemView.findViewById(R.id.platformText);
            price = itemView.findViewById(R.id.priceText);
            qty = itemView.findViewById(R.id.qtyText);

            removeItemBtn = itemView.findViewById(R.id.removeItemBtn);
            incrementQty = itemView.findViewById(R.id.qtyIncrementBtn);
            decrementQty = itemView.findViewById(R.id.qtyDecrementBtn);
        }
    }

    private void removeFromCart(DatabaseReference cartItems, @NonNull myViewHolder holder) {
        totalList.remove(Double.parseDouble(holder.price.getText().toString()));


        cartItems.removeValue();
        Toast.makeText(holder.name.getContext(),
                holder.name.getText().toString() + " was removed from cart ", Toast.LENGTH_SHORT).show();

        total = calculateTotal(totalList);
        updateTotal(total);
    }


    private void updateTotal(double total) {
        if (cartTotalListener != null) {
            cartTotalListener.onCartTotalUpdated(total);
        }
    }

    private double calculateTotal(ArrayList<Double> totalList){
        double total = 0;
        for (double itemTotal : totalList) {
            total += itemTotal;
        }
        return total;
    }

    public double getTotal() {
        return total;
    }
}
