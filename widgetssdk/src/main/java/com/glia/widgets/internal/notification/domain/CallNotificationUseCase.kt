package com.glia.widgets.internal.notification.domain

import com.glia.androidsdk.comms.MediaDirection
import com.glia.androidsdk.comms.MediaState
import com.glia.widgets.internal.notification.device.INotificationManager
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG

internal class CallNotificationUseCase(
    private val notificationManager: INotificationManager
) {
    operator fun invoke(visitorMedia: MediaState? = null, operatorMedia: MediaState? = null) {
        val audio: MediaDirection
        val video: MediaDirection
        try {
            audio = getAudioDirection(visitorMedia, operatorMedia)
            video = getVideoDirection(visitorMedia, operatorMedia)
        } catch (error: IllegalStateException) {
            // One of the impossible scenario was detected during parsing of the states which is not supported by current GliaHub
            Logger.i(TAG, "Unsupported request to show/update/hide media call notification \nDetails: ${error.message}")
            return
        }

        if (video == MediaDirection.NONE && audio == MediaDirection.NONE) {
            notificationManager.removeCallNotification()
        } else if (video == MediaDirection.NONE) {
            notificationManager.showAudioCallNotification()
        } else {
            notificationManager.showVideoCallNotification(
                isTwoWayVideo = video == MediaDirection.TWO_WAY,
                hasAudio = audio == MediaDirection.TWO_WAY
            )
        }
    }

    fun removeAllNotifications() {
        // If operator and visitor is null it would conclude that there is no audio or video and will remove notification
        invoke(null, null)
    }

    private fun getVideoDirection(visitorMedia: MediaState?, operatorMedia: MediaState?): MediaDirection {
        if (operatorMedia?.video == null && visitorMedia?.video == null) {
            return MediaDirection.NONE
        }
        if (operatorMedia?.video == null && visitorMedia?.video != null) {
            throw IllegalStateException("GliaHub does not support one-way visitor video calls")
        }
        if (operatorMedia?.video != null && visitorMedia?.video == null) {
            return MediaDirection.ONE_WAY
        }
        return MediaDirection.TWO_WAY
    }

    private fun getAudioDirection(visitorMedia: MediaState?, operatorMedia: MediaState?): MediaDirection {
        if (operatorMedia?.audio == null && visitorMedia?.audio == null) {
            return MediaDirection.NONE
        }
        if (operatorMedia?.audio == null && visitorMedia?.audio != null) {
            throw IllegalStateException("GliaHub does not support one-way audio calls")
        }
        if (operatorMedia?.audio != null && visitorMedia?.audio == null) {
            throw IllegalStateException("GliaHub does not support one-way audio calls")
        }
        return MediaDirection.TWO_WAY
    }
}
