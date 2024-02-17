package com.example.videogamesstore.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.videogamesstore.R;
import com.example.videogamesstore.models.Order;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OrderAdapter extends FirebaseRecyclerAdapter<Order, OrderAdapter.myViewHolder> {
    public OrderAdapter(@NonNull FirebaseRecyclerOptions<Order> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull OrderAdapter.myViewHolder holder, int position, @NonNull Order model) {
        holder.name.setText(model.getName());
        holder.platform.setText(model.getPlatform());
        holder.price.setText(String.valueOf(model.getPrice()));
        holder.qty.setText(String.valueOf(model.getQty()));

        // Parse the date string into a Date object
        SimpleDateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
        Date date;
        try {
            date = inputFormat.parse(model.getOrderDateTime());
        } catch (ParseException e) {
            e.printStackTrace();
            return; // Handle the parse exception or return if unable to parse
        }

        // Format date and time
        SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd yyyy", Locale.getDefault());
        if (date != null) {
            String formattedDateTime = outputFormat.format(date);
            holder.dateTime.setText(formattedDateTime);
        }

        Glide.with(holder.img.getContext())
                .load(model.getImgurl())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .into(holder.img);
    }

    @NonNull
    @Override
    public OrderAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_orders_recycler_view_layout, parent, false);
        return new OrderAdapter.myViewHolder(view);
    }

    static class myViewHolder extends RecyclerView.ViewHolder{
        ImageView img;
        TextView name, platform, price, qty, dateTime;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imageUrl);
            name = itemView.findViewById(R.id.nameText);
            platform = itemView.findViewById(R.id.platformText);
            price = itemView.findViewById(R.id.priceText);
            qty = itemView.findViewById(R.id.qtyTextView);
            dateTime = itemView.findViewById(R.id.dateOrderedTextView);
        }
    }
}
