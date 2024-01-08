package com.glia.widgets.engagement.domain

import com.glia.androidsdk.Engagement
import com.glia.widgets.engagement.EngagementRepository

internal interface EnqueueForEngagementUseCase {
    operator fun invoke(queueId: String, mediaType: Engagement.MediaType? = null, visitorContextAssetId: String? = null)
}

internal class EnqueueForEngagementUseCaseImpl(private val engagementRepository: EngagementRepository) : EnqueueForEngagementUseCase {
    override fun invoke(queueId: String, mediaType: Engagement.MediaType?, visitorContextAssetId: String?) {
        if (mediaType == null) {
            engagementRepository.queueForChatEngagement(queueId, visitorContextAssetId)
        } else {
            engagementRepository.queueForMediaEngagement(queueId, mediaType, visitorContextAssetId)
        }
    }
}
