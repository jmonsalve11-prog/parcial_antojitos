package com.antojitos.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.antojitos.app.data.Fmt;
import com.antojitos.app.data.Wish;
import com.antojitos.app.data.WishRepository;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

/** Modalidad 1: registro de antojos. */
public class WishesFragment extends Fragment {

    private WishRepository repo;
    private WishAdapter adapter;
    private List<Wish> all = new ArrayList<>();
    private int priorityFilter = -1; // -1 = todos
    private TextView amount, subtitle, empty;
    private FloatingActionButton fab;

    public WishesFragment() {
        super(R.layout.fragment_wishes);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repo = WishRepository.get(requireContext());

        ((TextView) view.findViewById(R.id.summaryTitle)).setText("Lo que quieres comprar");
        amount = view.findViewById(R.id.summaryAmount);
        subtitle = view.findViewById(R.id.summarySubtitle);
        empty = view.findViewById(R.id.txtEmpty);
        fab = view.findViewById(R.id.fab);

        RecyclerView recycler = view.findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new WishAdapter(false, new WishAdapter.Listener() {
            @Override
            public void onEdit(Wish wish) {
                startActivity(WishFormActivity.intentFor(requireContext(), wish));
            }

            @Override
            public void onAction(Wish wish) {
                repo.setPurchased(wish, true);
                Snackbar.make(requireView(), "¡Compra registrada! 🎉", Snackbar.LENGTH_LONG)
                        .setAnchorView(fab)
                        .setAction("Deshacer", v -> repo.setPurchased(wish, false))
                        .show();
            }

            @Override
            public void onDelete(Wish wish) {
                repo.delete(wish);
            }
        });
        recycler.setAdapter(adapter);

        fab.setOnClickListener(v -> startActivity(new Intent(requireContext(), WishFormActivity.class)));

        ChipGroup group = view.findViewById(R.id.chipGroupPriority);
        group.setOnCheckedStateChangeListener((g, ids) -> {
            int id = ids.isEmpty() ? R.id.chipAll : ids.get(0);
            if (id == R.id.chipHigh) priorityFilter = 0;
            else if (id == R.id.chipMid) priorityFilter = 1;
            else if (id == R.id.chipLow) priorityFilter = 2;
            else priorityFilter = -1;
            refresh();
        });

        repo.getPending().observe(getViewLifecycleOwner(), list -> {
            all = list;
            refresh();
        });
    }

    private void refresh() {
        if (getView() == null) return;
        List<Wish> shown = new ArrayList<>();
        long total = 0;
        for (Wish w : all) {
            total += w.getPrice();
            if (priorityFilter == -1 || w.getPriority() == priorityFilter) shown.add(w);
        }
        amount.setText(Fmt.cop(total));
        subtitle.setText(all.size() == 1 ? "1 antojo pendiente" : all.size() + " antojos pendientes");
        adapter.submitList(shown);
        empty.setVisibility(shown.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
