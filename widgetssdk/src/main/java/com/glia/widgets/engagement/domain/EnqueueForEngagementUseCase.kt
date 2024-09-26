package com.glia.widgets.engagement.domain

import com.glia.androidsdk.Engagement
import com.glia.widgets.engagement.EngagementRepository
import com.glia.widgets.launcher.ConfigurationManager

internal interface EnqueueForEngagementUseCase {
    operator fun invoke(mediaType: Engagement.MediaType = Engagement.MediaType.TEXT)
}

internal class EnqueueForEngagementUseCaseImpl(
    private val engagementRepository: EngagementRepository,
    private val configurationManager: ConfigurationManager
) : EnqueueForEngagementUseCase {
    override fun invoke(mediaType: Engagement.MediaType) = engagementRepository.queueForEngagement(configurationManager.queueIds.orEmpty(), mediaType)
}
