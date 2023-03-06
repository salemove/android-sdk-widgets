package com.glia.widgets.core.callvisualizer;

import android.content.Context;

import com.glia.widgets.callvisualizer.CallVisualizerRepository;
import com.glia.widgets.core.callvisualizer.domain.VisitorCodeViewBuilderUseCase;
import com.glia.widgets.core.callvisualizer.domain.CallVisualizer;
import com.glia.widgets.di.Dependencies;
import com.glia.widgets.view.VisitorCodeView;

public class CallVisualizerManager implements CallVisualizer {

    private CallVisualizerRepository repository;
    private final VisitorCodeViewBuilderUseCase buildVisitorCodeUseCase;

    public CallVisualizerManager(
            VisitorCodeViewBuilderUseCase buildVisitorCodeUseCase,
            CallVisualizerRepository repository
    ) {
        this.buildVisitorCodeUseCase = buildVisitorCodeUseCase;
        this.repository = repository;
    }

    @Override
    public VisitorCodeView createVisitorCodeView(Context context) {
        return buildVisitorCodeUseCase.invoke(context, false);
    }

    @Override
    public void showVisitorCodeDialog(Context context) {
        Dependencies.getControllerFactory().getDialogController().showVisitorCodeDialog();
    }

    @Override
    public void addVisitorContext(String visitorContext) {
        repository.addVisitorContext(visitorContext);
    }
}
