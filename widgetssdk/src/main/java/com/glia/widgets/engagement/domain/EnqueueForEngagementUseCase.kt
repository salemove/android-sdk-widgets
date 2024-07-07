package com.glia.widgets.engagement.domain

import com.glia.androidsdk.Engagement
import com.glia.widgets.engagement.EngagementRepository

internal interface EnqueueForEngagementUseCase {
    operator fun invoke(queueIds: List<String>?, mediaType: Engagement.MediaType? = null, visitorContextAssetId: String? = null)
}

internal class EnqueueForEngagementUseCaseImpl(private val engagementRepository: EngagementRepository) : EnqueueForEngagementUseCase {
    override fun invoke(queueIds: List<String>?, mediaType: Engagement.MediaType?, visitorContextAssetId: String?) {
        val type = mediaType ?: Engagement.MediaType.TEXT
        engagementRepository.queueForEngagement(queueIds ?: emptyList(), type, visitorContextAssetId)
    }
}
