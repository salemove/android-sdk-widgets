package com.glia.widgets.push.notifications

import android.content.Context
import androidx.lifecycle.Lifecycle
import com.glia.widgets.helper.ApplicationLifecycleManager
import com.glia.widgets.helper.IntentHelper
import com.glia.widgets.internal.notification.device.INotificationManager
import com.glia.widgets.internal.permissions.domain.IsNotificationPermissionGrantedUseCase

internal interface SecureMessagingPushController {
    fun handleSecureMessage(context: Context, queueId: String?, content: String, visitorId: String)
}

internal class SecureMessagingPushControllerImpl(
    private val applicationLifecycleManager: ApplicationLifecycleManager,
    private val notificationManager: INotificationManager,
    private val isNotificationPermissionGrantedUseCase: IsNotificationPermissionGrantedUseCase,
    private val intentHelper: IntentHelper
) : SecureMessagingPushController {

    init {
        // This class should be used with push notifications only, so at this point we are sure that
        // the notification are setup
        notificationManager.createSecureMessagingChannel()
    }

    private val isOnForeground: Boolean
        get() = applicationLifecycleManager.isAtLeast(Lifecycle.State.RESUMED)

    override fun handleSecureMessage(context: Context, queueId: String?, content: String, visitorId: String) {
        when {
            // App is on foreground, so we don't need to show a notification
            isOnForeground -> return

            // Notification permission is not granted
            !isNotificationPermissionGrantedUseCase() -> return

            else -> notificationManager.showSecureMessageNotification(
                content,
                intentHelper.pushClickHandlerPendingIntent(context, queueId, visitorId)
            )
        }
    }

}
