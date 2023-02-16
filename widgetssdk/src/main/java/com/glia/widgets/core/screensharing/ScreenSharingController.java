package com.glia.widgets.core.screensharing;

import android.content.Context;

import androidx.annotation.VisibleForTesting;

import com.glia.androidsdk.GliaException;
import com.glia.widgets.core.configuration.GliaSdkConfigurationManager;
import com.glia.widgets.core.dialog.DialogController;
import com.glia.widgets.core.notification.domain.RemoveScreenSharingNotificationUseCase;
import com.glia.widgets.core.notification.domain.ShowScreenSharingNotificationUseCase;
import com.glia.widgets.core.permissions.domain.HasScreenSharingNotificationChannelEnabledUseCase;
import com.glia.widgets.core.screensharing.data.GliaScreenSharingRepository;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.helper.Utils;

import java.util.HashSet;
import java.util.Set;

public class ScreenSharingController implements GliaScreenSharingCallback {
    private static final String TAG = "ScreenSharingController";

    private final GliaScreenSharingRepository repository;
    private final DialogController dialogController;
    private final ShowScreenSharingNotificationUseCase showScreenSharingNotificationUseCase;
    private final RemoveScreenSharingNotificationUseCase removeScreenSharingNotificationUseCase;
    private final HasScreenSharingNotificationChannelEnabledUseCase hasScreenSharingNotificationChannelEnabledUseCase;
    private final GliaSdkConfigurationManager gliaSdkConfigurationManager;
    private final Set<ViewCallback> viewCallbacks = new HashSet<>();

    @VisibleForTesting
    public boolean hasPendingScreenSharingRequest = false;

    public ScreenSharingController(
            GliaScreenSharingRepository gliaScreenSharingRepository,
            DialogController gliaDialogController,
            ShowScreenSharingNotificationUseCase showScreenSharingNotificationUseCase,
            RemoveScreenSharingNotificationUseCase removeScreenSharingNotificationUseCase,
            HasScreenSharingNotificationChannelEnabledUseCase hasScreenSharingNotificationChannelEnabledUseCase,
            GliaSdkConfigurationManager sdkConfigurationManager
    ) {
        Logger.d(TAG, "init");
        this.repository = gliaScreenSharingRepository;
        this.dialogController = gliaDialogController;
        this.showScreenSharingNotificationUseCase = showScreenSharingNotificationUseCase;
        this.removeScreenSharingNotificationUseCase = removeScreenSharingNotificationUseCase;
        this.hasScreenSharingNotificationChannelEnabledUseCase = hasScreenSharingNotificationChannelEnabledUseCase;
        this.gliaSdkConfigurationManager = sdkConfigurationManager;
    }

    public void init() {
        repository.init(this);
    }

    @Override
    public void onScreenSharingRequest() {
        Logger.d(TAG, "on screen sharing request");
        if (!viewCallbacks.isEmpty()) {
            if (!hasScreenSharingNotificationChannelEnabledUseCase.execute()) {
                hasPendingScreenSharingRequest = true;
                dialogController.showEnableScreenSharingNotificationsAndStartSharingDialog();
            } else {
                dialogController.showStartScreenSharingDialog();
            }
        }
    }

    @Override
    public void onScreenSharingStarted() {
        Logger.d(TAG, "screen sharing started");
    }

    @Override
    public void onScreenSharingEnded() {
        Logger.d(TAG, "screen sharing ended");
    }

    @Override
    public void onScreenSharingRequestError(GliaException exception) {
        Logger.e(TAG, "screen sharing request error: " + exception.getMessage());
        viewCallbacks.forEach(callback ->
                callback.onScreenSharingRequestError(exception)
        );
        hideScreenSharingEnabledNotification();
    }

    @Override
    public void onScreenSharingRequestSuccess() {
        Logger.d(TAG, "screen sharing request success");
        viewCallbacks.forEach(ViewCallback::onScreenSharingStarted);
    }

    public void onResume(Context context) {
        // spam all the time otherwise no way to end screen sharing
        if (hasPendingScreenSharingRequest) {
            if (!hasScreenSharingNotificationChannelEnabledUseCase.execute()) {
                dialogController.showEnableScreenSharingNotificationsAndStartSharingDialog();
            } else {
                onScreenSharingAccepted(context);
            }
        }
    }

    public void onDestroy() {
        Logger.d(TAG, "onDestroy");
        repository.onDestroy();
    }

    public boolean isSharingScreen() {
        return repository.isSharingScreen();
    }

    public void onScreenSharingAccepted(Context context) {
        Logger.d(TAG, "onScreenSharingAccepted");
        dialogController.dismissCurrentDialog();
        showScreenSharingEnabledNotification();
        repository.onScreenSharingAccepted(
                Utils.getActivity(context),
                gliaSdkConfigurationManager.getScreenSharingMode()
        );
        hasPendingScreenSharingRequest = false;
    }

    public void onScreenSharingDeclined() {
        Logger.d(TAG, "onScreenSharingDeclined");
        dialogController.dismissCurrentDialog();
        repository.onScreenSharingDeclined();
        hasPendingScreenSharingRequest = false;
    }

    public void onScreenSharingNotificationEndPressed() {
        hideScreenSharingEnabledNotification();
        repository.onEndScreenSharing();
    }

    public void setViewCallback(ViewCallback callback) {
        viewCallbacks.add(callback);
    }

    public void removeViewCallback(ViewCallback callback) {
        viewCallbacks.remove(callback);
    }

    private void showScreenSharingEnabledNotification() {
        showScreenSharingNotificationUseCase.execute();
    }

    private void hideScreenSharingEnabledNotification() {
        removeScreenSharingNotificationUseCase.execute();
    }

    public interface ViewCallback {
        void onScreenSharingRequestError(GliaException ex);
        void onScreenSharingStarted();
    }

    @Override
    public void onForceStopScreenSharing() {
        repository.forceEndScreenSharing();
    }
}
