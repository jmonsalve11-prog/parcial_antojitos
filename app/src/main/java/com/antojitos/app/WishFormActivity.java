package com.antojitos.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.antojitos.app.data.Cat;
import com.antojitos.app.data.Categories;
import com.antojitos.app.data.Wish;
import com.antojitos.app.data.WishRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/** Pantalla para crear o editar un antojo. */
public class WishFormActivity extends AppCompatActivity {

    private static final String X_EDIT = "edit";
    private static final String X_ID = "id";
    private static final String X_NAME = "name";
    private static final String X_PRICE = "price";
    private static final String X_PRIORITY = "priority";
    private static final String X_CATEGORY = "category";
    private static final String X_IMAGE = "imagePath";
    private static final String X_LINK = "link";
    private static final String X_PURCHASED = "purchased";
    private static final String X_CREATED = "createdAt";
    private static final String X_PURCHASED_AT = "purchasedAt";
    private static final int MAX_IMAGE_SIDE = 720;

    public static Intent intentFor(Context context, Wish w) {
        Intent i = new Intent(context, WishFormActivity.class);
        i.putExtra(X_EDIT, true);
        i.putExtra(X_ID, w.getId());
        i.putExtra(X_NAME, w.getName());
        i.putExtra(X_PRICE, w.getPrice());
        i.putExtra(X_PRIORITY, w.getPriority());
        i.putExtra(X_CATEGORY, w.getCategory());
        i.putExtra(X_IMAGE, w.getImagePath());
        i.putExtra(X_LINK, w.getLink());
        i.putExtra(X_PURCHASED, w.getPurchased());
        i.putExtra(X_CREATED, w.getCreatedAt());
        if (w.getPurchasedAt() != null) i.putExtra(X_PURCHASED_AT, w.getPurchasedAt().longValue());
        return i;
    }

    private WishRepository repo;
    private boolean editing;
    private String category;
    private String imagePath;

    private TextInputLayout layoutName, layoutPrice;
    private TextInputEditText inputName, inputPrice, inputLink;
    private ChipGroup groupPriority, groupCategory;
    private ImageView preview;
    private MaterialButton btnPhoto, btnRemove;

