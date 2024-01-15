package com.glia.widgets.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.glia.widgets.core.screensharing.ScreenSharingContract
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG

class NotificationActionReceiver : BroadcastReceiver() {
    private val controller: ScreenSharingContract.Controller by lazy {
        Dependencies.getControllerFactory().screenSharingController
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (ACTION_ON_SCREEN_SHARING_END_PRESSED == intent.action) {
            onScreenSharingEndPressed()
        }
    }

    private fun onScreenSharingEndPressed() {
        Logger.i(TAG, "End screen sharing tapped from notification")
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
