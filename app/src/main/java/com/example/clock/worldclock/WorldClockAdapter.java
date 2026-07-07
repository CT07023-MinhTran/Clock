package com.example.clock.worldclock;

import com.example.clock.R;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextClock;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.TimeZone;

public class WorldClockAdapter extends RecyclerView.Adapter<WorldClockAdapter.ViewHolder> {
    private List<CityClock> cities;

    public WorldClockAdapter(List<CityClock> cities) {
        this.cities = cities;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_world_clock, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CityClock city = cities.get(position);
        holder.tvCityName.setText(city.getCityName());
        holder.tcCityTime.setTimeZone(city.getTimeZone());
        
        TimeZone tz = TimeZone.getTimeZone(city.getTimeZone());
        holder.tvTimezoneOffset.setText(tz.getDisplayName(false, TimeZone.SHORT));
    }

    @Override
    public int getItemCount() {
        return cities.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCityName;
        TextClock tcCityTime;
        TextView tvTimezoneOffset;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCityName = itemView.findViewById(R.id.tv_city_name);
            tcCityTime = itemView.findViewById(R.id.tc_city_time);
            tvTimezoneOffset = itemView.findViewById(R.id.tv_timezone_offset);
        }
    }
}