package com.example.clock.main;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.clock.R;
import com.example.clock.alarm.AlarmFragment;
import com.example.clock.worldclock.WorldClockFragment;
import com.example.clock.timer.TimerFragment;
import com.example.clock.stopwatch.StopwatchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private static final String PREFS_NAME = "ClockPrefs";
    private static final String KEY_LAST_TAB = "last_tab";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            saveLastTab(id);

            if (id == R.id.nav_alarm) {
                selectedFragment = new AlarmFragment();
            } else if (id == R.id.nav_world_clock) {
                selectedFragment = new WorldClockFragment();
            } else if (id == R.id.nav_timer) {
                selectedFragment = new TimerFragment();
            } else if (id == R.id.nav_stopwatch) {
                selectedFragment = new StopwatchFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        if (getIntent() != null && "OPEN_TIMER".equals(getIntent().getStringExtra("target_tab"))) {
            bottomNav.setSelectedItemId(R.id.nav_timer);
        } else if (savedInstanceState == null) {
            int lastTabId = getLastTab();
            bottomNav.setSelectedItemId(lastTabId);
        }
    }

    private void saveLastTab(int tabId) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putInt(KEY_LAST_TAB, tabId).apply();
    }

    private int getLastTab() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getInt(KEY_LAST_TAB, R.id.nav_alarm);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null && "OPEN_TIMER".equals(intent.getStringExtra("target_tab"))) {
            bottomNav.setSelectedItemId(R.id.nav_timer);
        }
    }
}