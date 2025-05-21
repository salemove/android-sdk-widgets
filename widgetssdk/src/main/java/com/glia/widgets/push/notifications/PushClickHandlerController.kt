package com.glia.widgets.push.notifications

import com.glia.androidsdk.visitor.Visitor
import com.glia.widgets.di.GliaCore
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.launcher.ConfigurationManager
import com.glia.widgets.view.dialog.UiComponentsDispatcher

internal interface PushClickHandlerController {
    fun handlePushClick(queueId: String?, visitorId: String)
    fun onAuthenticated()
}

private class PendingPn(val queueId: String?, val visitorId: String)

internal class PushClickHandlerControllerImpl(
    private val configurationManager: ConfigurationManager,
    private val uiComponentsDispatcher: UiComponentsDispatcher,
    private val gliaCore: GliaCore
) : PushClickHandlerController {

    // Presence of this object indicates that we're waiting for authentication
    private var pendingPn: PendingPn? = null

    override fun handlePushClick(queueId: String?, visitorId: String) {
        pendingPn = PendingPn(queueId, visitorId)
    }

    override fun onAuthenticated() {
        if (pendingPn == null) return

        gliaCore.getCurrentVisitor(
            onSuccess = ::handleVisitorAuthenticated,
            onError = {
                pendingPn = null
            }
        )

    }

    private fun handleVisitorAuthenticated(visitor: Visitor) {
        when {
            !visitor.isVisitorAuthenticated -> Logger.i(
                TAG,
                "Secure message push with unauthenticated visitor is processed with no actions."
            )

            visitor.visitorId != pendingPn?.visitorId -> Logger.i(
                TAG,
                "Secure message push with unmatching `visitor_id` processed with no actions."
            )

            else -> {
                configurationManager.setQueueIds(listOfNotNull(pendingPn?.queueId))
                uiComponentsDispatcher.launchSCTranscriptActivity()

                Logger.i(
                    TAG,
                    "Secure message push with matching `visitor_id` and authenticated visitor is processed to open chat transcript."
                )
            }
        }

        // Clear pending push notification
        pendingPn = null
    }

}
