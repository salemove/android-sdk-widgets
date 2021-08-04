package com.glia.widgets.core.operator.domain;

import com.glia.widgets.core.operator.GliaOperatorMediaRepository;

public class AddOperatorMediaStateListenerUseCase {
    private final GliaOperatorMediaRepository repository;

    public AddOperatorMediaStateListenerUseCase(GliaOperatorMediaRepository repository) {
        this.repository = repository;
    }

    public void execute(GliaOperatorMediaRepository.OperatorMediaStateListener listener) {
        this.repository.addMediaStateListener(listener);
    }
}
