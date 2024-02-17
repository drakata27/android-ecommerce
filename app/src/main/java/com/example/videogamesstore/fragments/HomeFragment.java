package com.example.videogamesstore.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.videogamesstore.R;
import com.example.videogamesstore.activities.MainActivity;
import com.example.videogamesstore.adapters.MainAdapter;
import com.example.videogamesstore.databinding.FragmentHomeBinding;
import com.example.videogamesstore.models.Game;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.FirebaseDatabase;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private MainAdapter mainAdapter;
    private BottomNavigationView bottomNavigationView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setHasOptionsMenu(true);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        FirebaseRecyclerOptions<Game> options =
                new FirebaseRecyclerOptions.Builder<Game>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("videogames")
                        .orderByChild("qty")
                        .startAt(1), Game.class)
                        .build();

        MainActivity activity = (MainActivity) getActivity();

        if (activity!=null) {
            bottomNavigationView = activity.findViewById(R.id.bottomNavigationView);
        }

        mainAdapter = new MainAdapter(options, getParentFragmentManager(), bottomNavigationView);
        binding.recyclerView.setAdapter(mainAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        mainAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        mainAdapter.stopListening();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        binding.textViewHome.setVisibility(View.VISIBLE);

        inflater.inflate(R.menu.search, menu);
        MenuItem item = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) item.getActionView();

        assert searchView != null;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                txtSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (query.isEmpty()) {
                    binding.textViewHome.setVisibility(View.VISIBLE);
                    int marginTopInDp = 55; // Change this value as needed

                    float density = getResources().getDisplayMetrics().density;
                    int marginTopInPixels = (int) (marginTopInDp * density);

                    ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) binding.recyclerView.getLayoutParams();
                    layoutParams.topMargin = marginTopInPixels;
                    binding.recyclerView.setLayoutParams(layoutParams);
                } else {
                    binding.textViewHome.setVisibility(View.GONE);
                    // Remove top margin from recyclerView
                    ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) binding.recyclerView.getLayoutParams();
                    layoutParams.topMargin = 0;
                    binding.recyclerView.setLayoutParams(layoutParams);
                    txtSearch(query);
                }
                return false;
            }

        });

        searchView.setOnCloseListener(() -> {
            binding.textViewHome.setVisibility(View.VISIBLE);
            int marginTopInDp = 55;

            float density = getResources().getDisplayMetrics().density;
            int marginTopInPixels = (int) (marginTopInDp * density);

            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) binding.recyclerView.getLayoutParams();
            layoutParams.topMargin = marginTopInPixels;
            binding.recyclerView.setLayoutParams(layoutParams);

            FirebaseRecyclerOptions<Game> options =
                    new FirebaseRecyclerOptions.Builder<Game>()
                            .setQuery(FirebaseDatabase.getInstance().getReference().child("videogames")
                                    .orderByChild("qty")
                                    .startAt(1), Game.class)
                            .build();

            mainAdapter = new MainAdapter(options, getParentFragmentManager(), bottomNavigationView);
            mainAdapter.startListening();
            binding.recyclerView.setAdapter(mainAdapter);

            return false;
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void txtSearch(String str) {
        binding.textViewHome.setVisibility(View.GONE);

        FirebaseRecyclerOptions<Game> options =
                new FirebaseRecyclerOptions.Builder<Game>()
                        .setQuery(FirebaseDatabase
                                .getInstance()
                                .getReference().
                                child("videogames").
                                orderByChild("name").
                                startAt(str)
                                .endAt(str + "~"), Game.class).build();

        mainAdapter = new MainAdapter(options, getParentFragmentManager(), bottomNavigationView);
        mainAdapter.startListening();
        binding.recyclerView.setAdapter(mainAdapter);
    }
}
