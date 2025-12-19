package com.glia.widgets.internal.chathead.domain

import com.glia.telemetry_lib.BubbleType
import com.glia.widgets.internal.permissions.PermissionManager
import com.glia.widgets.launcher.ConfigurationManager
import com.glia.widgets.view.head.ChatHeadLogger

internal class IsDisplayBubbleInsideAppUseCase(
    private val permissionManager: PermissionManager,
    private val configurationManager: ConfigurationManager,
    private val isBubbleNeededUseCase: IsBubbleNeededUseCase
) {

    val isBubbleAllowedOutsideApp: Boolean
        get() = configurationManager.enableBubbleOutsideApp && permissionManager.hasOverlayPermission()

    operator fun invoke(viewName: String?): Boolean {

        return when {
            // Use only ChatHeadService instead of ChatHeadService + app bubble if bubble is enabled outside and inside
            isBubbleAllowedOutsideApp -> false

            // The exception to all of the above is: If we’re looking at the chat screen during an Audio/Video engagement,
            // show the Bubble always irrespective of integrator settings.
            // Clicking it takes you back to Call Screen.
            // This should be before the configurationManager.enableBubbleInsideApp check!
            isBubbleNeededUseCase.isBubbleNeededByChatScreenDuringMediaEngagement(viewName) -> {
                ChatHeadLogger.logChatHeadShown(BubbleType.IN_APP)
                true
            }

            // If bubble is disabled inside app, return immediately, no need to check anything else
            !configurationManager.enableBubbleInsideApp -> false

            isBubbleNeededUseCase(viewName) -> {
                ChatHeadLogger.logChatHeadShown(BubbleType.IN_APP)
                true
            }

            else -> {
                ChatHeadLogger.logChatHeadHidden(BubbleType.IN_APP)
                false
            }
        }
    }

}
