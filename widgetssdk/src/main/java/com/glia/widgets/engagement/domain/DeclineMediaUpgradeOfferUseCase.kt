package com.glia.widgets.engagement.domain

import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.widgets.engagement.EngagementRepository

internal interface DeclineMediaUpgradeOfferUseCase {
    operator fun invoke(offer: MediaUpgradeOffer)
}

internal class DeclineMediaUpgradeOfferUseCaseImpl(private val engagementRepository: EngagementRepository) : DeclineMediaUpgradeOfferUseCase {
    override fun invoke(offer: MediaUpgradeOffer) = engagementRepository.declineMediaUpgradeRequest(offer)
}
