package com.glia.widgets.engagement.domain

import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.widgets.engagement.EngagementRepository
import io.reactivex.rxjava3.core.Flowable

internal interface AcceptMediaUpgradeOfferUseCase {
    val result: Flowable<MediaUpgradeOffer>
    operator fun invoke(offer: MediaUpgradeOffer)
}

internal class AcceptMediaUpgradeOfferUseCaseImpl(private val engagementRepository: EngagementRepository) : AcceptMediaUpgradeOfferUseCase {
    override val result: Flowable<MediaUpgradeOffer> = engagementRepository.mediaUpgradeOfferAcceptResult
        .filter { it.isSuccess }
        .map { it.getOrThrow() }

    override fun invoke(offer: MediaUpgradeOffer) = engagementRepository.acceptMediaUpgradeRequest(offer)
}
