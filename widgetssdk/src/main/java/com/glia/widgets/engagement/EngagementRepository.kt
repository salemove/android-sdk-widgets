package com.glia.widgets.engagement

import android.app.Activity
import android.content.Intent
import com.glia.androidsdk.Engagement.MediaType
import com.glia.androidsdk.IncomingEngagementRequest
import com.glia.androidsdk.Operator
import com.glia.androidsdk.comms.MediaState
import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.androidsdk.screensharing.ScreenSharing.Mode
import com.glia.widgets.helper.Data
import io.reactivex.rxjava3.core.Flowable

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
    val screenSharingState: Flowable<ScreenSharingState>

    val currentOperatorValue: Operator?
    val isQueueingOrEngagement: Boolean
    val hasOngoingEngagement: Boolean
    val isQueueing: Boolean
    val isQueueingForMedia: Boolean
    val isCallVisualizerEngagement: Boolean
    val isOperatorPresent: Boolean
    val isSharingScreen: Boolean

    fun initialize()
    fun reset()
    fun resetQueueing()
    fun endEngagement(silently: Boolean)
    fun queueForChatEngagement(queueId: String, visitorContextAssetId: String?)
    fun queueForMediaEngagement(queueId: String, mediaType: MediaType, visitorContextAssetId: String?)
    fun cancelQueuing()
    fun acceptCurrentEngagementRequest(visitorContextAssetId: String)
    fun declineCurrentEngagementRequest()
    fun acceptMediaUpgradeRequest(offer: MediaUpgradeOffer)
    fun declineMediaUpgradeRequest(offer: MediaUpgradeOffer)
    fun muteVisitorAudio()
    fun unMuteVisitorAudio()
    fun pauseVisitorVideo()
    fun resumeVisitorVideo()
    fun endScreenSharing()
    fun declineScreenSharingRequest()
    fun acceptScreenSharingRequest(activity: Activity, mode: Mode)
    fun acceptScreenSharingWithAskedPermission(activity: Activity, mode: Mode)
    fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?)
    fun onActivityResultSkipScreenSharingPermissionRequest(resultCode: Int, intent: Intent?)
}
