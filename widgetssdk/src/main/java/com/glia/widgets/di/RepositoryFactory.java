package com.glia.widgets.di;

import com.glia.widgets.core.CoreGliaRepository;
import com.glia.widgets.model.GliaCallRepository;
import com.glia.widgets.model.GliaChatHeadControllerRepository;
import com.glia.widgets.model.GliaChatRepository;
import com.glia.widgets.model.GliaMessagesNotSeenRepository;
import com.glia.widgets.model.GliaScreenSharingRepository;
import com.glia.widgets.model.MediaUpgradeOfferRepository;

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

    public GliaChatHeadControllerRepository getGliaChatHeadControllerRepository() {
        return new GliaChatHeadControllerRepository();
    }

    public GliaMessagesNotSeenRepository getGliaMessagesNotSeenRepository() {
        return new GliaMessagesNotSeenRepository();
    }

    public CoreGliaRepository getCoreGliaRepository() {
        return new CoreGliaRepository();
    }
}
