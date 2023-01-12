package com.glia.widgets.core.callvisualizer;

import android.content.Context;

import com.glia.widgets.core.callvisualizer.domain.VisitorCodeViewBuilderUseCase;
import com.glia.widgets.core.callvisualizer.domain.CallVisualizer;
import com.glia.widgets.view.VisitorCodeView;

public class CallVisualizerManager implements CallVisualizer {

    private final VisitorCodeViewBuilderUseCase buildVisitorCodeUseCase;

    public CallVisualizerManager(VisitorCodeViewBuilderUseCase buildVisitorCodeUseCase) {
        this.buildVisitorCodeUseCase = buildVisitorCodeUseCase;
    }

    @Override
    public VisitorCodeView buildVisitorCodeView(Context context) {
        return buildVisitorCodeUseCase.execute(context);
    }

    public void init() {
        // Nothing to do right now. Placeholder for the next tickets
    }
}
