package com.antojitos.app;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.antojitos.app.data.Cat;
import com.antojitos.app.data.Categories;
import com.antojitos.app.data.Fmt;
import com.antojitos.app.data.Wish;
import com.antojitos.app.data.WishRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

/** Modalidad 2: control de ahorros (compras realizadas, filtrables). */
public class SavingsFragment extends Fragment {

    private WishRepository repo;
    private WishAdapter adapter;
    private List<Wish> all = new ArrayList<>();
    private String category = null; // null = todas
    private TextView amount, subtitle, empty;
    private TextInputEditText inputMin, inputMax;
    private MaterialButton btnClear;
    private Chip chipAll;

    public SavingsFragment() {
        super(R.layout.fragment_savings);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repo = WishRepository.get(requireContext());

        amount = view.findViewById(R.id.summaryAmount);
        subtitle = view.findViewById(R.id.summarySubtitle);
        empty = view.findViewById(R.id.txtEmpty);
        inputMin = view.findViewById(R.id.inputMin);
        inputMax = view.findViewById(R.id.inputMax);
        btnClear = view.findViewById(R.id.btnClear);

        RecyclerView recycler = view.findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new WishAdapter(true, new WishAdapter.Listener() {
            @Override
            public void onEdit(Wish wish) {
            }

            @Override
            public void onAction(Wish wish) {
                repo.setPurchased(wish, false);
            }

            @Override
            public void onDelete(Wish wish) {
                repo.delete(wish);
            }
        });
        recycler.setAdapter(adapter);

        // Chips de categoría
        ChipGroup group = view.findViewById(R.id.chipGroupCategory);
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        chipAll = newChip(inflater, group, "Todas", null);
        chipAll.setChecked(true);
        for (Cat c : Categories.ALL) {
            newChip(inflater, group, c.getEmoji() + " " + c.getName(), c.getName());
        }
        group.setOnCheckedStateChangeListener((g, ids) -> {
            if (ids.isEmpty()) {
                category = null;
            } else {
                Chip chip = g.findViewById(ids.get(0));
                category = (String) chip.getTag();
            }
            applyFilters();
        });

        // Rango de precios
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                applyFilters();
            }
        };
        inputMin.addTextChangedListener(watcher);
        inputMax.addTextChangedListener(watcher);

        btnClear.setOnClickListener(v -> {
            chipAll.setChecked(true);
            inputMin.setText("");
            inputMax.setText("");
        });

        repo.getBought().observe(getViewLifecycleOwner(), list -> {
            all = list;
            applyFilters();
        });
    }

    private Chip newChip(LayoutInflater inflater, ChipGroup group, String text, String tag) {
        Chip chip = (Chip) inflater.inflate(R.layout.chip_filter, group, false);
        chip.setId(View.generateViewId());
        chip.setText(text);
        chip.setTag(tag);
        group.addView(chip);
        return chip;
    }

    private static Long parse(TextInputEditText e) {
        if (e.getText() == null || e.getText().length() == 0) return null;
        try {
            return Long.parseLong(e.getText().toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void applyFilters() {
        if (getView() == null) return;
        Long min = parse(inputMin);
        Long max = parse(inputMax);
        List<Wish> out = new ArrayList<>();
        long total = 0;
        for (Wish w : all) {
            if (category != null && !category.equals(w.getCategory())) continue;
            if (min != null && w.getPrice() < min) continue;
            if (max != null && w.getPrice() > max) continue;
            out.add(w);
            total += w.getPrice();
        }
        boolean active = category != null || min != null || max != null;

        ((TextView) requireView().findViewById(R.id.summaryTitle))
                .setText(active ? "Total consumido (filtrado)" : "Total consumido");
        amount.setText(Fmt.cop(total));
        String count = out.size() == 1 ? "1 compra" : out.size() + " compras";
        subtitle.setText(count + (active ? " con los filtros aplicados" : " registradas"));
        btnClear.setVisibility(active ? View.VISIBLE : View.INVISIBLE);

        adapter.submitList(out);
        if (out.isEmpty()) {
            empty.setVisibility(View.VISIBLE);
            empty.setText(all.isEmpty()
                    ? "🎉\n\nAún no has comprado nada\nCuando marques un antojo como comprado, aparecerá aquí."
                    : "🔍\n\nSin resultados\nNinguna compra coincide con los filtros.");
        } else {
            empty.setVisibility(View.GONE);
        }
    }
}
