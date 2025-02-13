package com.glia.widgets.helper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.core.content.getSystemService
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

internal class DeviceMonitor(context: Context) : ConnectivityManager.NetworkCallback() {

    private val connectivityManager = context.getSystemService<ConnectivityManager>()
    private val networkCapabilities: NetworkCapabilities? get() = connectivityManager?.run { getNetworkCapabilities(activeNetwork) }
    private val isConnected: Boolean get() = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

    private val _networkState: BehaviorProcessor<NetworkState> = BehaviorProcessor.createDefault(NetworkState.from(isConnected))
    val networkState: Flowable<NetworkState> = _networkState.distinctUntilChanged().hide()

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
                _deviceState.onNext(DeviceState.INTERACTIVE)
            }
        }
    }

    private val screenOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                _deviceState.onNext(DeviceState.NON_INTERACTIVE)
            }
        }
    }

    init {
        // Listen for all network changes (Wi-Fi, Cellular, etc.).
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager?.registerNetworkCallback(networkRequest, this)

        context.registerReceiver(userPresentReceiver, IntentFilter(Intent.ACTION_USER_PRESENT))
        context.registerReceiver(screenOnReceiver, IntentFilter(Intent.ACTION_SCREEN_ON))
        context.registerReceiver(screenOffReceiver, IntentFilter(Intent.ACTION_SCREEN_OFF))
    }

    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        _networkState.onNext(NetworkState.from(isConnected))
    }

    override fun onLost(network: Network) {
        super.onLost(network)
        _networkState.onNext(NetworkState.from(isConnected))
    }

    override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
        super.onCapabilitiesChanged(network, networkCapabilities)
        _networkState.onNext(NetworkState.from(networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)))
    }
}
