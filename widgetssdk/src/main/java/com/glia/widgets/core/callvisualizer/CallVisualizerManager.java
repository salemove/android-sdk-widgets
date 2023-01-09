package com.glia.widgets.core.callvisualizer;

import android.content.Context;

import com.glia.widgets.core.callvisualizer.domain.BuildVisitorCodeUseCase;
import com.glia.widgets.core.callvisualizer.domain.CallVisualizer;
import com.glia.widgets.view.VisitorCodeView;

public class CallVisualizerManager implements CallVisualizer {

    private BuildVisitorCodeUseCase buildVisitorCodeUseCase;

    @Override
    public VisitorCodeView buildVisitorCodeView(Context context) {
        // TODO
        throw new IllegalStateException();
    }

    public void init() {
        // Nothing to do right now. Placeholder for the next tickets
    }
}
