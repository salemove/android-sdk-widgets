package com.glia.widgets.internal.notification.domain

import android.os.Parcelable
import com.glia.androidsdk.comms.MediaDirection
import kotlinx.parcelize.Parcelize

/**
 * What the ongoing call notification shows.
 * [video] and [hasAudio] describe the call the visitor asked for or is in, so they also decide the foreground service types.
 */
@Parcelize
internal data class CallNotification(val phase: Phase, val video: MediaDirection, val hasAudio: Boolean) : Parcelable {
    enum class Phase {
        /** The visitor is in the queue for an audio or video engagement. */
        WAITING,

        /** An operator is connected or an upgrade was accepted, but the call media is not live yet. */
        CONNECTING,
        LIVE
    }
}
