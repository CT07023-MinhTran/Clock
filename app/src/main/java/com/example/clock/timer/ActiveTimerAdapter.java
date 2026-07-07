package com.example.clock.timer;

import com.example.clock.R;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.List;
import java.util.Locale;

public class ActiveTimerAdapter extends RecyclerView.Adapter<ActiveTimerAdapter.TimerViewHolder> {
    private List<TimerModel> timerList;
    private OnTimerActionListener listener;

    public interface OnTimerActionListener {
        void onPausePlay(int position);
        void onRestart(int position);
        void onClose(int position);
    }

    public ActiveTimerAdapter(List<TimerModel> timerList, OnTimerActionListener listener) {
        this.timerList = timerList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TimerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_active_timer, parent, false);
        return new TimerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimerViewHolder holder, int position) {
        TimerModel timer = timerList.get(position);
        holder.tvTitle.setText(timer.getLabel());
        
        long seconds = timer.getRemainingTimeInMillis() / 1000;
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        holder.tvRemaining.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s));

        int progress = (int) (timer.getRemainingTimeInMillis() * 1000 / timer.getTotalTimeInMillis());
        holder.pbTimer.setProgress(progress);

        if (timer.getRemainingTimeInMillis() <= 0) {
            holder.btnPausePlay.setIconResource(R.drawable.ic_stop);
        } else {
            holder.btnPausePlay.setIconResource(timer.isRunning() ? R.drawable.ic_pause : R.drawable.ic_play);
        }

        // Sử dụng getAdapterPosition() để lấy vị trí chính xác nhất khi có thay đổi danh sách
        holder.btnPausePlay.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) listener.onPausePlay(pos);
        });
        
        holder.btnRestart.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) listener.onRestart(pos);
        });
        
        holder.btnClose.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) listener.onClose(pos);
        });
    }

    @Override
    public int getItemCount() {
        return timerList.size();
    }

    static class TimerViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvRemaining;
        ProgressBar pbTimer;
        ImageButton btnRestart, btnClose;
        MaterialButton btnPausePlay;

        public TimerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_timer_title);
            tvRemaining = itemView.findViewById(R.id.tv_remaining_time);
            pbTimer = itemView.findViewById(R.id.pb_timer);
            btnRestart = itemView.findViewById(R.id.btn_restart_timer);
            btnClose = itemView.findViewById(R.id.btn_close_timer);
            btnPausePlay = itemView.findViewById(R.id.btn_pause_play);
        }
    }
}