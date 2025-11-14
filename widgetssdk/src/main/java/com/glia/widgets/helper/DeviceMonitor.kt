package com.glia.widgets.helper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.glia.telemetry_lib.GliaLogger
import com.glia.telemetry_lib.LogEvents
import com.glia.widgets.di.GliaCore
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.processors.BehaviorProcessor

internal enum class NetworkState {
    CONNECTED,
    DISCONNECTED;

    companion object {
        fun from(isConnected: Boolean): NetworkState = if (isConnected) CONNECTED else DISCONNECTED
    }
}

internal enum class DeviceState {
    // when the user is present after device wakes up (e.g when the keyguard is gone).
    USER_PRESENT,

    // when the device is interactive and the screen is on.
    // Device can still be locked.
    INTERACTIVE,

    // when the device is not interactive and the screen is off.
    NON_INTERACTIVE
}

internal class DeviceMonitor(context: Context, core: GliaCore) {

    val networkState: Flowable<NetworkState> = core.getNetworkTracker(context)
        .state
        .toFlowable(BackpressureStrategy.LATEST)
        .map { NetworkState.from(it.isConnected) }
        .distinctUntilChanged()
        .hide()

    private val _deviceState: BehaviorProcessor<DeviceState> = BehaviorProcessor.createDefault(DeviceState.USER_PRESENT)
    val deviceState: Flowable<DeviceState> = _deviceState.distinctUntilChanged().hide()

    private val userPresentReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_USER_PRESENT) {
                _deviceState.onNext(DeviceState.USER_PRESENT)
            }
        }
    }

    private val screenOnReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_ON) {
                GliaLogger.i(LogEvents.DEVICE_SCREEN_ON)
                _deviceState.onNext(DeviceState.INTERACTIVE)
            }
        }
    }

    private val screenOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                GliaLogger.i(LogEvents.DEVICE_SCREEN_OFF)
                _deviceState.onNext(DeviceState.NON_INTERACTIVE)
            }
        }
    }

    init {
        context.registerReceiver(userPresentReceiver, IntentFilter(Intent.ACTION_USER_PRESENT))
        context.registerReceiver(screenOnReceiver, IntentFilter(Intent.ACTION_SCREEN_ON))
        context.registerReceiver(screenOffReceiver, IntentFilter(Intent.ACTION_SCREEN_OFF))
    }
}
