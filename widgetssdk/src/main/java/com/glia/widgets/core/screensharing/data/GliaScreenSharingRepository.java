package com.glia.widgets.core.screensharing.data;

import static com.glia.androidsdk.screensharing.ScreenSharing.Status.NOT_SHARING;
import static com.glia.androidsdk.screensharing.ScreenSharing.Status.SHARING;

import android.app.Activity;

import com.glia.androidsdk.Engagement;
import com.glia.androidsdk.Glia;
import com.glia.androidsdk.GliaException;
import com.glia.androidsdk.omnibrowse.Omnibrowse;
import com.glia.androidsdk.omnibrowse.OmnibrowseEngagement;
import com.glia.androidsdk.omnicore.OmnicoreEngagement;
import com.glia.androidsdk.screensharing.LocalScreen;
import com.glia.androidsdk.screensharing.ScreenSharing;
import com.glia.androidsdk.screensharing.ScreenSharingRequest;
import com.glia.androidsdk.screensharing.VisitorScreenSharingState;
import com.glia.widgets.core.screensharing.ScreenSharingController;
import com.glia.widgets.di.GliaCore;
import com.glia.widgets.helper.Logger;

import java.util.function.Consumer;

public class GliaScreenSharingRepository {

    public static final int SKIP_ASKING_SCREEN_SHARING_PERMISSION_RESULT_CODE = 0x1995;
    private static final String TAG = GliaScreenSharingRepository.class.getSimpleName();
    private static final int UNIQUE_RESULT_CODE = 0x1994;
    private final GliaCore gliaCore;
    private ScreenSharingController screenSharingController = null;
    private final Consumer<GliaException> exceptionConsumer = this::onScreenSharingRequestHandled;
    private LocalScreen currentScreen = null;
    private ScreenSharingRequest screenSharingRequest = null;
    private final Consumer<ScreenSharingRequest> screenSharingRequestConsumer = this::onScreenSharingRequest;
    private ScreenSharing.Status screenSharingStatus = NOT_SHARING;
    private final Consumer<VisitorScreenSharingState> visitorScreenSharingStateConsumer = this::onVisitorScreenSharingStateChanged;
    // Consumers
    private final Consumer<OmnicoreEngagement> omnicoreEngagementConsumer = this::onEngagement;
    private final Consumer<OmnibrowseEngagement> omnibrowseEngagementConsumer = this::onEngagement;

    public GliaScreenSharingRepository(GliaCore gliaCore) {
        this.gliaCore = gliaCore;
    }

    public void init(ScreenSharingController screenSharingController) {
        Logger.d(TAG, "init screen sharing repository");

        this.screenSharingController = screenSharingController;
        //TODO move this to engagement repository
        gliaCore.on(Glia.Events.ENGAGEMENT, omnicoreEngagementConsumer);
        gliaCore.getCallVisualizer().on(Omnibrowse.Events.ENGAGEMENT, omnibrowseEngagementConsumer);
    }

    public void onScreenSharingAccepted(
        Activity activity,
        ScreenSharing.Mode screenSharingMode
    ) {
        Logger.i(TAG, "Screen sharing accepted by visitor");
        screenSharingRequest.accept(
            screenSharingMode,
            activity,
            UNIQUE_RESULT_CODE,
            exceptionConsumer
        );
    }

    public void onScreenSharingAcceptedAndPermissionAsked(
        Activity activity,
        ScreenSharing.Mode screenSharingMode
    ) {
        Logger.i(TAG, "Screen sharing accepted by visitor, permission asked");

        screenSharingRequest.accept(
            screenSharingMode,
            activity,
            SKIP_ASKING_SCREEN_SHARING_PERMISSION_RESULT_CODE,
            exceptionConsumer
        );
    }

    public void onScreenSharingDeclined() {
        Logger.i(TAG, "Screen sharing declined by visitor");
        gliaCore.getCurrentEngagement().ifPresent(engagement -> {
            // Pass RESULT_CANCELED to Core SDK to stop waiting for permission result. Otherwise, subsequent screen sharing requests won't be shown to the visitor.
            // Also see related bug ticket: MOB-2102
            engagement.onActivityResult(SKIP_ASKING_SCREEN_SHARING_PERMISSION_RESULT_CODE, Activity.RESULT_CANCELED, null);
        });
        screenSharingRequest.decline();
    }

    public void onEndScreenSharing() {
        if (currentScreen != null && screenSharingStatus.equals(SHARING)) {
            currentScreen.stopSharing();
        }
    }

    public void forceEndScreenSharing() {
        onEndScreenSharing();
    }

    public boolean isSharingScreen() {
        return currentScreen != null && screenSharingStatus.equals(SHARING);
    }

    public void onDestroy() {
        gliaCore.getCurrentEngagement().ifPresent(engagement -> {//TODO move this to engagement repository
            engagement.getScreenSharing().off(
                ScreenSharing.Events.SCREEN_SHARING_REQUEST,
                screenSharingRequestConsumer
            );
            engagement.getScreenSharing().off(
                ScreenSharing.Events.VISITOR_STATE,
                visitorScreenSharingStateConsumer
            );
        });
    }

    private void onEngagement(Engagement engagement) {
        Logger.d(TAG, "init screen sharing");
//TODO move this to engagement repository
        engagement.getScreenSharing().on(ScreenSharing.Events.SCREEN_SHARING_REQUEST, screenSharingRequestConsumer);
        engagement.getScreenSharing().on(ScreenSharing.Events.VISITOR_STATE, visitorScreenSharingStateConsumer);
    }

    private void onScreenSharingRequest(ScreenSharingRequest currentScreenSharingRequest) {
        Logger.d(TAG, "screen sharing requested event");

        screenSharingRequest = currentScreenSharingRequest;
        screenSharingController.onScreenSharingRequest();
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
        Logger.i(TAG, "Screen sharing started");

        currentScreen = screen;
        screenSharingController.onScreenSharingStarted();
    }

    private void onScreenSharingEnded() {
        Logger.i(TAG, "Screen sharing ended");

        currentScreen = null;
        screenSharingController.onScreenSharingEnded();
    }

    private void onScreenSharingRequestHandled(GliaException error) {
        Logger.d(TAG, "screen sharing request handled error" + (error != null));

        if (error != null) {
            Logger.e(TAG, error.debugMessage);

            screenSharingController.onScreenSharingRequestError(error);
        } else {
            screenSharingController.onScreenSharingRequestSuccess();
        }
    }
}
