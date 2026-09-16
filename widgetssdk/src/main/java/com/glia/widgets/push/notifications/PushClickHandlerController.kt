package com.glia.widgets.push.notifications

import android.os.Bundle
import androidx.annotation.VisibleForTesting
import com.glia.androidsdk.visitor.Visitor
import com.glia.telemetry_lib.EventAttribute
import com.glia.telemetry_lib.GliaLogger
import com.glia.telemetry_lib.LogEvents
import com.glia.telemetry_lib.PushType
import com.glia.widgets.chat.Intention
import com.glia.widgets.chat.domain.IsAuthenticatedUseCase
import com.glia.widgets.di.GliaCore
import com.glia.widgets.engagement.State
import com.glia.widgets.engagement.domain.EngagementStateUseCase
import com.glia.widgets.engagement.domain.IsQueueingOrLiveEngagementUseCase
import com.glia.widgets.fcm.PushMessageType
import com.glia.widgets.fcm.toWidgetsType
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.launcher.ConfigurationManager
import com.glia.widgets.view.dialog.UiComponentsDispatcher
import io.reactivex.rxjava3.disposables.Disposable
import java.util.concurrent.TimeUnit

internal interface PushClickHandlerController {
    fun handlePushClick(queueId: String?, visitorId: String)
    fun onAuthenticationAttempt()

    /**
     * Handles a tap on an engagement push notification. Safe to call with any bundle.
     */
    fun handlePushNotificationClick(bundle: Bundle?)

    /**
     * Must be called once `GliaWidgets.init` has finished, to flush a launch parked before it.
     */
    fun onSdkInitialized()
}

// Intent extra that Firebase puts on every notification it delivers, used here to recognise a repeated call
private const val GOOGLE_MESSAGE_ID_KEY = "google.message_id"

/**
 * How long to wait for the engagement the notification is about to be restored over the socket.
 *
 * Core reports initialization complete as soon as site info and the visitor session are available, while
 * the engagement is restored later. There is no "restoration settled" signal to wait for — `NoEngagement`
 * is also the initial state — so a timeout is unavoidable.
 */
@VisibleForTesting
internal const val ENGAGEMENT_RESTORE_TIMEOUT_SEC = 10L

private class PendingPn(val queueId: String?, val visitorId: String)

