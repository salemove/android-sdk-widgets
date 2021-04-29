package com.glia.widgets.di;

import com.glia.widgets.model.GliaCallRepository;
import com.glia.widgets.model.GliaEngagementRepository;
import com.glia.widgets.model.GliaMediaStateRepository;
import com.glia.widgets.model.GliaMessageRepository;
import com.glia.widgets.model.GliaMessagesNotSeenRepository;
import com.glia.widgets.model.GliaScreenSharingRepository;
import com.glia.widgets.model.GliaTicketRepository;
import com.glia.widgets.model.MediaUpgradeOfferRepository;

public class RepositoryFactory {

    private MediaUpgradeOfferRepository mediaUpgradeOfferRepository;

    public MediaUpgradeOfferRepository getMediaUpgradeOfferRepository() {
        if (mediaUpgradeOfferRepository == null) {
            mediaUpgradeOfferRepository = new MediaUpgradeOfferRepository();
        }
        return mediaUpgradeOfferRepository;
    }

    public GliaCallRepository getGliaCallRepository() {
        return new GliaCallRepository();
    }

    public GliaScreenSharingRepository getGliaScreenSharingRepository() {
        return new GliaScreenSharingRepository();
    }

    public GliaMessagesNotSeenRepository getGliaMessagesNotSeenRepository() {
        return new GliaMessagesNotSeenRepository();
    }

    public GliaMessageRepository getGliaMessageRepository() {
        return new GliaMessageRepository();
    }

    public GliaTicketRepository getGliaTicketRepository() {
        return new GliaTicketRepository();
    }

    public GliaEngagementRepository getGliaEngagementRepository() {
        return new GliaEngagementRepository();
    }

    public GliaMediaStateRepository getGliaMediaStateRepository() {
        return new GliaMediaStateRepository();
    }
}
