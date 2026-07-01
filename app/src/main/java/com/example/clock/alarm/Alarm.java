package com.example.clock;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Alarm implements Serializable {
    private int hour;
    private int minute;
    private boolean isEnabled;
    private List<Integer> days; // 1: Sunday, 2: Monday, ..., 7: Saturday

    public Alarm(int hour, int minute, boolean isEnabled) {
        this.hour = hour;
        this.minute = minute;
        this.isEnabled = isEnabled;
        this.days = new ArrayList<>();
    }

    public int getHour() { return hour; }
    public void setHour(int hour) { this.hour = hour; }
    public int getMinute() { return minute; }
    public void setMinute(int minute) { this.minute = minute; }
    public boolean isEnabled() { return isEnabled; }
    public void setEnabled(boolean enabled) { isEnabled = enabled; }
    public List<Integer> getDays() { return days; }
    public void setDays(List<Integer> days) { this.days = days; }

    public String getTimeFormatted() {
        int h = hour % 12;
        if (h == 0) h = 12;
        String amPm = (hour < 12) ? "AM" : "PM";
        return String.format("%02d:%02d %s", h, minute, amPm);
    }

    public String getDaysFormatted() {
        if (days == null || days.isEmpty()) {
            return "";
        }
        if (days.size() == 7) {
            return "Hàng ngày";
        }
        StringBuilder sb = new StringBuilder();
        for (Integer day : days) {
            switch (day) {
                case 2: sb.append("T2 "); break;
                case 3: sb.append("T3 "); break;
                case 4: sb.append("T4 "); break;
                case 5: sb.append("T5 "); break;
                case 6: sb.append("T6 "); break;
                case 7: sb.append("T7 "); break;
                case 1: sb.append("CN "); break;
            }
        }
        return sb.toString().trim();
    }
}