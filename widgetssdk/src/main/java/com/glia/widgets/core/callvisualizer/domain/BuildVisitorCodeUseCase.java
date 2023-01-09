package com.glia.widgets.core.callvisualizer.domain;

import android.content.Context;

import com.glia.androidsdk.omnibrowse.VisitorCode;
import com.glia.widgets.view.VisitorCodeView;

public class BuildVisitorCodeUseCase {

    private final VisitorCodeRepository visitorCodeRepository;
    private final VisitorCodeViewRepository visitorCodeViewRepository;

    public BuildVisitorCodeUseCase(VisitorCodeRepository visitorCodeRepository, VisitorCodeViewRepository visitorCodeViewRepository) {
        this.visitorCodeRepository = visitorCodeRepository;
        this.visitorCodeViewRepository = visitorCodeViewRepository;
    }

    public VisitorCodeView execute(Context context) {
        VisitorCodeView visitorCodeView = visitorCodeViewRepository.createVisitorCodeView(context);
        visitorCodeView.setVisitorCodeRepository(visitorCodeRepository);
        return visitorCodeView;
    }
}
