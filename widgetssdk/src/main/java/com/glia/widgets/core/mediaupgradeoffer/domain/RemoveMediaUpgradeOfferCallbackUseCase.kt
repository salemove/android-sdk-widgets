package com.glia.widgets.core.mediaupgradeoffer.domain

import com.glia.widgets.core.mediaupgradeoffer.MediaUpgradeOfferRepository
import com.glia.widgets.core.mediaupgradeoffer.MediaUpgradeOfferRepositoryCallback

class RemoveMediaUpgradeOfferCallbackUseCase(
    private val mediaUpgradeOfferRepository: MediaUpgradeOfferRepository
) {
    operator fun invoke(callback: MediaUpgradeOfferRepositoryCallback) {
        mediaUpgradeOfferRepository.removeCallback(callback)
    }
}
