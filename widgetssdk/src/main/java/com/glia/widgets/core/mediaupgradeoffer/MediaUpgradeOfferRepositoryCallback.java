package com.glia.widgets.core.mediaupgradeoffer;

import com.glia.androidsdk.comms.MediaUpgradeOffer;

public interface MediaUpgradeOfferRepositoryCallback {

    void newOffer(MediaUpgradeOffer offer);

    void upgradeOfferChoiceSubmitSuccess(
            MediaUpgradeOffer offer,
            MediaUpgradeOfferRepository.Submitter submitter
    );

    void upgradeOfferChoiceDeclinedSuccess(MediaUpgradeOfferRepository.Submitter submitter);
}