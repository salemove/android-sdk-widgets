package com.glia.widgets.core.callvisualizer;

import android.content.Context;

import com.glia.widgets.core.callvisualizer.domain.VisitorCodeViewBuilderUseCase;
import com.glia.widgets.core.callvisualizer.domain.CallVisualizer;
import com.glia.widgets.di.Dependencies;
import com.glia.widgets.view.VisitorCodeView;

public class CallVisualizerManager implements CallVisualizer {

    private final VisitorCodeViewBuilderUseCase buildVisitorCodeUseCase;

    public CallVisualizerManager(VisitorCodeViewBuilderUseCase buildVisitorCodeUseCase) {
        this.buildVisitorCodeUseCase = buildVisitorCodeUseCase;
    }

    @Override
    public VisitorCodeView createVisitorCodeView(Context context) {
        return buildVisitorCodeUseCase.invoke(context, false);
    }

    @Override
    public void showVisitorCodeDialog(Context context) {
        Dependencies.getControllerFactory().getDialogController().showVisitorCodeDialog();
    }
}
