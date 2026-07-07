package com.example.clock.stopwatch;

import com.example.clock.R;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StopwatchFragment extends Fragment {
    private TextView tvDisplay;
    private MaterialButton btnStart, btnReset, btnLap;
    private RecyclerView rvLaps;
    private LapAdapter lapAdapter;
    private List<String> lapList = new ArrayList<>();
    
    private Handler handler = new Handler();
    private long startTime = 0L;
    private long timeInMilliseconds = 0L;
    private long timeSwapBuff = 0L;
    private long updateTime = 0L;
    private boolean isRunning = false;

    private Runnable updateTimerThread = new Runnable() {
        public void run() {
            timeInMilliseconds = SystemClock.elapsedRealtime() - startTime;
            updateTime = timeSwapBuff + timeInMilliseconds;
            
            int secs = (int) (updateTime / 1000);
            int mins = secs / 60;
            secs = secs % 60;
            int milliseconds = (int) (updateTime % 1000) / 10;
            
            tvDisplay.setText(String.format(Locale.getDefault(), "%02d:%02d.%02d", mins, secs, milliseconds));
            handler.postDelayed(this, 10);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stopwatch, container, false);

        tvDisplay = view.findViewById(R.id.tv_stopwatch_display);
        btnStart = view.findViewById(R.id.btn_start_stopwatch);
        btnReset = view.findViewById(R.id.btn_reset_stopwatch);
        btnLap = view.findViewById(R.id.btn_lap_stopwatch);
        rvLaps = view.findViewById(R.id.rv_laps);

        lapAdapter = new LapAdapter(lapList);
        rvLaps.setLayoutManager(new LinearLayoutManager(getContext()));
        rvLaps.setAdapter(lapAdapter);

        btnStart.setOnClickListener(v -> {
            if (isRunning) {
                pauseStopwatch();
            } else {
                startStopwatch();
            }
        });

        btnReset.setOnClickListener(v -> resetStopwatch());
        
        btnLap.setOnClickListener(v -> recordLap());

        return view;
    }

    private void startStopwatch() {
        startTime = SystemClock.elapsedRealtime();
        handler.postDelayed(updateTimerThread, 0);
        isRunning = true;
        
        btnStart.setText("Dừng");
        btnStart.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF5252")));
        btnReset.setVisibility(View.INVISIBLE);
        btnLap.setVisibility(View.VISIBLE);
    }

    private void pauseStopwatch() {
        timeSwapBuff += timeInMilliseconds;
        handler.removeCallbacks(updateTimerThread);
        isRunning = false;
        
        btnStart.setText("Tiếp tục");
        btnStart.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#6200EE")));
        btnReset.setVisibility(View.VISIBLE);
        btnLap.setVisibility(View.INVISIBLE);
    }

    private void recordLap() {
        String currentTime = tvDisplay.getText().toString();
        lapList.add(0, currentTime); // Thêm vòng mới lên đầu
        lapAdapter.notifyItemInserted(0);
        rvLaps.scrollToPosition(0);
    }

    private void resetStopwatch() {
        startTime = 0L;
        timeInMilliseconds = 0L;
        timeSwapBuff = 0L;
        updateTime = 0L;
        
        tvDisplay.setText("00:00.00");
        btnStart.setText("Bắt đầu");
        btnStart.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#6200EE")));
        btnReset.setVisibility(View.INVISIBLE);
        btnLap.setVisibility(View.INVISIBLE);
        isRunning = false;
        handler.removeCallbacks(updateTimerThread);
        
        lapList.clear();
        lapAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateTimerThread);
    }
}