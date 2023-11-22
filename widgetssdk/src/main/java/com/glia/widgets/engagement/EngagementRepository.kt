package com.glia.widgets.engagement

import com.glia.androidsdk.IncomingEngagementRequest
import com.glia.androidsdk.Operator
import com.glia.androidsdk.comms.MediaState
import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.widgets.helper.Data
import io.reactivex.Flowable

internal interface EngagementRepository {
    val engagementRequest: Flowable<IncomingEngagementRequest>
    val engagementState: Flowable<State>
    val survey: Flowable<SurveyState>
    val currentOperator: Flowable<Data<Operator>>
    val operatorTypingStatus: Flowable<Boolean>
    val mediaUpgradeOffer: Flowable<MediaUpgradeOffer>
    val mediaUpgradeOfferAcceptResult: Flowable<Result<MediaUpgradeOffer>>
    val visitorMediaState: Flowable<Data<MediaState>>
    val visitorCurrentMediaState: MediaState?
    val onHoldState: Flowable<Boolean>
    val operatorMediaState: Flowable<Data<MediaState>>
    val operatorCurrentMediaState: MediaState?

    val hasOngoingEngagement: Boolean
    val isCallVisualizerEngagement: Boolean
    val isOperatorPresent: Boolean

    fun initialize()
    fun reset()
    fun endEngagement(silently: Boolean)
    fun acceptCurrentEngagementRequest(visitorContextAssetId: String)
    fun declineCurrentEngagementRequest()
    fun acceptMediaUpgradeRequest(offer: MediaUpgradeOffer)
    fun declineMediaUpgradeRequest(offer: MediaUpgradeOffer)
    fun muteVisitorAudio()
    fun unMuteVisitorAudio()
    fun pauseVisitorVideo()
    fun resumeVisitorVideo()
}
