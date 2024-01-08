package com.glia.widgets.engagement.domain

import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.widgets.engagement.EngagementRepository
import io.reactivex.Flowable

internal interface MediaUpgradeOfferUseCase {
    operator fun invoke(): Flowable<MediaUpgradeOffer>
}

internal class MediaUpgradeOfferUseCaseImpl(private val engagementRepository: EngagementRepository) : MediaUpgradeOfferUseCase {
    override fun invoke(): Flowable<MediaUpgradeOffer> = engagementRepository.mediaUpgradeOffer
}
