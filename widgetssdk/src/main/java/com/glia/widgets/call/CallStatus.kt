package com.glia.widgets.call

import com.glia.androidsdk.comms.MediaState

/**
 * @hide
 */
internal interface CallStatus {

    val formattedOperatorName: String?

    val operatorProfileImageUrl: String?

    /**
     * In case of [EngagementOngoingOperatorIsConnecting] the time displays the time it takes to upgrade to
     * [EngagementOngoingVideoCallStarted] or [EngagementOngoingAudioCallStarted].
     * In case of [EngagementOngoingVideoCallStarted] or [EngagementOngoingAudioCallStarted] the time is the ongoing
     * call duration
     *
     * @return A string value of the time. Either 0,1,2,3 in case of [EngagementOngoingOperatorIsConnecting]
     * or MM:ss in case of [EngagementOngoingAudioCallStarted] or [EngagementOngoingVideoCallStarted]
     */
    val time: String?

    val operatorMediaState: MediaState?

    var visitorMediaState: MediaState?

    /**
     * @hide
     */
    data class EngagementNotOngoing(override var visitorMediaState: MediaState?) : CallStatus {
        override val formattedOperatorName: String? = null
        override val operatorProfileImageUrl: String? = null
        override val time: String? = null
        override val operatorMediaState: MediaState? = null
    }

    /**
     * @hide
     */
    data class EngagementOngoingOperatorIsConnecting(
        override val formattedOperatorName: String?,
        override val time: String?,
        override val operatorProfileImageUrl: String?,
        override var visitorMediaState: MediaState?
    ) : CallStatus {
        override val operatorMediaState: MediaState? = null
    }

    /**
     * @hide
     */
    data class EngagementOngoingAudioCallStarted(
        override val formattedOperatorName: String?,
        override val time: String?,
        override val operatorProfileImageUrl: String?,
        override val operatorMediaState: MediaState?,
        override var visitorMediaState: MediaState?
    ) : CallStatus

    /**
     * @hide
     */
    data class EngagementOngoingVideoCallStarted(
        override val formattedOperatorName: String?,
        override val time: String?,
        override val operatorProfileImageUrl: String?,
        override val operatorMediaState: MediaState?,
        override var visitorMediaState: MediaState?
    ) : CallStatus

    /**
     * @hide
     */
    data class EngagementOngoingTransferring(
        override val time: String?,
        override var visitorMediaState: MediaState?
    ) : CallStatus {
        override val formattedOperatorName: String? = null
        override val operatorProfileImageUrl: String? = null
        override val operatorMediaState: MediaState? = null
    }
}
