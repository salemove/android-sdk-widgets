package com.glia.widgets.internal.chathead

import com.glia.widgets.internal.chathead.domain.ResolveChatHeadNavigationUseCase
import kotlinx.coroutines.flow.StateFlow

internal interface ChatBubbleContract {

    /**
     * Owns every bubble business rule and publishes one [BubbleUiModel] for the hosts to draw.
     * Hosts are pure renderers: they collect [uiState] and report lifecycle through [onContextEvent].
     */
    interface Controller {
        val uiState: StateFlow<BubbleUiModel>

        /**
         * Where the visitor dragged the bubble, shared by both hosts so that it does not jump when the
         * app moves between foreground and background — see [BubblePosition] for why it is fractions
         * and not pixels. `null` until the first drag; hosts fall back to their default corner. Held
         * here rather than in a host because both of them are recreated far more often than the
         * bubble's position changes.
         */
        var bubblePosition: BubblePosition?

        fun onContextEvent(event: BubbleContextEvent)

        /** Resolves where a tap should take the visitor. The host performs the navigation. */
        fun onBubbleTapped(): ResolveChatHeadNavigationUseCase.Destinations

        /** Whether chat was opened from the call screen — the watcher finishes `ChatActivity` in that case. */
        fun isFromCallScreen(): Boolean

        fun resetFromCallScreen()

        /** SDK teardown: stop the overlay service, reset state, drop the unread-count subscription. */
        fun onDestroy()
    }
}
