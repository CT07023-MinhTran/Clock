package com.example.clock;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;
import java.util.Locale;

public class TimerFragment extends Fragment {
    private TextView tvDisplay;
    private GridLayout layoutKeyboard;
    private View layoutInput;
    private RecyclerView rvActiveTimers;
    private FloatingActionButton fabAddTimer;
    private ActiveTimerAdapter adapter;
    private List<TimerModel> timerList;
    private String inputTime = "";
    private static final int ONGOING_NOTIFICATION_ID = 201;
    
    private Handler handler = new Handler();
    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            boolean hasRunning = false;
            for (int i = 0; i < timerList.size(); i++) {
                TimerModel timer = timerList.get(i);
                if (timer.isRunning()) {
                    long now = System.currentTimeMillis();
                    long remaining = Math.max(0, timer.getEndTime() - now);
                    timer.setRemainingTimeInMillis(remaining);
                    
                    if (remaining == 0) {
                        timer.setRunning(false);
                    } else {
                        hasRunning = true;
                    }
                }
            }
            
            if (hasRunning) {
                updateNotification();
            } else if (timerList.isEmpty()) {
                cancelNotification();
            }

            if (adapter != null && isVisible() && !timerList.isEmpty()) {
                for (int i = 0; i < rvActiveTimers.getChildCount(); i++) {
                    View view = rvActiveTimers.getChildAt(i);
                    RecyclerView.ViewHolder vh = rvActiveTimers.getChildViewHolder(view);
                    if (vh instanceof ActiveTimerAdapter.TimerViewHolder) {
                        int pos = vh.getAbsoluteAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION && pos < timerList.size()) {
                            adapter.onBindViewHolder((ActiveTimerAdapter.TimerViewHolder) vh, pos);
                        }
                    }
                }
            }
            handler.postDelayed(this, 100);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timer, container, false);

        tvDisplay = view.findViewById(R.id.tv_timer_display);
        layoutKeyboard = view.findViewById(R.id.layout_keyboard);
        layoutInput = view.findViewById(R.id.layout_input);
        rvActiveTimers = view.findViewById(R.id.rv_active_timers);
        fabAddTimer = view.findViewById(R.id.fab_add_timer);
        MaterialButton btnStart = view.findViewById(R.id.btn_start_timer);
        MaterialButton btnDelete = view.findViewById(R.id.btn_delete_timer);
        MaterialButton btnDoubleZero = view.findViewById(R.id.btn_double_zero);

        timerList = TimerRepository.getInstance().getTimerList();

        adapter = new ActiveTimerAdapter(timerList, new ActiveTimerAdapter.OnTimerActionListener() {
            @Override
            public void onPausePlay(int position) {
                if (position >= 0 && position < timerList.size()) {
                    TimerModel t = timerList.get(position);
                    if (t.getRemainingTimeInMillis() <= 0) {
                        t.setRemainingTimeInMillis(t.getTotalTimeInMillis());
                        t.setRunning(false);
                        TimerReceiver.stopGlobalAlarm(getContext());
                    } else if (t.isRunning()) {
                        t.setRunning(false);
                        cancelSystemTimer(t);
                    } else {
                        t.setEndTime(System.currentTimeMillis() + t.getRemainingTimeInMillis());
                        t.setRunning(true);
                        scheduleSystemTimer(t);
                    }
                    adapter.notifyItemChanged(position);
                }
            }

            @Override
            public void onRestart(int position) {
                if (position >= 0 && position < timerList.size()) {
                    TimerModel t = timerList.get(position);
                    t.setEndTime(System.currentTimeMillis() + t.getTotalTimeInMillis());
                    t.setRunning(true);
                    scheduleSystemTimer(t);
                    adapter.notifyItemChanged(position);
                    TimerReceiver.stopGlobalAlarm(getContext());
                }
            }

            @Override
            public void onClose(int position) {
                if (position >= 0 && position < timerList.size()) {
                    TimerModel t = timerList.get(position);
                    cancelSystemTimer(t);
                    timerList.remove(position);
                    adapter.notifyDataSetChanged();
                    updateUIState();
                    TimerReceiver.stopGlobalAlarm(getContext());
                    if (timerList.isEmpty()) cancelNotification();
                }
            }
        });
        rvActiveTimers.setLayoutManager(new LinearLayoutManager(getContext()));
        rvActiveTimers.setAdapter(adapter);

        for (int i = 0; i < layoutKeyboard.getChildCount(); i++) {
            View child = layoutKeyboard.getChildAt(i);
            if (child instanceof MaterialButton && 
                child.getId() != R.id.btn_delete_timer && 
                child.getId() != R.id.btn_double_zero && 
                child.getId() != R.id.btn_start_timer) {
                MaterialButton b = (MaterialButton) child;
                b.setOnClickListener(v -> appendNumber(b.getText().toString()));
            }
        }

        btnDoubleZero.setOnClickListener(v -> { appendNumber("0"); appendNumber("0"); });
        btnDelete.setOnClickListener(v -> { if (!inputTime.isEmpty()) { inputTime = inputTime.substring(0, inputTime.length() - 1); updateDisplay(); } });

        btnStart.setOnClickListener(v -> {
            long millis = calculateMillis();
            if (millis > 0) {
                TimerModel newTimer = new TimerModel(millis);
                TimerRepository.getInstance().addTimer(newTimer);
                scheduleSystemTimer(newTimer);
                adapter.notifyDataSetChanged();
                inputTime = "";
                updateDisplay();
                updateUIState();
            }
        });

        fabAddTimer.setOnClickListener(v -> { layoutInput.setVisibility(View.VISIBLE); fabAddTimer.setVisibility(View.GONE); });

        handler.post(updateRunnable);
        updateDisplay();
        updateUIState();
        return view;
    }

    private void scheduleSystemTimer(TimerModel timer) {
        if (getContext() == null) return;
        AlarmManager am = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getContext(), TimerReceiver.class);
        intent.putExtra("timer_label", timer.getLabel());
        PendingIntent pi = PendingIntent.getBroadcast(getContext(), (int)timer.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timer.getEndTime(), pi);
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, timer.getEndTime(), pi);
        }
    }

    private void cancelSystemTimer(TimerModel timer) {
        if (getContext() == null) return;
        AlarmManager am = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getContext(), TimerReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(getContext(), (int)timer.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        am.cancel(pi);
    }

    private void appendNumber(String num) { if (inputTime.length() < 6) { inputTime += num; updateDisplay(); } }

    private void updateDisplay() {
        String fullInput = String.format(Locale.getDefault(), "%06d", inputTime.isEmpty() ? 0 : Long.parseLong(inputTime));
        int h = Integer.parseInt(fullInput.substring(0, 2));
        int m = Integer.parseInt(fullInput.substring(2, 4));
        int s = Integer.parseInt(fullInput.substring(4, 6));
        tvDisplay.setText(String.format(Locale.getDefault(), "%02dh %02dm %02ds", h, m, s));
    }

    private long calculateMillis() {
        String fullInput = String.format(Locale.getDefault(), "%06d", inputTime.isEmpty() ? 0 : Long.parseLong(inputTime));
        int h = Integer.parseInt(fullInput.substring(0, 2));
        int m = Integer.parseInt(fullInput.substring(2, 4));
        int s = Integer.parseInt(fullInput.substring(4, 6));
        return ((long) h * 3600 + (long) m * 60 + s) * 1000;
    }

    private void updateUIState() {
        if (timerList.isEmpty()) {
            layoutInput.setVisibility(View.VISIBLE);
            fabAddTimer.setVisibility(View.GONE);
        } else {
            layoutInput.setVisibility(View.GONE);
            fabAddTimer.setVisibility(View.VISIBLE);
        }
    }

    private void updateNotification() {
        if (timerList.isEmpty() || getContext() == null) return;
        
        TimerModel firstTimer = timerList.get(0);
        long seconds = firstTimer.getRemainingTimeInMillis() / 1000;
        String timeStr = String.format(Locale.getDefault(), "%02d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, seconds % 60);

        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.putExtra("target_tab", "OPEN_TIMER");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(getContext(), 10, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationManager nm = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "timer_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(new NotificationChannel(channelId, "Hẹn giờ", NotificationManager.IMPORTANCE_LOW));
        }
        NotificationCompat.Builder b = new NotificationCompat.Builder(getContext(), channelId)
                .setSmallIcon(R.drawable.ic_hourglass)
                .setContentTitle(firstTimer.getLabel())
                .setContentText("Còn lại: " + timeStr)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setContentIntent(pi);
        nm.notify(ONGOING_NOTIFICATION_ID, b.build());
    }

    private void cancelNotification() {
        if (getContext() != null) {
            ((NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE)).cancel(ONGOING_NOTIFICATION_ID);
        }
    }

    @Override public void onDestroy() { super.onDestroy(); }
}