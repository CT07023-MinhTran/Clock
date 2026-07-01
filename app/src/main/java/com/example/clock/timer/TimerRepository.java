package com.example.clock;

import java.util.ArrayList;
import java.util.List;

public class TimerRepository {
    private static TimerRepository instance;
    private List<TimerModel> timerList;

    private TimerRepository() {
        timerList = new ArrayList<>();
    }

    public static synchronized TimerRepository getInstance() {
        if (instance == null) {
            instance = new TimerRepository();
        }
        return instance;
    }

    public List<TimerModel> getTimerList() {
        return timerList;
    }

    public void addTimer(TimerModel timer) {
        timerList.add(0, timer);
    }

    public void removeTimer(int position) {
        if (position >= 0 && position < timerList.size()) {
            timerList.remove(position);
        }
    }
}