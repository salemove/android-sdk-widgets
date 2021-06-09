package com.glia.widgets.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class TimeCounter {

    private final static String TAG = "TimeCounter";
    private TimerTask timerTask;
    private Timer timer;
    private final List<FormattedTimerStatusListener> formattedListeners = new ArrayList<>();
    private final List<RawTimerStatusListener> rawListeners = new ArrayList<>();

    public void startNew(int timerDelayMs, int timerIntervalMs) {
        Logger.d(TAG, "startNew! timerDelay: " + Integer.valueOf(timerDelayMs).toString() +
                ", timerInterval: " + Integer.valueOf(timerIntervalMs).toString());
        createNewTimerTask(timerDelayMs, timerIntervalMs);
        createNewTimer();
        timer.schedule(timerTask, timerDelayMs, timerIntervalMs);
    }

    public void stop() {
        Logger.d(TAG, "stop");
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
        Logger.d(TAG, "clear");
        stop();
        formattedListeners.clear();
        rawListeners.clear();
    }

    public void addFormattedValueListener(FormattedTimerStatusListener listener) {
        Logger.d(TAG, "addFormattedListener");
        formattedListeners.add(listener);
    }

    public void removeFormattedValueListener(FormattedTimerStatusListener listener) {
        Logger.d(TAG, "removeFormattedListener");
        formattedListeners.remove(listener);
    }

    public void addRawValueListener(RawTimerStatusListener listener) {
        Logger.d(TAG, "addRawListener");
        rawListeners.add(listener);
    }

    private void createNewTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        timer = new Timer();
    }

    private void createNewTimerTask(int timerDelayMs, int timerIntervalMs) {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        timerTask = new TimerTask() {
            int value = timerDelayMs;

            @Override
            public void run() {
                Logger.d(TAG, "Timer value: " + value);
                for (RawTimerStatusListener listener : rawListeners) {
                    listener.onNewRawTimerValue(value);
                }

                if (!formattedListeners.isEmpty()) {
                    String time = Utils.toMmSs(Long.valueOf(TimeUnit.MILLISECONDS.toSeconds(value)).intValue());
                    Logger.d(TAG, "Formatted timer: " + time);
                    for (FormattedTimerStatusListener listener : formattedListeners) {
                        listener.onNewFormattedTimerValue(time);
                    }
                }
                value = value + timerIntervalMs;
            }

            @Override
            public boolean cancel() {
                for (RawTimerStatusListener listener : rawListeners) {
                    listener.onRawTimerCancelled();
                }
                for (FormattedTimerStatusListener listener : formattedListeners) {
                    listener.onFormattedTimerCancelled();
                }
                Logger.d(TAG, "cancel");
                return super.cancel();
            }
        };
    }

    public boolean isRunning() {
        return timerTask != null;
    }

    public interface FormattedTimerStatusListener {
        void onNewFormattedTimerValue(String formatedValue);

        void onFormattedTimerCancelled();
    }

    public interface RawTimerStatusListener {
        void onNewRawTimerValue(int timerValue);

        void onRawTimerCancelled();
    }
}
