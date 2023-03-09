package com.glia.widgets.core.callvisualizer;

import android.content.Context;

import com.glia.androidsdk.GliaException;
import com.glia.widgets.GliaWidgets;
import androidx.annotation.NonNull;

import com.glia.widgets.callvisualizer.CallVisualizerRepository;
import com.glia.widgets.core.callvisualizer.domain.VisitorCodeViewBuilderUseCase;
import com.glia.widgets.core.callvisualizer.domain.CallVisualizer;
import com.glia.widgets.core.engagement.GliaEngagementRepository;
import com.glia.widgets.di.Dependencies;
import com.glia.widgets.view.VisitorCodeView;

public class CallVisualizerManager implements CallVisualizer {

    private final CallVisualizerRepository callVisualizerRepository;
    private final VisitorCodeViewBuilderUseCase buildVisitorCodeUseCase;
    private final GliaEngagementRepository engagementRepository;
    private Runnable onEngagementStartRunnable;
    private boolean isListeningForEngagement = false;
    public static final String TAG = CallVisualizer.class.getSimpleName();

    public CallVisualizerManager(
            VisitorCodeViewBuilderUseCase buildVisitorCodeUseCase,
            CallVisualizerRepository callVisualizerRepository,
            GliaEngagementRepository engagementRepository
    ) {
        this.buildVisitorCodeUseCase = buildVisitorCodeUseCase;
        this.callVisualizerRepository = callVisualizerRepository;
        this.engagementRepository = engagementRepository;
    }

    private void checkForProperInit() {
        if (Dependencies.getSdkConfigurationManager().getCompanyName() == null || Dependencies.getSdkConfigurationManager().getCompanyName().isEmpty()) {
            throw new GliaException("companyName not set during GliaWidgets.init(GliaWidgetsConfig gliaWidgetsConfig)", GliaException.Cause.INVALID_INPUT);
        }
    }

    @Override
    public VisitorCodeView createVisitorCodeView(Context context) {
        checkForProperInit();
        startListeningForEngagements();
        return buildVisitorCodeUseCase.invoke(context, false);
    }

    @Override
    public void showVisitorCodeDialog(Context context) {
        checkForProperInit();
        startListeningForEngagements();
        Dependencies.getControllerFactory().getDialogController().showVisitorCodeDialog();
    }

    @Override
    public void addVisitorContext(String visitorContext) {
        callVisualizerRepository.addVisitorContext(visitorContext);
    }

    @Override
    public void onEngagementStart(@NonNull Runnable runnable) {
        onEngagementStartRunnable = runnable;
    }

    private void startListeningForEngagements() {
        // Can't start Core SDK listener on this class init/construction because this class is initialized
        // on `Application.onCreate()` before the Glia Core SDK is initialised.
        if (isListeningForEngagement) return;
        isListeningForEngagement = true;
        // Subscribe only once because otherwise need to add method to unsubscribe
        engagementRepository.listenForCallVisualizerEngagement((engagement) -> {
            if (onEngagementStartRunnable != null) {
                onEngagementStartRunnable.run();
            }
        });
    }
}
