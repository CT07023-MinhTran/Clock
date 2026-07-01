package com.example.clock;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {
    private List<Alarm> alarmList;
    private OnAlarmClickListener listener;

    public interface OnAlarmClickListener {
        void onAlarmClick(int position);
    }

    public AlarmAdapter(List<Alarm> alarmList, OnAlarmClickListener listener) {
        this.alarmList = alarmList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alarm, parent, false);
        return new AlarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {
        Alarm alarm = alarmList.get(position);
        holder.tvTime.setText(alarm.getTimeFormatted());
        
        String daysFormatted = alarm.getDaysFormatted();
        if (daysFormatted.isEmpty()) {
            holder.tvDays.setVisibility(View.GONE);
        } else {
            holder.tvDays.setVisibility(View.VISIBLE);
            holder.tvDays.setText(daysFormatted);
        }

        holder.switchAlarm.setChecked(alarm.isEnabled());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAlarmClick(position);
            }
        });

        holder.switchAlarm.setOnCheckedChangeListener((buttonView, isChecked) -> {
            alarm.setEnabled(isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return alarmList.size();
    }

    static class AlarmViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime;
        TextView tvDays;
        SwitchCompat switchAlarm;

        public AlarmViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tv_alarm_time);
            tvDays = itemView.findViewById(R.id.tv_alarm_days);
            switchAlarm = itemView.findViewById(R.id.switch_alarm);
        }
    }
}