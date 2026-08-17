package com.glia.widgets.internal.chathead.domain

import com.glia.widgets.engagement.domain.EngagementTypeUseCase
import com.glia.widgets.engagement.domain.IsQueueingOrLiveEngagementUseCase
import com.glia.widgets.internal.chathead.BubbleContext
import com.glia.widgets.internal.chathead.BubbleRenderTarget
import com.glia.widgets.internal.chathead.VisibleScreen
import com.glia.widgets.internal.permissions.PermissionManager
import com.glia.widgets.launcher.ConfigurationManager

/**
 * Decides where the bubble renders for a given [BubbleContext], and whether the overlay service needs
 * to be running at all. Pure — starting and stopping the service, and the telemetry that goes with it,
 * is the controller's job.
 *
 * The two settings are orthogonal: `enableBubbleOutsideApp` governs the overlay bubble the visitor
 * sees once the app is in the background, `enableBubbleInsideApp` governs the bubble drawn inside the
 * app. Whichever layer is on screen, the app is never showing both.
 */
internal class DecideBubbleRenderTargetUseCase(
    private val permissionManager: PermissionManager,
    private val configurationManager: ConfigurationManager,
    private val isQueueingOrLiveEngagementUseCase: IsQueueingOrLiveEngagementUseCase,
    private val engagementTypeUseCase: EngagementTypeUseCase
) {

    /**
     * Whether `ChatHeadService` should be running, which is deliberately *not* the same question as
     * whether the overlay bubble is currently visible.
     *
     * It ignores the visible screen so that the service is already up by the time the app is
     * backgrounded: API 26+ refuses to start a service from the background, so a service started only
     * at that moment would be racing the restriction. The service adds and removes its own window as
     * the render target changes.
     */
    val isOverlayServiceNeeded: Boolean
        get() = isBubbleAllowedOutsideApp && hasEngagementToReturnTo

    /**
     * The overlay bubble is allowed only when the integrator has not turned it off (it is on by
     * default) and the visitor has granted the overlay permission.
     */
    private val isBubbleAllowedOutsideApp: Boolean
        get() = configurationManager.enableBubbleOutsideApp && permissionManager.hasOverlayPermission()

    /**
     * There is somewhere for a tap to go back to: the visitor is waiting in a queue, or is in a live
     * engagement. Deliberately does not wait for the operator to connect — the bubble shows a
     * placeholder until then, rather than disappearing between the queue ending and the operator
     * arriving.
     */
    private val hasEngagementToReturnTo: Boolean
        get() = isQueueingOrLiveEngagementUseCase.isQueueing ||
            (isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement && isReturnableLiveEngagement)

    /**
     * A Call Visualizer engagement is only worth a bubble once it carries audio/video: until then
     * there is no chat or call screen behind it to return to.
     */
    private val isReturnableLiveEngagement: Boolean
        get() = !engagementTypeUseCase.isCallVisualizer || engagementTypeUseCase.hasMedia

    /**
     * Looking at the chat screen during an audio/video engagement always shows the bubble, so the
     * visitor can get back to the call. This is the only rule that cares about media, and the only one
     * that overrides `enableBubbleInsideApp`.
     */
    private fun isChatScreenDuringMediaEngagement(screen: VisibleScreen): Boolean =
        screen == VisibleScreen.CHAT && engagementTypeUseCase.isMediaEngagement

    private fun isBubbleNeededOn(screen: VisibleScreen): Boolean =
        hasEngagementToReturnTo && !screen.hidesBubble

    operator fun invoke(context: BubbleContext): BubbleRenderTarget = when (context) {
        // Nothing in the app can host a bubble, so the overlay is the only layer left
        BubbleContext.AppInBackground ->
            if (isOverlayServiceNeeded) BubbleRenderTarget.SERVICE else BubbleRenderTarget.NONE

        is BubbleContext.InApp -> when {
            // Must be checked before enableBubbleInsideApp - it overrides that setting
            isChatScreenDuringMediaEngagement(context.visibleScreen) -> BubbleRenderTarget.APPLICATION
            !configurationManager.enableBubbleInsideApp -> BubbleRenderTarget.NONE
            isBubbleNeededOn(context.visibleScreen) -> BubbleRenderTarget.APPLICATION
            else -> BubbleRenderTarget.NONE
        }
    }
}
