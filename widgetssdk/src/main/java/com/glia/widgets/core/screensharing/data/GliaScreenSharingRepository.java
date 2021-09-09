package com.glia.widgets.core.screensharing.data;

import com.glia.androidsdk.Engagement;
import com.glia.androidsdk.Glia;
import com.glia.androidsdk.GliaException;
import com.glia.androidsdk.omnicore.OmnicoreEngagement;
import com.glia.androidsdk.screensharing.LocalScreen;
import com.glia.androidsdk.screensharing.ScreenSharing;
import com.glia.androidsdk.screensharing.ScreenSharingRequest;
import com.glia.androidsdk.screensharing.VisitorScreenSharingState;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.core.screensharing.GliaScreenSharingCallback;

import java.util.function.Consumer;

public class GliaScreenSharingRepository {
    private static final int UNIQUE_RESULT_CODE = 0x1994;
    private static final String TAG = "GliaScreenSharingRepository";

    private GliaScreenSharingCallback callback = null;

    // Consumers
    private final Consumer<ScreenSharingRequest> screenSharingRequestConsumer = this::onScreenSharingRequest;
    private final Consumer<VisitorScreenSharingState> visitorScreenSharingStateConsumer = this::onVisitorScreenSharingStateChanged;
    private final Consumer<OmnicoreEngagement> engagementConsumer = this::initScreenSharing;
    private final Consumer<GliaException> exceptionConsumer = this::onScreenSharingRequestHandled;

    private LocalScreen currentScreen = null;
    private ScreenSharingRequest screenSharingRequest = null;
    private ScreenSharing.Status screenSharingStatus = ScreenSharing.Status.NOT_SHARING;

    public void init(GliaScreenSharingCallback gliaScreenSharingCallback) {
        Logger.d(TAG, "init screen sharing repository");
        this.callback = gliaScreenSharingCallback;
        Glia.on(Glia.Events.ENGAGEMENT, engagementConsumer);
    }

    private void initScreenSharing(Engagement engagement) {
        Logger.d(TAG, "init screen sharing");

        engagement.getScreenSharing().on(ScreenSharing.Events.SCREEN_SHARING_REQUEST, screenSharingRequestConsumer);
        engagement.getScreenSharing().on(ScreenSharing.Events.VISITOR_STATE, visitorScreenSharingStateConsumer);
    }

    private void onVisitorScreenSharingStateChanged(VisitorScreenSharingState state) {
        Logger.d(TAG, "onVisitorScreenSharingStateChanged " + state.getStatus());

        if (state.getStatus().equals(ScreenSharing.Status.SHARING) && screenSharingStatus != ScreenSharing.Status.SHARING) {
            onScreenSharingStarted(state.getLocalScreen());
        }

        if (state.getStatus().equals(ScreenSharing.Status.NOT_SHARING) && screenSharingStatus != ScreenSharing.Status.NOT_SHARING) {
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

    private void onScreenSharingRequest(ScreenSharingRequest currentScreenSharingRequest) {
        Logger.d(TAG, "screen sharing requested event");
        screenSharingRequest = currentScreenSharingRequest;
        callback.onScreenSharingRequest();
    }

    public void onScreenSharingAccepted(android.app.Activity activity) {
        Logger.d(TAG, "screen sharing accepted by the user");
        screenSharingRequest.accept(ScreenSharing.Mode.UNBOUNDED, activity, UNIQUE_RESULT_CODE, exceptionConsumer);
    }

    public void onScreenSharingDeclined() {
        Logger.d(TAG, "screen sharing declined by the user");
        screenSharingRequest.decline();
    }

    private void onScreenSharingRequestHandled(GliaException error) {
        Logger.d(TAG, "screen sharing request handeled error" + (error != null));
        if (error != null) {
            Logger.e(TAG, error.debugMessage);
            callback.onScreenSharingRequestError(error);
        } else {
            callback.onScreenSharingRequestSuccess();
        }
    }

    public void onDestroy() {
        Glia.getCurrentEngagement().ifPresent(engagement -> {
            engagement.getScreenSharing().off(ScreenSharing.Events.SCREEN_SHARING_REQUEST, screenSharingRequestConsumer);
            engagement.getScreenSharing().off(ScreenSharing.Events.VISITOR_STATE, visitorScreenSharingStateConsumer);
        });

        Glia.off(Glia.Events.ENGAGEMENT, engagementConsumer);
    }

    public void onEndScreenSharing() {
        if (currentScreen != null && screenSharingStatus.equals(ScreenSharing.Status.SHARING)) {
            currentScreen.stopSharing();
        }
    }
}
