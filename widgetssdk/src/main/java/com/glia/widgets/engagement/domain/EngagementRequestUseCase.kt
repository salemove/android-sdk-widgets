package com.glia.widgets.engagement.domain

import com.glia.androidsdk.IncomingEngagementRequest
import com.glia.widgets.engagement.EngagementRepository
import io.reactivex.Flowable

internal interface EngagementRequestUseCase {
    operator fun invoke(): Flowable<IncomingEngagementRequest>

    fun accept(visitorContextAssetId: String)
    fun decline()
}

internal class EngagementRequestUseCaseImpl(private val engagementRepository: EngagementRepository) : EngagementRequestUseCase {
    override fun invoke(): Flowable<IncomingEngagementRequest> = engagementRepository.engagementRequest

    override fun accept(visitorContextAssetId: String) = engagementRepository.acceptCurrentEngagementRequest(visitorContextAssetId)
    override fun decline() = engagementRepository.declineCurrentEngagementRequest()
}
