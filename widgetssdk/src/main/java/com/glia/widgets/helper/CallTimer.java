package com.glia.widgets.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CallTimer {

    private final static String TAG = "CallTimer";
    private TimerTask timerTask;
    private Timer timer;
    private final List<TimerStatusListener> listeners = new ArrayList<>();

    public void startNew() {
        int timerDelay = 1000;
        int timer1SecInterval = 1000;
        createNewTimerTask();
        createNewTimer();
        timer.schedule(timerTask, timerDelay, timer1SecInterval);
    }

    public void stop() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void clear() {
        stop();
        listeners.clear();
    }

    public void addListener(TimerStatusListener listener) {
        listeners.add(listener);
    }

    public void removeListener(TimerStatusListener listener){
        listeners.remove(listener);
    }

    private void createNewTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        timer = new Timer();
    }

    private void createNewTimerTask() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        timerTask = new TimerTask() {
            int seconds = 1;

            @Override
            public void run() {
                String time = Utils.toMmSs(seconds);
                for (TimerStatusListener listener : listeners) {
                    listener.onNewTimerValue(time);
                }
                Logger.d(TAG, "timer: " + time);
                seconds++;
            }

            @Override
            public boolean cancel() {
                for (TimerStatusListener listener : listeners) {
                    listener.onCancel();
                }
                return super.cancel();
            }
        };
    }

    public interface TimerStatusListener {
        void onNewTimerValue(String formatedValue);

        void onCancel();
    }
}
