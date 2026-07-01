package com.example.clock.alarm;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.clock.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AlarmFragment extends Fragment {
    private AlarmAdapter adapter;
    private List<Alarm> alarmList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alarm, container, false);

        RecyclerView rvAlarms = view.findViewById(R.id.rv_alarms);
        FloatingActionButton fabAdd = view.findViewById(R.id.fab_add_alarm);

        alarmList = AlarmRepository.getInstance().getAlarmList();

        adapter = new AlarmAdapter(alarmList, position -> showEditDialog(position));
        rvAlarms.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAlarms.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> createNewAlarm());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void createNewAlarm() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                (view, hourOfDay, minuteOfHour) -> {
                    Alarm newAlarm = new Alarm(hourOfDay, minuteOfHour, true);
                    alarmList.add(newAlarm);
                    int position = alarmList.size() - 1;
                    adapter.notifyItemInserted(position);
                    showEditDialog(position);
                }, hour, minute, false);

        timePickerDialog.setTitle("Thêm báo thức");
        timePickerDialog.show();
    }

    private void showEditDialog(int position) {
        Alarm alarm = alarmList.get(position);
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_alarm, null);

        TextView tvTime = dialogView.findViewById(R.id.tv_edit_alarm_time);
        CheckBox cbMon = dialogView.findViewById(R.id.cb_monday);
        CheckBox cbTue = dialogView.findViewById(R.id.cb_tuesday);
        CheckBox cbWed = dialogView.findViewById(R.id.cb_wednesday);
        CheckBox cbThu = dialogView.findViewById(R.id.cb_thursday);
        CheckBox cbFri = dialogView.findViewById(R.id.cb_friday);
        CheckBox cbSat = dialogView.findViewById(R.id.cb_saturday);
        CheckBox cbSun = dialogView.findViewById(R.id.cb_sunday);
        View btnDelete = dialogView.findViewById(R.id.btn_delete_alarm);

        tvTime.setText(alarm.getTimeFormatted());
        tvTime.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                    (tpView, hourOfDay, minuteOfHour) -> {
                        alarm.setHour(hourOfDay);
                        alarm.setMinute(minuteOfHour);
                        tvTime.setText(alarm.getTimeFormatted());
                    }, alarm.getHour(), alarm.getMinute(), false);
            timePickerDialog.show();
        });

        List<Integer> days = alarm.getDays();
        cbMon.setChecked(days.contains(2));
        cbTue.setChecked(days.contains(3));
        cbWed.setChecked(days.contains(4));
        cbThu.setChecked(days.contains(5));
        cbFri.setChecked(days.contains(6));
        cbSat.setChecked(days.contains(7));
        cbSun.setChecked(days.contains(1));

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Tùy chỉnh báo thức")
                .setView(dialogView)
                .setPositiveButton("Lưu", (d, which) -> {
                    List<Integer> selectedDays = new ArrayList<>();
                    if (cbMon.isChecked()) selectedDays.add(2);
                    if (cbTue.isChecked()) selectedDays.add(3);
                    if (cbWed.isChecked()) selectedDays.add(4);
                    if (cbThu.isChecked()) selectedDays.add(5);
                    if (cbFri.isChecked()) selectedDays.add(6);
                    if (cbSat.isChecked()) selectedDays.add(7);
                    if (cbSun.isChecked()) selectedDays.add(1);
                    
                    alarm.setDays(selectedDays);
                    alarm.setEnabled(true);
                    adapter.notifyItemChanged(position);
                    
                    startAlarm(alarm, position);
                })
                .setNegativeButton("Hủy", null)
                .create();

        btnDelete.setOnClickListener(v -> {
            cancelAlarm(position);
            AlarmRepository.getInstance().removeAlarm(position);
            adapter.notifyDataSetChanged();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void startAlarm(Alarm alarm, int requestId) {
        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getContext(), AlarmReceiver.class);
        intent.putExtra("alarm_id", requestId);
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), requestId, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, alarm.getHour());
        calendar.set(Calendar.MINUTE, alarm.getMinute());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DATE, 1);
        }

        if (alarmManager != null) {
            alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(), pendingIntent), pendingIntent);
            Toast.makeText(getContext(), "Đã đặt báo thức lúc " + alarm.getTimeFormatted(), Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelAlarm(int requestId) {
        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), requestId, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}