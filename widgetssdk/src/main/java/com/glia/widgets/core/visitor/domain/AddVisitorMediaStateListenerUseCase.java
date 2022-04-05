package com.glia.widgets.core.visitor.domain;

import com.glia.widgets.core.visitor.GliaVisitorMediaRepository;
import com.glia.widgets.core.visitor.VisitorMediaUpdatesListener;

public class AddVisitorMediaStateListenerUseCase {
    private final GliaVisitorMediaRepository repository;

    public AddVisitorMediaStateListenerUseCase(GliaVisitorMediaRepository repository) {
        this.repository = repository;
    }

    public void execute(VisitorMediaUpdatesListener listener) {
        repository.addVisitorMediaStateListener(listener);
    }
}
