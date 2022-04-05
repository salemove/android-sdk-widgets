package com.glia.widgets.core.visitor.domain;

import com.glia.widgets.core.visitor.GliaVisitorMediaRepository;
import com.glia.widgets.core.visitor.VisitorMediaUpdatesListener;

public class RemoveVisitorMediaStateListenerUseCase {
    private final GliaVisitorMediaRepository repository;

    public RemoveVisitorMediaStateListenerUseCase(GliaVisitorMediaRepository repository) {
        this.repository = repository;
    }

    public void execute(VisitorMediaUpdatesListener listener) {
        repository.removeVisitorMediaStateListener(listener);
    }
}