internal class PushClickHandlerControllerImpl(
    private val configurationManager: ConfigurationManager,
    private val uiComponentsDispatcher: UiComponentsDispatcher,
    private val gliaCore: GliaCore,
    private val isQueueingOrLiveEngagementUseCase: IsQueueingOrLiveEngagementUseCase,
    private val isAuthenticatedUseCase: IsAuthenticatedUseCase,
    private val engagementStateUseCase: EngagementStateUseCase
) : PushClickHandlerController {

    // Presence of this object indicates that we're waiting for authentication
    private var pendingPn: PendingPn? = null

    // Presence of this value indicates an engagement push click waiting for initialization or authentication
    private var pendingPushMessageType: PushMessageType? = null

    // `google.message_id` of the last click handled, so one notification is never acted on twice
    private var handledMessageIds: MutableSet<String> = mutableSetOf()

    private var sdkInitialized: Boolean = false

    private var engagementRestoreDisposable: Disposable? = null

    private val isSdkInitialized: Boolean
        get() = sdkInitialized || gliaCore.isInitialized

    private val intention: Intention
        get() = when {
            isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement -> Intention.RETURN_TO_CHAT
            else -> Intention.SC_CHAT
        }

    override fun handlePushClick(queueId: String?, visitorId: String) {
        if (isAuthenticatedUseCase() || isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement) {
            // If the user is authenticated or has an ongoing engagement, we can handle the push notification immediately
            handlePendingPushNotification(PendingPn(queueId, visitorId))
        } else {
            pendingPn = PendingPn(queueId, visitorId)
        }

        GliaLogger.i(LogEvents.PUSH_NOTIFICATIONS_CLICKED) {
            put(EventAttribute.PushType, PushType.SECURE_MESSAGE)
        }
    }

    override fun handlePushNotificationClick(bundle: Bundle?) {
        val pushMessageType = gliaCore.pushNotifications.parsePushMessageType(bundle)?.toWidgetsType() ?: return

        val messageId = bundle?.getString(GOOGLE_MESSAGE_ID_KEY) ?: return
        if (handledMessageIds.contains(messageId)) {
            Logger.d(TAG, "Push notification click is already handled, the repeated call is ignored.")
            return
        }
        handledMessageIds.add(messageId)

        when (pushMessageType) {
            PushMessageType.CHAT_MESSAGE, PushMessageType.QUEUED_MESSAGE -> {
                logPushNotificationClicked(pushMessageType)
                handleEngagementPushClick(pushMessageType)
            }

            // Secure conversation pushes are displayed by the SDK itself and their clicks arrive through
            // `PushClickHandlerActivity`, never through the integrator's Activity intent.
            PushMessageType.SECURE_CONVERSATION -> Logger.i(
                TAG,
                "Secure conversation push click is handled by the SDK itself and processed here with no actions."
            )

            PushMessageType.UNIDENTIFIED -> Logger.i(
                TAG,
                "Push click of a type unsupported by this SDK version is processed with no actions."
            )
        }
    }

    override fun onSdkInitialized() {
        sdkInitialized = true
        val pushMessageType = pendingPushMessageType ?: return
        pendingPushMessageType = null
        dispatchEngagementPushClick(pushMessageType)
    }

    override fun onAuthenticationAttempt() {
        pendingPn?.let {
            handlePendingPushNotification(it)
            pendingPn = null
        }
        pendingPushMessageType?.let {
            pendingPushMessageType = null
            dispatchEngagementPushClick(it)
        }
    }

    private fun handleEngagementPushClick(pushMessageType: PushMessageType) {
        if (!isSdkInitialized) {
            Logger.i(TAG, "Push click is parked until the SDK is initialized.")
            pendingPushMessageType = pushMessageType
            return
        }
        dispatchEngagementPushClick(pushMessageType)
    }

    private fun dispatchEngagementPushClick(pushMessageType: PushMessageType) {
        when (pushMessageType) {
            // The engagement is restored after initialization completes. Opening the chat screen before it
            // lands would read as "no engagement" and start a new one, ending the one the push was about.
            PushMessageType.CHAT_MESSAGE -> awaitEngagementRestore()

            // Secure conversations have no live engagement to wait for, only an authenticated visitor.
            PushMessageType.QUEUED_MESSAGE -> awaitAuthentication(pushMessageType)

            else -> Unit
        }
    }

    private fun awaitAuthentication(pushMessageType: PushMessageType) {
        if (isAuthenticatedUseCase()) {
            uiComponentsDispatcher.launchChatScreen(Intention.SC_CHAT)
            Logger.i(TAG, "Queued message push click is processed to open the chat transcript.")
        } else {
            Logger.i(TAG, "Push click is parked until the visitor is authenticated.")
            pendingPushMessageType = pushMessageType
        }
    }

    private fun awaitEngagementRestore() {
        if (isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement) {
            uiComponentsDispatcher.launchChatScreen(Intention.RETURN_TO_CHAT)
            Logger.i(TAG, "Chat message push click is processed to return to the ongoing engagement.")
            return
        }

        engagementRestoreDisposable?.dispose()
        engagementRestoreDisposable = engagementStateUseCase()
            .filter(State::isLiveEngagement)
            .firstOrError()
            .timeout(ENGAGEMENT_RESTORE_TIMEOUT_SEC, TimeUnit.SECONDS)
            .subscribe(
                {
                    uiComponentsDispatcher.launchChatScreen(Intention.RETURN_TO_CHAT)
                    Logger.i(TAG, "Chat message push click is processed to return to the restored engagement.")
                },
                { Logger.i(TAG, "No engagement was restored in time, the push notification is treated as stale.") }
            )
    }

    private fun logPushNotificationClicked(pushMessageType: PushMessageType) {
        // `com.glia.telemetry_lib.PushType` has no constant for `queued_message.created` yet. Sending no
        // push type at all is better than sending a wrong one.
        val telemetryPushType: String? = PushType.MESSAGE.takeIf { pushMessageType == PushMessageType.CHAT_MESSAGE }

        GliaLogger.i(LogEvents.PUSH_NOTIFICATIONS_CLICKED) {
            telemetryPushType?.let { put(EventAttribute.PushType, it) }
        }
    }

    private fun handlePendingPushNotification(pendingPn: PendingPn) {
        gliaCore.getCurrentVisitor(onSuccess = {
            handleVisitorAuthenticated(it, pendingPn)
        })
    }

    private fun handleVisitorAuthenticated(visitor: Visitor, pendingPn: PendingPn) {
        when {
            !visitor.isVisitorAuthenticated -> Logger.i(
                TAG,
                "Secure message push with unauthenticated visitor is processed with no actions."
            )

            visitor.visitorId != pendingPn.visitorId -> Logger.i(
                TAG,
                "Secure message push with unmatching `visitor_id` processed with no actions."
            )

            else -> {
                configurationManager.setQueueIds(listOfNotNull(pendingPn.queueId))
                uiComponentsDispatcher.launchChatScreen(intention)

                Logger.i(
                    TAG,
                    "Secure message push with matching `visitor_id` and authenticated visitor is processed to open chat transcript."
                )
            }
        }
    }

}
