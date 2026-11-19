package com.antojitos.app;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/** Pantalla principal: barra inferior para alternar entre las dos modalidades. */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView nav = findViewById(R.id.bottomNav);
        if (savedInstanceState == null) {
            show(new WishesFragment());
        }
        nav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_wishes) {
                show(new WishesFragment());
            } else {
                show(new SavingsFragment());
            }
            return true;
        });
        nav.setOnItemReselectedListener(item -> {
            // No hacer nada al reseleccionar la pestaña activa
        });
    }

    private void show(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }
}
