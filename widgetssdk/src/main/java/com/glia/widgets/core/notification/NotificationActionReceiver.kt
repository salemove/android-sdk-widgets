package com.glia.widgets.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.glia.widgets.di.Dependencies
import com.glia.widgets.engagement.domain.EndScreenSharingUseCase
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG

/**
 * Glia internal class.
 *
 * It will be automatically added to the integrator's manifest file by the manifest merger during compilation.
 *
 * This receiver is used to listen to push notification actions (e.g. button clicks).
 */
internal class NotificationActionReceiver : BroadcastReceiver() {
    private val endScreenSharingUseCase: EndScreenSharingUseCase by lazy {
        Dependencies.useCaseFactory.endScreenSharingUseCase
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (ACTION_ON_SCREEN_SHARING_END_PRESSED == intent.action) {
            onScreenSharingEndPressed()
        }
    }

    private fun onScreenSharingEndPressed() {
        Logger.i(TAG, "End screen sharing tapped from notification")
        endScreenSharingUseCase()
    }

    internal companion object {
        // NB - literals should match with ones in the manifest
        private const val ACTION_ON_SCREEN_SHARING_END_PRESSED =
            "com.glia.widgets.core.notification.NotificationActionReceiver.ACTION_ON_SCREEN_SHARING_END_PRESSED"

        @JvmStatic
        fun getScreenSharingEndPressedActionIntent(context: Context): Intent =
            Intent(context, NotificationActionReceiver::class.java)
                .setAction(ACTION_ON_SCREEN_SHARING_END_PRESSED)
    }
}
