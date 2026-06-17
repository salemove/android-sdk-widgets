package com.glia.widgets.internal.notification.device

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.internal.notification.NotificationFactory

private const val EXTRA_CALL_TYPE = "call_type"
private const val EXTRA_HAS_AUDIO = "has_audio"

private const val CALL_TYPE_AUDIO = 0
private const val CALL_TYPE_ONE_WAY_VIDEO = 1
private const val CALL_TYPE_TWO_WAY_VIDEO = 2

// ServiceInfo constants are compile-time inlined, so safe on minSdk 24.
// ServiceCompat.startForeground ignores the type param on API < 29.
@SuppressLint("InlinedApi")
private val FOREGROUND_TYPE_MICROPHONE = ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE

@SuppressLint("InlinedApi")
private val FOREGROUND_TYPE_CAMERA = ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA

/**
 * Glia internal class.
 *
 * It will be automatically added to the integrator's manifest file by the manifest merger during compilation.
 *
 * Runs as a foreground service during active audio/video calls to prevent the OS from killing
 * the media process when the app is backgrounded.
 */
internal class CallForegroundService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val callType = intent?.getIntExtra(EXTRA_CALL_TYPE, CALL_TYPE_AUDIO) ?: CALL_TYPE_AUDIO
        val hasAudio = intent?.getBooleanExtra(EXTRA_HAS_AUDIO, true) ?: true

        val notification = when (callType) {
            CALL_TYPE_TWO_WAY_VIDEO -> NotificationFactory.createVideoCallNotification(
                context = this,
                isTwoWayVideo = true,
                hasAudio = hasAudio
            )
            CALL_TYPE_ONE_WAY_VIDEO -> NotificationFactory.createVideoCallNotification(
                context = this,
                isTwoWayVideo = false,
                hasAudio = hasAudio
            )
            else -> NotificationFactory.createAudioCallNotification(this)
        }

        // Two-way video: visitor has both microphone and camera active.
        // All other call types: visitor has microphone only.
        val serviceType = if (callType == CALL_TYPE_TWO_WAY_VIDEO) {
            FOREGROUND_TYPE_MICROPHONE or FOREGROUND_TYPE_CAMERA
        } else {
            FOREGROUND_TYPE_MICROPHONE
        }

        try {
            ServiceCompat.startForeground(
                this,
                NotificationFactory.CALL_NOTIFICATION_ID,
                notification,
                serviceType
            )
        } catch (error: Throwable) {
            Logger.e(TAG, "Failed to promote call service to foreground", error)
        }

        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        getSystemService(NotificationManager::class.java)
            .cancel(NotificationFactory.CALL_NOTIFICATION_ID)
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    internal companion object {
        private const val TAG = "CallForegroundService"

        fun startAudio(context: Context) {
            start(context, CALL_TYPE_AUDIO, hasAudio = true)
        }

        fun startVideo(context: Context, isTwoWayVideo: Boolean, hasAudio: Boolean) {
            val callType = if (isTwoWayVideo) CALL_TYPE_TWO_WAY_VIDEO else CALL_TYPE_ONE_WAY_VIDEO
            start(context, callType, hasAudio = hasAudio)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, CallForegroundService::class.java))
        }

        private fun start(context: Context, callType: Int, hasAudio: Boolean) {
            val intent = Intent(context, CallForegroundService::class.java).apply {
                putExtra(EXTRA_CALL_TYPE, callType)
                putExtra(EXTRA_HAS_AUDIO, hasAudio)
            }
            try {
                ContextCompat.startForegroundService(context, intent)
            } catch (error: Throwable) {
                Logger.e(TAG, "Failed to start CallForegroundService", error)
            }
        }
    }
}
