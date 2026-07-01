package com.example.clock;

public class CityClock {
    private String cityName;
    private String timeZone;

    public CityClock(String cityName, String timeZone) {
        this.cityName = cityName;
        this.timeZone = timeZone;
    }

    public String getCityName() { return cityName; }
    public String getTimeZone() { return timeZone; }
}