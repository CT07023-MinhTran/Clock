package com.example.clock;

public class TimerModel {
    private long id;
    private long totalTimeInMillis;
    private long remainingTimeInMillis;
    private long endTime;
    private boolean isRunning;
    private String label;

    public TimerModel(long totalTimeInMillis) {
        this.id = System.currentTimeMillis();
        this.totalTimeInMillis = totalTimeInMillis;
        this.remainingTimeInMillis = totalTimeInMillis;
        this.isRunning = true;
        this.endTime = System.currentTimeMillis() + totalTimeInMillis;
        this.label = generateLabel(totalTimeInMillis);
    }

    public long getId() { return id; }
    public long getEndTime() { return endTime; }
    public void setEndTime(long endTime) { this.endTime = endTime; }

    private String generateLabel(long millis) {
        long seconds = millis / 1000;
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;

        if (h > 0) {
            if (m > 0 && s > 0) return "Bộ hẹn giờ " + h + " giờ " + m + " phút " + s + " giây";
            if (m > 0) return "Bộ hẹn giờ " + h + " giờ " + m + " phút";
            if (s > 0) return "Bộ hẹn giờ " + h + " giờ " + s + " giây";
            return "Bộ hẹn giờ " + h + " giờ";
        } else if (m > 0) {
            if (s > 0) return "Bộ hẹn " + m + " phút " + s + " giây";
            return "Bộ hẹn " + m + " phút";
        } else {
            return "Bộ hẹn " + s + " giây";
        }
    }

    public long getTotalTimeInMillis() { return totalTimeInMillis; }
    public long getRemainingTimeInMillis() { return remainingTimeInMillis; }
    public void setRemainingTimeInMillis(long remainingTimeInMillis) { this.remainingTimeInMillis = remainingTimeInMillis; }
    public boolean isRunning() { return isRunning; }
    public void setRunning(boolean running) { isRunning = running; }
    public String getLabel() { return label; }
}