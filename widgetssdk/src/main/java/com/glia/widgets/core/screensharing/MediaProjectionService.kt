package com.glia.widgets.core.screensharing

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import com.glia.widgets.core.notification.NotificationFactory.createScreenSharingNotification
import com.glia.widgets.di.Dependencies
import com.glia.widgets.engagement.EngagementStateUseCase
import com.glia.widgets.engagement.State
import io.reactivex.disposables.CompositeDisposable

private const val SERVICE_ID = 123
const val ACTION_START = "EngagementMonitoringService:Start"

/**
 * Apps targeting SDK version 29 or later require for screen-sharing a running foreground service.
 * This service requires declaration in the Manifest file.
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
        setupAsForegroundService()
    }

    private fun setupAsForegroundService() {
        // Register this service as a foreground service.
        startForeground(SERVICE_ID, createScreenSharingNotification(this))
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
        compositeDisposable.clear()
        super.onDestroy()
    }
}
