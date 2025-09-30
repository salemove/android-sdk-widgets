package com.glia.widgets.push.notifications

import com.glia.androidsdk.visitor.Visitor
import com.glia.telemetry_lib.EventAttribute
import com.glia.telemetry_lib.GliaLogger
import com.glia.telemetry_lib.LogEvents
import com.glia.telemetry_lib.PushType
import com.glia.widgets.chat.Intention
import com.glia.widgets.chat.domain.IsAuthenticatedUseCase
import com.glia.widgets.di.GliaCore
import com.glia.widgets.engagement.domain.IsQueueingOrLiveEngagementUseCase
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.launcher.ConfigurationManager
import com.glia.widgets.view.dialog.UiComponentsDispatcher

internal interface PushClickHandlerController {
    fun handlePushClick(queueId: String?, visitorId: String)
    fun onAuthenticationAttempt()
}

private class PendingPn(val queueId: String?, val visitorId: String)

internal class PushClickHandlerControllerImpl(
    private val configurationManager: ConfigurationManager,
    private val uiComponentsDispatcher: UiComponentsDispatcher,
    private val gliaCore: GliaCore,
    private val isQueueingOrLiveEngagementUseCase: IsQueueingOrLiveEngagementUseCase,
    private val isAuthenticatedUseCase: IsAuthenticatedUseCase
) : PushClickHandlerController {

    // Presence of this object indicates that we're waiting for authentication
    private var pendingPn: PendingPn? = null

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

    override fun onAuthenticationAttempt() {
        handlePendingPushNotification(pendingPn ?: return)
        pendingPn = null
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
