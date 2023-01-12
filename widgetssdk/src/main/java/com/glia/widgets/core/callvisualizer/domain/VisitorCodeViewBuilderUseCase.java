package com.glia.widgets.core.callvisualizer.domain;

import android.content.Context;

import com.glia.widgets.view.VisitorCodeView;

public class VisitorCodeViewBuilderUseCase {

    private final VisitorCodeRepository visitorCodeRepository;
    private final VisitorCodeViewRepository visitorCodeViewRepository;

    public VisitorCodeViewBuilderUseCase(VisitorCodeRepository visitorCodeRepository, VisitorCodeViewRepository visitorCodeViewRepository) {
        this.visitorCodeRepository = visitorCodeRepository;
        this.visitorCodeViewRepository = visitorCodeViewRepository;
    }

    public VisitorCodeView execute(Context context) {
        VisitorCodeView visitorCodeView = visitorCodeViewRepository.createVisitorCodeView(context);
        visitorCodeView.setVisitorCodeRepository(visitorCodeRepository);
        return visitorCodeView;
    }
}
