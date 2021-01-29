package com.glia.widgets.di;

import com.glia.widgets.model.GliaCallRepository;
import com.glia.widgets.model.GliaChatRepository;

public class RepositoryFactory {

    public GliaChatRepository getGliaChatRepository() {
        return new GliaChatRepository();
    }

    public GliaCallRepository getGliaCallRepository() {
        return new GliaCallRepository();
    }
}
