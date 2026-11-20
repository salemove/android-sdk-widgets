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
import androidx.core.content.IntentCompat
import com.glia.androidsdk.comms.MediaDirection
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.internal.notification.NotificationFactory
import com.glia.widgets.internal.notification.domain.CallNotification

private const val EXTRA_CALL = "call"
private const val EXTRA_USES_MICROPHONE = "uses_microphone"
private const val EXTRA_USES_CAMERA = "uses_camera"

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
 * Runs as a foreground service from the moment the visitor asks for an audio or video call until the call ends.
 * This keeps the media process alive and the microphone and camera usable when the app is backgrounded.
 */
internal class CallForegroundService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val call = intent?.let { IntentCompat.getParcelableExtra(it, EXTRA_CALL, CallNotification::class.java) } ?: DEFAULT_CALL
        // The caller asks for a type only when the visitor sends that media and granted its permission.
        // Asking for the microphone type without RECORD_AUDIO makes startForeground throw on Android 14+.
        val usesMicrophone = intent?.getBooleanExtra(EXTRA_USES_MICROPHONE, true) ?: true
        val usesCamera = intent?.getBooleanExtra(EXTRA_USES_CAMERA, false) ?: false
        val microphone = if (usesMicrophone) FOREGROUND_TYPE_MICROPHONE else 0
        val camera = if (usesCamera) FOREGROUND_TYPE_CAMERA else 0
        val serviceType = microphone or camera
        val notification = NotificationFactory.createCallNotification(this, call)

        try {
            ServiceCompat.startForeground(
                this,
                NotificationFactory.CALL_NOTIFICATION_ID,
                notification,
                serviceType
            )
        } catch (error: Throwable) {
            // Android counts the startForegroundService() promise as kept before it checks the types and the app state,
            // so stopping here does not crash the app. A service that never became foreground must not keep running.
            Logger.e(TAG, "Failed to promote call service to foreground", error)
            stopSelf()
            Dependencies.ongoingCallNotificationManager.onCallServiceStopped()
        }

        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        getSystemService(NotificationManager::class.java)
            .cancel(NotificationFactory.CALL_NOTIFICATION_ID)
        stopSelf()
        Dependencies.ongoingCallNotificationManager.onCallServiceStopped()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    internal companion object {
        private const val TAG = "CallForegroundService"
        private val DEFAULT_CALL = CallNotification(CallNotification.Phase.LIVE, MediaDirection.NONE, hasAudio = true)

        fun stop(context: Context) {
            context.stopService(Intent(context, CallForegroundService::class.java))
        }

        // Returns false when the system refused the start, for example because the app is in the background.
        fun start(context: Context, call: CallNotification, usesMicrophone: Boolean, usesCamera: Boolean): Boolean {
            val intent = Intent(context, CallForegroundService::class.java).apply {
                putExtra(EXTRA_CALL, call)
                putExtra(EXTRA_USES_MICROPHONE, usesMicrophone)
                putExtra(EXTRA_USES_CAMERA, usesCamera)
            }
            return try {
                ContextCompat.startForegroundService(context, intent)
                true
            } catch (error: Throwable) {
                Logger.e(TAG, "Failed to start CallForegroundService", error)
                false
            }
        }
    }
}
