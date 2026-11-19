package com.antojitos.app;

import android.content.res.ColorStateList;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.graphics.ColorUtils;

import com.antojitos.app.data.Categories;
import com.antojitos.app.data.Wish;
import com.google.android.material.chip.Chip;

import java.io.File;

final class UiUtils {

    private UiUtils() {
    }

    /** Muestra la foto del antojo o, si no tiene, el emoji de su categoría. */
    static void bindThumb(ImageView image, TextView emoji, Wish w) {
        String path = w.getImagePath();
        if (path != null && new File(path).exists()) {
            image.setImageURI(Uri.fromFile(new File(path)));
            image.setVisibility(View.VISIBLE);
            emoji.setVisibility(View.GONE);
        } else {
            image.setImageDrawable(null);
            image.setVisibility(View.GONE);
            emoji.setVisibility(View.VISIBLE);
            emoji.setText(Categories.emoji(w.getCategory()));
        }
    }

    /** Pinta un chip con fondo suave del color dado y texto del color pleno. */
    static void tint(Chip chip, int color) {
        chip.setChipBackgroundColor(ColorStateList.valueOf(ColorUtils.setAlphaComponent(color, 40)));
        chip.setTextColor(color);
    }
}
