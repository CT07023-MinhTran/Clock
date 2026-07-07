package com.example.clock.worldclock;

import androidx.annotation.NonNull;

public class CitySearchResult {
    private String cityName;
    private String areaName; // Châu lục hoặc Quốc gia
    private String timeZoneId;

    public CitySearchResult(String cityName, String areaName, String timeZoneId) {
        this.cityName = cityName;
        this.areaName = areaName;
        this.timeZoneId = timeZoneId;
    }

    public String getCityName() { return cityName; }
    public String getTimeZoneId() { return timeZoneId; }

    @NonNull
    @Override
    public String toString() {
        return cityName + " (" + areaName + ")";
    }
}