package com.antojitos.app;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.antojitos.app.data.Categories;
import com.antojitos.app.data.Fmt;
import com.antojitos.app.data.Priority;
import com.antojitos.app.data.Wish;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Adaptador de la lista. Con boughtMode = false pinta antojos pendientes
 * y con boughtMode = true pinta compras realizadas.
 */
public class WishAdapter extends ListAdapter<Wish, WishAdapter.VH> {

    public interface Listener {
        void onEdit(Wish wish);

        /** Pendiente: "Ya lo compré". Comprado: "Volver a pendientes". */
        void onAction(Wish wish);

        void onDelete(Wish wish);
    }

    private static final int COLOR_CATEGORY = 0xFF00A99D;

    private static final DiffUtil.ItemCallback<Wish> DIFF = new DiffUtil.ItemCallback<Wish>() {
        @Override
        public boolean areItemsTheSame(@NonNull Wish a, @NonNull Wish b) {
            return a.getId() == b.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Wish a, @NonNull Wish b) {
            return a.equals(b);
        }
    };

    private final boolean boughtMode;
    private final Listener listener;

    public WishAdapter(boolean boughtMode, Listener listener) {
        super(DIFF);
        this.boughtMode = boughtMode;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = boughtMode ? R.layout.item_bought : R.layout.item_wish;
        return new VH(LayoutInflater.from(parent.getContext()).inflate(layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Wish w = getItem(position);
        h.name.setText(w.getName());
        h.price.setText(Fmt.cop(w.getPrice()));
        UiUtils.bindThumb(h.image, h.emoji, w);

        if (boughtMode) {
            Long purchasedAt = w.getPurchasedAt();
            long ts = purchasedAt != null ? purchasedAt : w.getCreatedAt();
            h.meta.setText(Categories.emoji(w.getCategory()) + " " + w.getCategory() + " · " + Fmt.date(ts));
        } else {
            h.chipPriority.setText(Priority.label(w.getPriority()));
            UiUtils.tint(h.chipPriority, Priority.color(w.getPriority()));
            h.chipCategory.setText(Categories.emoji(w.getCategory()) + " " + w.getCategory());
            UiUtils.tint(h.chipCategory, COLOR_CATEGORY);

            final String link = w.getLink();
            if (link != null) {
                h.link.setVisibility(View.VISIBLE);
                h.link.setOnClickListener(v -> openLink(v, link));
            } else {
                h.link.setVisibility(View.GONE);
            }
            h.edit.setOnClickListener(v -> listener.onEdit(w));
        }

        h.action.setOnClickListener(v -> listener.onAction(w));
        h.delete.setOnClickListener(v ->
                new MaterialAlertDialogBuilder(v.getContext())
                        .setTitle("¿Eliminar?")
                        .setMessage("Se borrará \"" + w.getName() + "\" de tu lista.")
                        .setPositiveButton("Eliminar", (d, which) -> listener.onDelete(w))
                        .setNegativeButton("Cancelar", null)
                        .show());
    }

    private static void openLink(View v, String raw) {
        String url = raw.startsWith("http") ? raw : "https://" + raw;
        try {
            v.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception ignored) {
            // enlace inválido o sin navegador: no hacemos nada
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView emoji, name, price, meta, link;
        final Chip chipPriority, chipCategory;
        final View edit, delete, action;

        VH(@NonNull View v) {
            super(v);
            image = v.findViewById(R.id.thumbImage);
            emoji = v.findViewById(R.id.thumbEmoji);
            name = v.findViewById(R.id.txtName);
            price = v.findViewById(R.id.txtPrice);
            meta = v.findViewById(R.id.txtMeta);
            link = v.findViewById(R.id.txtLink);
            chipPriority = v.findViewById(R.id.chipPriority);
            chipCategory = v.findViewById(R.id.chipCategory);
            edit = v.findViewById(R.id.btnEdit);
            delete = v.findViewById(R.id.btnDelete);
            action = v.findViewById(R.id.btnAction);
        }
    }
}
