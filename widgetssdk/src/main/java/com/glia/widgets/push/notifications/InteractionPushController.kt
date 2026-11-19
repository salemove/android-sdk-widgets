package com.glia.widgets.push.notifications

import android.content.Context
import androidx.lifecycle.Lifecycle
import com.glia.widgets.helper.ApplicationLifecycleManager
import com.glia.widgets.helper.IntentHelper
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.internal.notification.device.INotificationManager
import com.glia.widgets.internal.permissions.domain.IsNotificationPermissionGrantedUseCase

internal interface InteractionPushController {
    /**
     * Shows the notification of a live push registered through the push subscription.
     *
     * Those pushes are data-only, so the system shows nothing by itself. Webhook pushes keep their
     * notification block and must not be passed here, or they would be shown twice.
     */
    fun handleInteractionPush(context: Context, data: Map<String, String>, messageId: String?)
}

// Push notification key for the alert text
private const val BODY_KEY = "body"

internal class InteractionPushControllerImpl(
    private val applicationLifecycleManager: ApplicationLifecycleManager,
    private val notificationManager: INotificationManager,
    private val isNotificationPermissionGrantedUseCase: IsNotificationPermissionGrantedUseCase,
    private val intentHelper: IntentHelper
) : InteractionPushController {

    init {
        notificationManager.createInteractionChannel()
    }

    private val isOnForeground: Boolean
        get() = applicationLifecycleManager.isAtLeast(Lifecycle.State.RESUMED)

    override fun handleInteractionPush(context: Context, data: Map<String, String>, messageId: String?) {
        // As the system does for a push with a notification block, nothing is shown while the app is open.
        if (isOnForeground || !isNotificationPermissionGrantedUseCase()) return

        val body: String = data[BODY_KEY]?.takeIf { it.isNotBlank() } ?: run {
            Logger.i(TAG, "Interaction push has no alert text, so no notification is shown.")
            return
        }
        val contentIntent = intentHelper.pushNotificationLaunchPendingIntent(context, data, messageId) ?: run {
            Logger.i(TAG, "The application has no launch activity, so no interaction push notification is shown.")
            return
        }
        notificationManager.showInteractionNotification(body, contentIntent)
    }
}
