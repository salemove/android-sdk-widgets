package com.glia.widgets.core.screensharing.data;

import static com.glia.androidsdk.screensharing.ScreenSharing.Status.NOT_SHARING;
import static com.glia.androidsdk.screensharing.ScreenSharing.Status.SHARING;

import android.app.Activity;

import com.glia.androidsdk.Engagement;
import com.glia.androidsdk.Glia;
import com.glia.androidsdk.GliaException;
import com.glia.androidsdk.omnicore.OmnicoreEngagement;
import com.glia.androidsdk.screensharing.LocalScreen;
import com.glia.androidsdk.screensharing.ScreenSharing;
import com.glia.androidsdk.screensharing.ScreenSharingRequest;
import com.glia.androidsdk.screensharing.VisitorScreenSharingState;
import com.glia.widgets.core.screensharing.GliaScreenSharingCallback;
import com.glia.widgets.di.Dependencies;
import com.glia.widgets.helper.Logger;

import java.util.function.Consumer;

public class GliaScreenSharingRepository {
    private GliaScreenSharingCallback callback = null;

    // Consumers
    private final Consumer<OmnicoreEngagement> engagementConsumer = this::onEngagement;
    private final Consumer<ScreenSharingRequest> screenSharingRequestConsumer = this::onScreenSharingRequest;
    private final Consumer<VisitorScreenSharingState> visitorScreenSharingStateConsumer = this::onVisitorScreenSharingStateChanged;
    private final Consumer<GliaException> exceptionConsumer = this::onScreenSharingRequestHandled;

    private LocalScreen currentScreen = null;
    private ScreenSharingRequest screenSharingRequest = null;
    private ScreenSharing.Status screenSharingStatus = NOT_SHARING;

    public void init(GliaScreenSharingCallback gliaScreenSharingCallback) {
        Logger.d(TAG, "init screen sharing repository");

        this.callback = gliaScreenSharingCallback;
        Dependencies.glia().on(Glia.Events.ENGAGEMENT, engagementConsumer);
    }

    public void onScreenSharingAccepted(
            Activity activity,
            ScreenSharing.Mode screenSharingMode
    ) {
        Logger.d(TAG, "screen sharing accepted by the user");

        screenSharingRequest.accept(
                screenSharingMode,
                activity,
                UNIQUE_RESULT_CODE,
                exceptionConsumer
        );
    }

    public void onScreenSharingDeclined() {
        Logger.d(TAG, "screen sharing declined by the user");

        screenSharingRequest.decline();
    }

    public void onEndScreenSharing() {
        if (currentScreen != null && screenSharingStatus.equals(SHARING)) {
            currentScreen.stopSharing();
        }
    }

    public void onDestroy() {
        Dependencies.glia().getCurrentEngagement().ifPresent(engagement -> {
            engagement.getScreenSharing().off(
                    ScreenSharing.Events.SCREEN_SHARING_REQUEST,
                    screenSharingRequestConsumer
            );
            engagement.getScreenSharing().off(
                    ScreenSharing.Events.VISITOR_STATE,
                    visitorScreenSharingStateConsumer
            );
        });

        Dependencies.glia().off(Glia.Events.ENGAGEMENT, engagementConsumer);
    }

    private void onEngagement(Engagement engagement) {
        Logger.d(TAG, "init screen sharing");

        engagement.getScreenSharing().on(ScreenSharing.Events.SCREEN_SHARING_REQUEST, screenSharingRequestConsumer);
        engagement.getScreenSharing().on(ScreenSharing.Events.VISITOR_STATE, visitorScreenSharingStateConsumer);
    }

    private void onScreenSharingRequest(ScreenSharingRequest currentScreenSharingRequest) {
        Logger.d(TAG, "screen sharing requested event");

        screenSharingRequest = currentScreenSharingRequest;
        callback.onScreenSharingRequest();
    }

    private void onVisitorScreenSharingStateChanged(VisitorScreenSharingState state) {
        Logger.d(TAG, "onVisitorScreenSharingStateChanged " + state.getStatus());

        if (state.getStatus() == SHARING &&
                screenSharingStatus != SHARING) {
            onScreenSharingStarted(state.getLocalScreen());
        }

        if (state.getStatus() == NOT_SHARING &&
                screenSharingStatus != NOT_SHARING) {
            onScreenSharingEnded();
        }

        screenSharingStatus = state.getStatus();
    }

    private void onScreenSharingStarted(LocalScreen screen) {
        Logger.d(TAG, "screen sharing IS SHARING");

        currentScreen = screen;
        callback.onScreenSharingStarted();
    }

    private void onScreenSharingEnded() {
        Logger.d(TAG, "screen sharing NOT SHARING");

        currentScreen = null;
        callback.onScreenSharingEnded();
    }

    private void onScreenSharingRequestHandled(GliaException error) {
        Logger.d(TAG, "screen sharing request handled error" + (error != null));

        if (error != null) {
            Logger.e(TAG, error.debugMessage);

            callback.onScreenSharingRequestError(error);
        } else {
            callback.onScreenSharingRequestSuccess();
        }
    }

    private static final String TAG = GliaScreenSharingRepository.class.getSimpleName();
    private static final int UNIQUE_RESULT_CODE = 0x1994;
}