    private final ActivityResultLauncher<PickVisualMediaRequest> picker =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) importPhoto(uri);
            });

    @Override
    protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        setContentView(R.layout.activity_wish_form);
        repo = WishRepository.get(this);

        final Intent in = getIntent();
        editing = in.getBooleanExtra(X_EDIT, false);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(editing ? "Editar antojo" : "Nuevo antojo");
        toolbar.setNavigationOnClickListener(v -> finish());

        layoutName = findViewById(R.id.layoutName);
        layoutPrice = findViewById(R.id.layoutPrice);
        inputName = findViewById(R.id.inputName);
        inputPrice = findViewById(R.id.inputPrice);
        inputLink = findViewById(R.id.inputLink);
        groupPriority = findViewById(R.id.chipGroupPriorityForm);
        groupCategory = findViewById(R.id.chipGroupCategoryForm);
        preview = findViewById(R.id.imagePreview);
        btnPhoto = findViewById(R.id.btnPhoto);
        btnRemove = findViewById(R.id.btnRemovePhoto);

        // Estado inicial (desde el intent o desde una rotación de pantalla)
        category = saved != null ? saved.getString(X_CATEGORY) : in.getStringExtra(X_CATEGORY);
        imagePath = saved != null ? saved.getString(X_IMAGE) : in.getStringExtra(X_IMAGE);
        if (category == null) category = Categories.ALL.get(0).getName();

        if (saved == null && editing) {
            inputName.setText(in.getStringExtra(X_NAME));
            inputPrice.setText(String.valueOf(in.getLongExtra(X_PRICE, 0)));
            String link = in.getStringExtra(X_LINK);
            if (link != null) inputLink.setText(link);
        }

        int[] priorityIds = {R.id.chipP0, R.id.chipP1, R.id.chipP2};
        int p = saved != null ? saved.getInt(X_PRIORITY, in.getIntExtra(X_PRIORITY, 1)) : in.getIntExtra(X_PRIORITY, 1);
        groupPriority.check(priorityIds[Math.max(0, Math.min(2, p))]);

        buildCategoryChips();
        updatePhotoUi();

        btnPhoto.setOnClickListener(v -> picker.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));
        btnRemove.setOnClickListener(v -> {
            imagePath = null;
            updatePhotoUi();
        });
        findViewById(R.id.btnSave).setOnClickListener(v -> save(in));
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle out) {
        super.onSaveInstanceState(out);
        out.putString(X_CATEGORY, category);
        out.putString(X_IMAGE, imagePath);
        int checked = groupPriority.getCheckedChipId();
        out.putInt(X_PRIORITY, checked == R.id.chipP0 ? 0 : checked == R.id.chipP2 ? 2 : 1);
    }

    private void buildCategoryChips() {
        LayoutInflater inflater = LayoutInflater.from(this);
        for (Cat c : Categories.ALL) {
            Chip chip = (Chip) inflater.inflate(R.layout.chip_filter, groupCategory, false);
            chip.setId(View.generateViewId());
            chip.setText(c.getEmoji() + " " + c.getName());
            chip.setTag(c.getName());
            groupCategory.addView(chip);
            if (c.getName().equals(category)) chip.setChecked(true);
        }
        groupCategory.setOnCheckedStateChangeListener((g, ids) -> {
            if (!ids.isEmpty()) {
                Chip chip = g.findViewById(ids.get(0));
                category = (String) chip.getTag();
            }
        });
    }

    private void updatePhotoUi() {
        if (imagePath != null && new File(imagePath).exists()) {
            preview.setVisibility(View.VISIBLE);
            preview.setImageURI(Uri.fromFile(new File(imagePath)));
            btnPhoto.setText("Cambiar foto");
            btnRemove.setVisibility(View.VISIBLE);
        } else {
            imagePath = null;
            preview.setVisibility(View.GONE);
            btnPhoto.setText("📷 Agregar foto de la galería");
            btnRemove.setVisibility(View.GONE);
        }
    }

    private void importPhoto(Uri uri) {
        new Thread(() -> {
            try {
                String path = copyScaled(uri);
                runOnUiThread(() -> {
                    imagePath = path;
                    updatePhotoUi();
                });
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "No se pudo cargar la foto", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    /** Copia la foto a la carpeta privada de la app, reducida para no gastar memoria. */
    private String copyScaled(Uri uri) throws IOException {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream in = getContentResolver().openInputStream(uri)) {
            BitmapFactory.decodeStream(in, null, bounds);
        }
        int sample = 1;
        while (bounds.outWidth / sample > MAX_IMAGE_SIDE || bounds.outHeight / sample > MAX_IMAGE_SIDE) {
            sample *= 2;
        }
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = sample;
        Bitmap bmp;
        try (InputStream in = getContentResolver().openInputStream(uri)) {
            bmp = BitmapFactory.decodeStream(in, null, opts);
        }
        if (bmp == null) throw new IOException("Imagen inválida");
        File file = new File(getFilesDir(), "img_" + System.currentTimeMillis() + ".jpg");
        try (FileOutputStream out = new FileOutputStream(file)) {
            if (!bmp.compress(Bitmap.CompressFormat.JPEG, 85, out)) throw new IOException("Error al guardar imagen");
        } finally {
            bmp.recycle();
        }
        return file.getAbsolutePath();
    }

    private static String text(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }

    private void save(Intent in) {
        String name = text(inputName);
        Long price = null;
        try {
            price = Long.parseLong(text(inputPrice));
        } catch (NumberFormatException ignored) {
        }

        boolean ok = true;
        if (name.isEmpty()) {
            layoutName.setError("Escribe el nombre del producto");
            ok = false;
        } else {
            layoutName.setError(null);
        }
        if (price == null || price < 0) {
            layoutPrice.setError("Escribe un precio válido (0 o mayor)");
            ok = false;
        } else {
            layoutPrice.setError(null);
        }
        if (!ok) return;

        int checked = groupPriority.getCheckedChipId();
        int priority = checked == R.id.chipP0 ? 0 : checked == R.id.chipP2 ? 2 : 1;
        String link = text(inputLink);

        long id = editing ? in.getLongExtra(X_ID, 0) : 0;
        boolean purchased = editing && in.getBooleanExtra(X_PURCHASED, false);
        long createdAt = editing ? in.getLongExtra(X_CREATED, System.currentTimeMillis())
                : System.currentTimeMillis();
        Long purchasedAt = editing && in.hasExtra(X_PURCHASED_AT)
                ? Long.valueOf(in.getLongExtra(X_PURCHASED_AT, 0)) : null;

        repo.save(new Wish(id, name, price, priority, category, imagePath,
                link.isEmpty() ? null : link, purchased, createdAt, purchasedAt));
        finish();
    }
}
