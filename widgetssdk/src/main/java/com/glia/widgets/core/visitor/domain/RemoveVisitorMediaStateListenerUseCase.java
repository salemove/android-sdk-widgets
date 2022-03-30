package com.glia.widgets.core.visitor.domain;

import com.glia.widgets.core.visitor.GliaVisitorMediaRepository;
import com.glia.widgets.core.visitor.VisitorMediaStateListener;

public class RemoveVisitorMediaStateListenerUseCase {
    private final GliaVisitorMediaRepository repository;

    public RemoveVisitorMediaStateListenerUseCase(GliaVisitorMediaRepository repository) {
        this.repository = repository;
    }

    public void execute(VisitorMediaStateListener listener) {
        repository.removeVisitorMediaStateListener(listener);
    }
}
