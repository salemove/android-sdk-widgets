package com.glia.widgets.engagement.domain

import com.glia.androidsdk.Engagement.MediaType
import com.glia.widgets.helper.hasAudio
import com.glia.widgets.helper.hasVideo
import io.reactivex.rxjava3.core.Flowable

internal interface EngagementTypeUseCase {
    val isAudioEngagement: Boolean
    val isVideoEngagement: Boolean
    val isMediaEngagement: Boolean
    val isChatEngagement: Boolean
    val isCallVisualizer: Boolean
    val isCallVisualizerScreenSharing: Boolean
    val hasVideo: Boolean

    operator fun invoke(): Flowable<MediaType>
}

internal class EngagementTypeUseCaseImpl(
    private val isQueueingOrLiveEngagementUseCase: IsQueueingOrLiveEngagementUseCase,
    private val isCurrentEngagementCallVisualizerUseCase: IsCurrentEngagementCallVisualizerUseCase,
    private val screenSharingUseCase: ScreenSharingUseCase,
    private val operatorMediaUseCase: OperatorMediaUseCase,
    private val visitorMediaUseCase: VisitorMediaUseCase,
    private val isOperatorPresentUseCase: IsOperatorPresentUseCase,
) : EngagementTypeUseCase {
    private val hasOngoingEngagement get() = isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement
    private val hasAudio: Boolean get() = visitorMediaUseCase.hasAudio || operatorMediaUseCase.hasAudio
    override val hasVideo: Boolean get() = visitorMediaUseCase.hasVideo || operatorMediaUseCase.hasVideo
    override val isAudioEngagement: Boolean get() = hasOngoingEngagement && isOperatorPresentUseCase() && hasAudio
    override val isVideoEngagement: Boolean get() = hasOngoingEngagement && isOperatorPresentUseCase() && hasVideo
    private val hasAnyMedia: Boolean get() = visitorMediaUseCase.hasMedia || operatorMediaUseCase.hasMedia
    override val isMediaEngagement: Boolean get() = hasOngoingEngagement && isOperatorPresentUseCase() && hasAnyMedia
    override val isChatEngagement: Boolean get() = hasOngoingEngagement && !isCurrentEngagementCallVisualizerUseCase() && isOperatorPresentUseCase() && !hasAnyMedia
    override val isCallVisualizer: Boolean get() = isCurrentEngagementCallVisualizerUseCase()
    override val isCallVisualizerScreenSharing: Boolean get() = isCurrentEngagementCallVisualizerUseCase() && screenSharingUseCase.isSharing
    private val operatorMediaObservable by lazy { operatorMediaUseCase() }

    override fun invoke(): Flowable<MediaType> {
        return operatorMediaObservable.map { operatorMediaState ->
            when {
                operatorMediaState.hasVideo -> MediaType.VIDEO
                operatorMediaState.hasAudio -> MediaType.AUDIO
                else -> MediaType.UNKNOWN
            }
        }
    }
}
