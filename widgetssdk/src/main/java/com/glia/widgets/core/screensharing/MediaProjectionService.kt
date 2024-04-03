package com.glia.widgets.core.screensharing

import android.app.ForegroundServiceStartNotAllowedException
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import com.glia.widgets.core.notification.NotificationFactory.createScreenSharingNotification
import com.glia.widgets.di.Dependencies
import com.glia.widgets.engagement.State
import com.glia.widgets.engagement.domain.EngagementStateUseCase
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import io.reactivex.rxjava3.disposables.CompositeDisposable

private const val SERVICE_ID = 123
const val ACTION_START = "EngagementMonitoringService:Start"

/**
 * Glia internal class.
 *
 * It will be automatically added to the integrator's manifest file by the manifest merger during compilation.
 *
 * Apps targeting SDK version 29 or later require a running foreground service for screen sharing.
 */
@RequiresApi(api = Build.VERSION_CODES.O)
class MediaProjectionService : Service() {
    private val compositeDisposable: CompositeDisposable by lazy { CompositeDisposable() }
    private val engagementStateUseCase: EngagementStateUseCase by lazy { Dependencies.getUseCaseFactory().engagementStateUseCase }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        try {
            setupAsForegroundService()
        } catch (e: Exception) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && e is ForegroundServiceStartNotAllowedException) {
                // App not in a valid state to start foreground service
                // (e.g., started from bg)
                Logger.d(TAG, "Service started from background")
            } else {
                Logger.e(TAG, "Failed to start service", e)
            }
        }
    }

    private fun setupAsForegroundService() {
        // Register this service as a foreground service.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(SERVICE_ID, createScreenSharingNotification(this), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
        } else {
            startForeground(SERVICE_ID, createScreenSharingNotification(this))
        }
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        stopSelf()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (ACTION_START == intent.action) {
            engagementStateUseCase()
                .filter { it is State.FinishedCallVisualizer || it is State.FinishedOmniCore }
                .subscribe { stopSelf() }
                .also(compositeDisposable::add)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }
}
