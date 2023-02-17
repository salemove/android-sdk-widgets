package com.glia.widgets.core.screensharing.data;

import static com.glia.androidsdk.screensharing.ScreenSharing.Status.NOT_SHARING;
import static com.glia.androidsdk.screensharing.ScreenSharing.Status.SHARING;
import static com.glia.widgets.core.screensharing.MediaProjectionService.Actions.START;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;

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
import com.glia.widgets.core.screensharing.GliaScreenSharingCallback;
import com.glia.widgets.core.screensharing.MediaProjectionService;
import com.glia.widgets.di.GliaCore;
import com.glia.widgets.helper.Logger;

import java.util.function.Consumer;

public class GliaScreenSharingRepository {

    private final GliaCore gliaCore;
    private GliaScreenSharingCallback callback = null;

    // Consumers
    private final Consumer<OmnicoreEngagement> omnicoreEngagementConsumer = this::onEngagement;
    private final Consumer<OmnibrowseEngagement> omnibrowseEngagementConsumer = this::onEngagement;
    private final Consumer<ScreenSharingRequest> screenSharingRequestConsumer = this::onScreenSharingRequest;
    private final Consumer<VisitorScreenSharingState> visitorScreenSharingStateConsumer = this::onVisitorScreenSharingStateChanged;
    private final Consumer<GliaException> exceptionConsumer = this::onScreenSharingRequestHandled;

    private LocalScreen currentScreen = null;
    private ScreenSharingRequest screenSharingRequest = null;
    private ScreenSharing.Status screenSharingStatus = NOT_SHARING;

    public GliaScreenSharingRepository(GliaCore gliaCore) {
        this.gliaCore = gliaCore;
    }

    public void init(GliaScreenSharingCallback gliaScreenSharingCallback) {
        Logger.d(TAG, "init screen sharing repository");

        this.callback = gliaScreenSharingCallback;
        gliaCore.on(Glia.Events.ENGAGEMENT, omnicoreEngagementConsumer);
        gliaCore.getCallVisualizer().on(Omnibrowse.Events.ENGAGEMENT, omnibrowseEngagementConsumer);
    }

    public void onScreenSharingAccepted(
            Activity activity,
            ScreenSharing.Mode screenSharingMode
    ) {
        Logger.d(TAG, "screen sharing accepted by the user");
        startMediaProjectionService(activity);
        screenSharingRequest.accept(
                screenSharingMode,
                activity,
                UNIQUE_RESULT_CODE,
                exceptionConsumer
        );
    }

    @SuppressLint("ShouldUseStaticImport")
    private void startMediaProjectionService(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Intent intent = new Intent(activity, MediaProjectionService.class);
            intent.setAction(START);
            activity.startForegroundService(intent);
        }
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

    public void forceEndScreenSharing() {
        onEndScreenSharing();
    }

    public boolean isSharingScreen() {
        return currentScreen != null && screenSharingStatus.equals(SHARING);
    }

    public void onDestroy() {
        gliaCore.getCurrentEngagement().ifPresent(engagement -> {
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
    public static final int UNIQUE_RESULT_CODE = 0x1994;
}
