package com.glia.widgets.core.callvisualizer;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;

import com.glia.androidsdk.GliaException;
import com.glia.widgets.callvisualizer.controller.CallVisualizerController;
import com.glia.widgets.core.callvisualizer.domain.CallVisualizer;
import com.glia.widgets.core.callvisualizer.domain.VisitorCodeViewBuilderUseCase;
import com.glia.widgets.core.configuration.GliaSdkConfiguration;
import com.glia.widgets.di.Dependencies;
import com.glia.widgets.view.VisitorCodeView;

public class CallVisualizerManager implements CallVisualizer {

    private final VisitorCodeViewBuilderUseCase buildVisitorCodeUseCase;
    private final CallVisualizerController callVisualizerController;
    public static final String TAG = CallVisualizer.class.getSimpleName();
    public GliaSdkConfiguration configuration;

    public CallVisualizerManager(
        VisitorCodeViewBuilderUseCase buildVisitorCodeUseCase,
        CallVisualizerController callVisualizerController
    ) {
        this.buildVisitorCodeUseCase = buildVisitorCodeUseCase;
        this.callVisualizerController = callVisualizerController;
    }

    private void checkForProperInit() {
        if (Dependencies.getSdkConfigurationManager().getCompanyName() == null || Dependencies.getSdkConfigurationManager().getCompanyName().isEmpty()) {
            throw new GliaException("companyName not set during GliaWidgets.init(GliaWidgetsConfig gliaWidgetsConfig)", GliaException.Cause.INVALID_INPUT);
        }
    }

    @Override
    public VisitorCodeView createVisitorCodeView(Context context) {
        checkForProperInit();
        return buildVisitorCodeUseCase.invoke(context, false);
    }

    @Override
    public void showVisitorCodeDialog(Context context) {
        checkForProperInit();
        Dependencies.getControllerFactory().getDialogController().showVisitorCodeDialog();
    }

    @Override
    public void addVisitorContext(String visitorContext) {
        callVisualizerController.saveVisitorContextAssetId(visitorContext);
    }

    @SuppressLint("CheckResult")
    @Override
    public void onEngagementStart(@NonNull Runnable runnable) {
        callVisualizerController.getEngagementStartFlow().subscribe(ignore -> runnable.run(), ignore -> {
        });
    }

    @SuppressLint("CheckResult")
    @Override
    public void onEngagementEnd(Runnable runnable) {
        callVisualizerController.getEngagementEndFlow().subscribe(ignore -> runnable.run(), ignore -> {
        });
    }
}
