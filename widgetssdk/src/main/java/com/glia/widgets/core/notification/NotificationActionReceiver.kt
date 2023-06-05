package com.glia.widgets.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.glia.widgets.core.screensharing.ScreenSharingController
import com.glia.widgets.di.Dependencies

class NotificationActionReceiver : BroadcastReceiver() {
    private val controller: ScreenSharingController by lazy {
        Dependencies.getControllerFactory().screenSharingController
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (ACTION_ON_SCREEN_SHARING_END_PRESSED == intent.action) {
            onScreenSharingEndPressed()
        }
    }

    private fun onScreenSharingEndPressed() {
        controller.onScreenSharingNotificationEndPressed()
    }

    companion object {
        // NB - literals should match with ones in the manifest
        private const val ACTION_ON_SCREEN_SHARING_END_PRESSED =
            "com.glia.widgets.core.notification.NotificationActionReceiver.ACTION_ON_SCREEN_SHARING_END_PRESSED"

        @JvmStatic
        fun getScreenSharingEndPressedActionIntent(context: Context): Intent =
            Intent(context, NotificationActionReceiver::class.java)
                .setAction(ACTION_ON_SCREEN_SHARING_END_PRESSED)
    }
}
