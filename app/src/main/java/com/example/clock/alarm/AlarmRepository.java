package com.example.clock.alarm;

import java.util.ArrayList;
import java.util.List;

public class AlarmRepository {
    private static AlarmRepository instance;
    private List<Alarm> alarmList;

    private AlarmRepository() {
        alarmList = new ArrayList<>();
    }

    public static synchronized AlarmRepository getInstance() {
        if (instance == null) {
            instance = new AlarmRepository();
        }
        return instance;
    }

    public List<Alarm> getAlarmList() {
        return alarmList;
    }

    public void addAlarm(Alarm alarm) {
        alarmList.add(alarm);
    }

    public void removeAlarm(int position) {
        if (position >= 0 && position < alarmList.size()) {
            alarmList.remove(position);
        }
    }
    
    public Alarm getAlarm(int position) {
        if (position >= 0 && position < alarmList.size()) {
            return alarmList.get(position);
        }
        return null;
    }
}