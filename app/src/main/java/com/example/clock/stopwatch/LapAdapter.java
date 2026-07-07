package com.example.clock.stopwatch;

import com.example.clock.R;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class LapAdapter extends RecyclerView.Adapter<LapAdapter.LapViewHolder> {
    private List<String> lapList;

    public LapAdapter(List<String> lapList) {
        this.lapList = lapList;
    }

    @NonNull
    @Override
    public LapViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lap, parent, false);
        return new LapViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LapViewHolder holder, int position) {
        // Hiển thị vòng mới nhất lên đầu (danh sách đảo ngược)
        int lapNumber = lapList.size() - position;
        holder.tvLapNumber.setText("Vòng " + lapNumber);
        holder.tvLapTime.setText(lapList.get(position));
    }

    @Override
    public int getItemCount() {
        return lapList.size();
    }

    static class LapViewHolder extends RecyclerView.ViewHolder {
        TextView tvLapNumber, tvLapTime;

        public LapViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLapNumber = itemView.findViewById(R.id.tv_lap_number);
            tvLapTime = itemView.findViewById(R.id.tv_lap_time);
        }
    }
}