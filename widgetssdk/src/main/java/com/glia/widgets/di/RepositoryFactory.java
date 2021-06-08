package com.glia.widgets.di;

import com.glia.widgets.model.GliaChatRepository;
import com.glia.widgets.model.GliaEngagementRepository;
import com.glia.widgets.model.GliaMediaRepository;
import com.glia.widgets.model.GliaMessagesNotSeenRepository;
import com.glia.widgets.model.GliaRepository;
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

    public GliaScreenSharingRepository getGliaScreenSharingRepository() {
        return new GliaScreenSharingRepository();
    }

    public GliaMessagesNotSeenRepository getGliaMessagesNotSeenRepository() {
        return new GliaMessagesNotSeenRepository();
    }

    public GliaChatRepository getGliaMessageRepository() {
        return new GliaChatRepository();
    }

    public GliaRepository getGliaRepository() {
        return new GliaRepository();
    }

    public GliaEngagementRepository getGliaEngagementRepository() {
        return new GliaEngagementRepository();
    }

    public GliaMediaRepository getGliaMediaStateRepository() {
        return new GliaMediaRepository();
    }
}
