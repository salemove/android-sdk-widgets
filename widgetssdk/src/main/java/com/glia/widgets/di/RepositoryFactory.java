package com.glia.widgets.di;

import com.glia.widgets.model.GliaCallRepository;
import com.glia.widgets.model.GliaChatHeadControllerRepository;
import com.glia.widgets.model.GliaChatRepository;
import com.glia.widgets.model.MediaUpgradeOfferRepository;
import com.glia.widgets.model.GliaScreenSharingRepository;

public class RepositoryFactory {

    private MediaUpgradeOfferRepository mediaUpgradeOfferRepository;

    public MediaUpgradeOfferRepository getMediaUpgradeOfferRepository() {
        if (mediaUpgradeOfferRepository == null) {
            mediaUpgradeOfferRepository = new MediaUpgradeOfferRepository();
        }
        return mediaUpgradeOfferRepository;
    }

    public GliaChatRepository getGliaChatRepository() {
        return new GliaChatRepository();
    }

    public GliaCallRepository getGliaCallRepository() {
        return new GliaCallRepository();
    }

    public GliaScreenSharingRepository getGliaScreenSharingRepository() {
        return new GliaScreenSharingRepository();
    }

    public GliaChatHeadControllerRepository getGliaChatHeadControllerRepository(){
        return new GliaChatHeadControllerRepository();
    }
}
