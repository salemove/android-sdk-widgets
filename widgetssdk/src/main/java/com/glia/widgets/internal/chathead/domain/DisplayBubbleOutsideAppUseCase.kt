package com.glia.widgets.internal.chathead.domain

import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.internal.chathead.ChatHeadManager
import com.glia.widgets.internal.permissions.PermissionManager
import com.glia.widgets.launcher.ConfigurationManager
/**
 * This use case:
 * 1) determines whether the chat head (bubble) should be displayed
 * 2) starts or stops the chat head service (bubble outside the app)
 */
internal class DisplayBubbleOutsideAppUseCase(
    private val chatHeadManager: ChatHeadManager,
    private val permissionManager: PermissionManager,
    private val configurationManager: ConfigurationManager,
    private val isBubbleNeededUseCase: IsBubbleNeededUseCase
) {

    /**
     * "Global" device bubble should be displayed only when allowed.
     *
     * @return `true` when:
     * global device bubble is NOT turned off by integrator (turned ON by default)
     * AND
     * global device bubble is allowed by visitor (overlay permission).
     */
    private val isBubbleAllowedOutsideApp: Boolean
        get() = configurationManager.enableBubbleOutsideApp && permissionManager.hasOverlayPermission()

    operator fun invoke(viewName: String?) = when {
        // If bubble is not allowed outside app, return false immediately to not trigger unnecessary functions
        !isBubbleAllowedOutsideApp -> {
            Logger.d(TAG, "Bubble: not allowed to show device bubble")
        }

        // App is in background, that means the bubble is needed
        viewName == null -> showBubble()

        // App is in foreground and bubble is needed based on engagement and screen
        isBubbleNeededUseCase(viewName) -> showBubble()

        // App is in foreground and bubble is NOT needed based on engagement and screen
        else -> hideBubble()
    }

    private fun showBubble() {
        chatHeadManager.startChatHeadService()
        Logger.i(TAG, "Bubble: show device bubble")
    }

    private fun hideBubble() {
        chatHeadManager.stopChatHeadService()
        Logger.d(TAG, "Bubble: hide device bubble")
    }

    fun onDestroy() {
        chatHeadManager.stopChatHeadService()
    }
}
