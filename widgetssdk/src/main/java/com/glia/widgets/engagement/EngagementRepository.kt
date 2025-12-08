package com.glia.widgets.engagement

import android.content.Intent
import com.glia.androidsdk.EngagementRequest.Outcome
import com.glia.androidsdk.IncomingEngagementRequest
import com.glia.androidsdk.Operator
import com.glia.androidsdk.comms.CameraDevice
import com.glia.androidsdk.comms.MediaQuality
import com.glia.androidsdk.comms.MediaState
import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.widgets.helper.Data
import io.reactivex.rxjava3.core.Flowable

internal interface EngagementRepository {
    val engagementRequest: Flowable<IncomingEngagementRequest>
    val engagementOutcome: Flowable<Outcome>
    val engagementState: Flowable<State>
    val currentOperator: Flowable<Data<Operator>>
    val operatorTypingStatus: Flowable<Boolean>
    val mediaQuality: Flowable<MediaQuality>
    val mediaUpgradeOffer: Flowable<MediaUpgradeOffer>
    val mediaUpgradeOfferAcceptResult: Flowable<Result<MediaUpgradeOffer>>
    val visitorMediaState: Flowable<Data<MediaState>>
    val visitorCurrentMediaState: MediaState?
    val onHoldState: Flowable<Boolean>
    val operatorMediaState: Flowable<Data<MediaState>>
    val operatorCurrentMediaState: MediaState?
    val visitorCameraState: Flowable<VisitorCamera>

    val currentOperatorValue: Operator?
    val isQueueingOrLiveEngagement: Boolean
    val hasOngoingLiveEngagement: Boolean
    val isTransferredSecureConversation: Boolean
    val isQueueing: Boolean
    val isQueueingForMedia: Boolean
    val isQueueingForVideo: Boolean
    val isQueueingForAudio: Boolean
    val isCallVisualizerEngagement: Boolean
    val isOperatorPresent: Boolean
    val isSecureMessagingRequested: Boolean
    val isRetainAfterEnd: Boolean
    val cameras: List<CameraDevice>?
    val currentVisitorCamera: VisitorCamera

    fun initialize()
    fun reset()

    //used for ending engagement after visitor explicitly ends engagement
    fun endEngagement()

    //used for ending engagement after integrator ends the engagement for example [GliaWidgets.endEngagement]
    fun terminateEngagement()
    fun queueForEngagement(mediaType: MediaType, replaceExisting: Boolean)
    fun cancelQueuing()
    fun acceptCurrentEngagementRequest(visitorContextAssetId: String)
    fun declineCurrentEngagementRequest()
    fun acceptMediaUpgradeRequest(offer: MediaUpgradeOffer)
    fun declineMediaUpgradeRequest(offer: MediaUpgradeOffer)
    fun muteVisitorAudio()
    fun unMuteVisitorAudio()
    fun pauseVisitorVideo()
    fun resumeVisitorVideo()
    fun setVisitorCamera(camera: CameraDevice)
    fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?)
    fun updateIsSecureMessagingRequested(isSecureMessagingRequested: Boolean)
}
