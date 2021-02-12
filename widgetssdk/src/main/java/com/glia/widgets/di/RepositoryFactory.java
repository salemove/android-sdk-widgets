package com.glia.widgets.di;

import com.glia.widgets.model.GliaCallRepository;
import com.glia.widgets.model.GliaChatRepository;
import com.glia.widgets.model.MediaUpgradeOfferRepository;

public class RepositoryFactory {

    private MediaUpgradeOfferRepository mediaUpgradeOfferRepository;

    public MediaUpgradeOfferRepository getMediaUpgradeOfferRepository() {
        if(mediaUpgradeOfferRepository==null){
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
}
