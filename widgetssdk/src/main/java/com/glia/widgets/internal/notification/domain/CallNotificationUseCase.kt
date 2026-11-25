package com.glia.widgets.internal.notification.domain

import com.glia.androidsdk.comms.MediaDirection
import com.glia.androidsdk.comms.MediaState

internal class CallNotificationUseCase {
    /**
     * Returns the live call that the visitor and operator media describe, or null when neither side sends media.
     *
     * @throws IllegalStateException for combinations GliaHub does not support. They show up briefly while
     * the update for one side has arrived and the update for the other side has not.
     */
    operator fun invoke(visitorMedia: MediaState, operatorMedia: MediaState): CallNotification? {
        val audio = getAudioDirection(visitorMedia, operatorMedia)
        val video = getVideoDirection(visitorMedia, operatorMedia)
        if (audio == MediaDirection.NONE && video == MediaDirection.NONE) return null

        return CallNotification(CallNotification.Phase.LIVE, video, hasAudio = audio == MediaDirection.TWO_WAY)
    }

    private fun getVideoDirection(visitorMedia: MediaState, operatorMedia: MediaState): MediaDirection {
        if (operatorMedia.video == null && visitorMedia.video == null) {
            return MediaDirection.NONE
        }
        if (operatorMedia.video == null && visitorMedia.video != null) {
            throw IllegalStateException("GliaHub does not support one-way visitor video calls")
        }
        if (operatorMedia.video != null && visitorMedia.video == null) {
            return MediaDirection.ONE_WAY
        }
        return MediaDirection.TWO_WAY
    }

    private fun getAudioDirection(visitorMedia: MediaState, operatorMedia: MediaState): MediaDirection {
        if (operatorMedia.audio == null && visitorMedia.audio == null) {
            return MediaDirection.NONE
        }
        if (operatorMedia.audio == null || visitorMedia.audio == null) {
            throw IllegalStateException("GliaHub does not support one-way audio calls")
        }
        return MediaDirection.TWO_WAY
    }
}
