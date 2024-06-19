package com.glia.widgets.engagement.domain

import com.glia.androidsdk.Engagement
import com.glia.widgets.engagement.EngagementRepository

internal interface EnqueueForEngagementUseCase {
    operator fun invoke(queueId: String?, mediaType: Engagement.MediaType? = null, visitorContextAssetId: String? = null)
}

internal class EnqueueForEngagementUseCaseImpl(private val engagementRepository: EngagementRepository) : EnqueueForEngagementUseCase {
    override fun invoke(queueId: String?, mediaType: Engagement.MediaType?, visitorContextAssetId: String?) {
        val ids = queueId?.let { listOf(queueId) } ?: emptyList()
        val type = mediaType ?: Engagement.MediaType.TEXT
        engagementRepository.queueForEngagement(ids, type, visitorContextAssetId)
    }
}
