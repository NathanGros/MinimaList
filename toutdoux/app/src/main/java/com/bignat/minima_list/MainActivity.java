package com.bignat.toutdoux;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bignat.toutdoux.day_view.DayViewFragment;
import com.bignat.toutdoux.timeless_lists.TimelessListsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView nav = findViewById(R.id.bottomNavigation);

        // Default screen
        if (savedInstanceState == null) {
            loadFragment(new DayViewFragment());
        }

        // Switch fragments
        nav.setOnItemSelectedListener(item -> {
            Fragment fragment;

            if (item.getItemId() == R.id.nav_daily) {
                fragment = new DayViewFragment();
            } else {
                fragment = new TimelessListsFragment();
            }

            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            loadFragment(fragment);
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit();
    }
}
