package com.glia.widgets.engagement.domain

internal interface EngagementTypeUseCase {
    val isMediaEngagement: Boolean
    val isChatEngagement: Boolean
    val isCallVisualizerScreenSharing: Boolean
}

internal class EngagementTypeUseCaseImpl(
    private val isQueueingOrLiveEngagementUseCase: IsQueueingOrLiveEngagementUseCase,
    private val isCurrentEngagementCallVisualizerUseCase: IsCurrentEngagementCallVisualizerUseCase,
    private val operatorMediaUseCase: OperatorMediaUseCase,
    private val visitorMediaUseCase: VisitorMediaUseCase,
    private val isOperatorPresentUseCase: IsOperatorPresentUseCase
) : EngagementTypeUseCase {
    private val hasOngoingEngagement get() = isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement
    private val hasAnyMedia: Boolean get() = visitorMediaUseCase.hasMedia || operatorMediaUseCase.hasMedia
    override val isMediaEngagement: Boolean get() = hasOngoingEngagement && isOperatorPresentUseCase() && hasAnyMedia
    override val isChatEngagement: Boolean get() = hasOngoingEngagement && !isCurrentEngagementCallVisualizerUseCase() && isOperatorPresentUseCase() && !hasAnyMedia
    override val isCallVisualizerScreenSharing: Boolean get() = isCurrentEngagementCallVisualizerUseCase() && !hasAnyMedia
}
