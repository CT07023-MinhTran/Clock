package com.example.clock;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import androidx.core.app.NotificationCompat;

public class TimerReceiver extends BroadcastReceiver {
    public static Ringtone globalRingtone;
    public static Vibrator globalVibrator;
    private static final int FINISH_NOTIFICATION_ID = 202;
    public static final String ACTION_STOP_TIMER = "com.example.clock.STOP_TIMER_ALARM";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        if (ACTION_STOP_TIMER.equals(action)) {
            stopGlobalAlarm(context);
            return;
        }

        String label = intent.getStringExtra("timer_label");
        
        // 1. Intent khi nhấn vào thông báo (Mở tab hẹn giờ)
        Intent contentIntent = new Intent(context, MainActivity.class);
        contentIntent.putExtra("target_tab", "OPEN_TIMER");
        contentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(context, 1, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // 2. Intent cho nút Dừng
        Intent stopIntent = new Intent(context, TimerReceiver.class);
        stopIntent.setAction(ACTION_STOP_TIMER);
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(context, 2, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "timer_end_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Hết giờ hẹn giờ", NotificationManager.IMPORTANCE_HIGH);
            channel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);
            nm.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_hourglass)
                .setContentTitle("Hết giờ!")
                .setContentText(label)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setOngoing(true)
                .setContentIntent(contentPendingIntent) // Mở app khi nhấn
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(android.R.drawable.ic_delete, "Dừng", stopPendingIntent); // Dùng icon hệ thống cho chắc chắn hiện

        nm.notify(FINISH_NOTIFICATION_ID, builder.build());

        playGlobalAlarm(context);
    }

    public static void playGlobalAlarm(Context context) {
        try {
            stopGlobalAlarm(context);
            Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmUri == null) alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            
            globalRingtone = RingtoneManager.getRingtone(context, alarmUri);
            if (globalRingtone != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    globalRingtone.setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build());
                }
                globalRingtone.play();
            }

            globalVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (globalVibrator != null) {
                globalVibrator.vibrate(new long[]{0, 1000, 1000}, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void stopGlobalAlarm(Context context) {
        if (globalRingtone != null && globalRingtone.isPlaying()) {
            globalRingtone.stop();
        }
        if (globalVibrator != null) {
            globalVibrator.cancel();
        }
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(FINISH_NOTIFICATION_ID);
    }
}