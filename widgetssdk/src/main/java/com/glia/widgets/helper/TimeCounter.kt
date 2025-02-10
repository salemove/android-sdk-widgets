package com.glia.widgets.helper

import java.util.Timer
import java.util.TimerTask

internal class TimeCounter {
    private val formattedListeners: MutableList<FormattedTimerStatusListener> = ArrayList()
    private val rawListeners: MutableList<RawTimerStatusListener> = ArrayList()
    private var timerTask: TimerTask? = null
    private var timer: Timer? = null
    fun startNew(timerDelayMs: Int, timerIntervalMs: Int) {
        Logger.d(TAG, "startNew! timerDelay: " + Integer.valueOf(timerDelayMs).toString() +
                    ", timerInterval: " + Integer.valueOf(timerIntervalMs).toString())
        createNewTimerTask(timerDelayMs, timerIntervalMs)
        createNewTimer()
        timer?.schedule(timerTask, timerDelayMs.toLong(), timerIntervalMs.toLong())
    }

    fun stop() {
        Logger.d(TAG, "stop")
        timer?.cancel()
        timer = null

        timerTask?.cancel()
        timerTask = null
    }

    fun clear() {
        Logger.d(TAG, "clear")
        stop()
        formattedListeners.clear()
        rawListeners.clear()
    }

    fun addFormattedValueListener(listener: FormattedTimerStatusListener) {
        Logger.d(TAG, "addFormattedListener")
        formattedListeners.add(listener)
    }

    fun removeFormattedValueListener(listener: FormattedTimerStatusListener) {
        Logger.d(TAG, "removeFormattedListener")
        formattedListeners.remove(listener)
    }

    fun addRawValueListener(listener: RawTimerStatusListener) {
        Logger.d(TAG, "addRawListener")
        rawListeners.add(listener)
    }

    private fun createNewTimer() {
        timer?.cancel()
        timer = Timer()
    }

    private fun createNewTimerTask(timerDelayMs: Int, timerIntervalMs: Int) {
        timerTask?.cancel()
        timerTask = object : TimerTask() {
            var value = timerDelayMs

            @Synchronized
            override fun run() {
                Logger.d(TAG, "Timer value: $value")
                for (listener in rawListeners) {
                    listener.onNewRawTimerValue(value)
                }

                if (formattedListeners.isNotEmpty()) {
                    val time = formatElapsedTime(value.toLong())
                    Logger.d(TAG, "Formatted timer: $time")
                    for (listener in formattedListeners) {
                        listener.onNewFormattedTimerValue(time)
                    }
                }
                value += timerIntervalMs
            }

            @Synchronized
            override fun cancel(): Boolean {
                for (listener in rawListeners) {
                    listener.onRawTimerCancelled()
                }
                for (listener in formattedListeners) {
                    listener.onFormattedTimerCancelled()
                }
                Logger.d(TAG, "cancel")
                return super.cancel()
            }
        }
    }

    val isRunning: Boolean
        get() = timerTask != null

    interface FormattedTimerStatusListener {
        fun onNewFormattedTimerValue(formattedValue: String)
        fun onFormattedTimerCancelled()
    }

    interface RawTimerStatusListener {
        fun onNewRawTimerValue(timerValue: Int)
        fun onRawTimerCancelled()
    }
}
