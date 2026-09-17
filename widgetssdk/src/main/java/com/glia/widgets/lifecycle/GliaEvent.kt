package com.glia.widgets.lifecycle

import com.glia.widgets.engagement.MediaType

/**
 * An event describing a change in the Widgets SDK's engagement lifecycle.
 *
 * @see com.glia.widgets.GliaWidgets.subscribeToEvents
 */
sealed class GliaEvent {
    /**
     * An engagement has started.
     */
    data object EngagementStarted : GliaEvent()

    /**
     * An engagement is ongoing, with the given media type.
     *
     * Emitted when an engagement starts and whenever its media type changes,
     * for example when a chat engagement is upgraded to an audio or video call.
     *
     * @property mediaType The current engagement media type: [MediaType.TEXT], [MediaType.AUDIO], or [MediaType.VIDEO].
     */
    data class EngagementOngoing(val mediaType: MediaType) : GliaEvent()

    /**
     * The engagement has ended.
     */
    data object EngagementEnded : GliaEvent()
}
