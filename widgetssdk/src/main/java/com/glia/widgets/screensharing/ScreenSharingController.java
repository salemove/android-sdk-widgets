package com.glia.widgets.screensharing;

import android.content.Context;

import com.glia.androidsdk.GliaException;
import com.glia.widgets.dialog.DialogController;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.helper.Utils;
import com.glia.widgets.model.GliaScreenSharingRepository;
import com.glia.widgets.notification.NotificationActionReceiver;
import com.glia.widgets.notification.domain.RemoveScreenSharingNotificationUseCase;
import com.glia.widgets.notification.domain.ShowScreenSharingNotificationUseCase;

public class ScreenSharingController {
    private static final String TAG = "GliaScreenSharingController";
    private final GliaScreenSharingRepository repository;
    private final DialogController dialogController;
    private final ShowScreenSharingNotificationUseCase showScreenSharingNotificationUseCase;
    private final RemoveScreenSharingNotificationUseCase removeScreenSharingNotificationUseCase;

    private ViewCallback viewCallback;

    public ScreenSharingController(
            GliaScreenSharingRepository gliaScreenSharingRepository,
            DialogController gliaDialogController,
            ShowScreenSharingNotificationUseCase showScreenSharingNotificationUseCase,
            RemoveScreenSharingNotificationUseCase removeScreenSharingNotificationUseCase,
            ViewCallback callback
    ) {
        Logger.d(TAG, "init");
        repository = gliaScreenSharingRepository;
        dialogController = gliaDialogController;
        this.showScreenSharingNotificationUseCase = showScreenSharingNotificationUseCase;
        this.removeScreenSharingNotificationUseCase = removeScreenSharingNotificationUseCase;
        viewCallback = callback;
        initControllerCallback();
    }

    private void initControllerCallback() {
        repository.init(new GliaScreenSharingCallback() {
            @Override
            public void onScreenSharingRequest() {
                if (viewCallback != null) dialogController.showStartScreenSharingDialog();
                Logger.d(TAG, "on screen sharing request");
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
                if (viewCallback != null) viewCallback.onScreenSharingRequestError(exception);
                Logger.e(TAG, "screen sharing request error");
                exception.printStackTrace();
                hideScreenSharingEnabledNotification();
            }

            @Override
            public void onScreenSharingRequestSuccess() {
                Logger.d(TAG, "screen sharing request success");
            }
        });
    }

    public void onScreenSharingAccepted(Context context) {
        Logger.d(TAG, "onScreenSharingAccepted");
        dialogController.dismissDialogs();
        showScreenSharingEnabledNotification();
        repository.onScreenSharingAccepted(Utils.getActivity(context));
    }

    public void onScreenSharingDeclined() {
        Logger.d(TAG, "onScreenSharingDeclined");
        dialogController.dismissDialogs();
        repository.onScreenSharingDeclined();
    }

    public void onDismissEndScreenSharing() {
        Logger.d(TAG, "onDismissEndScreenSharing");
        dialogController.dismissDialogs();
    }

    public void onEndScreenSharing() {
        Logger.d(TAG, "onEndScreenSharing");
        dialogController.dismissDialogs();
        hideScreenSharingEnabledNotification();
        repository.onEndScreenSharing();
    }

    public void onDestroy(boolean retain) {
        Logger.d(TAG, "onDestroy retain=" + retain);
        viewCallback = null;
        if (!retain) {
            repository.onDestroy();
        }
    }

    public void onScreenSharingNotificationEndPressed(Context context) {
        Logger.d(TAG, "onScreenSharingNotificationEndPressed");
        if (viewCallback == null) {
            onEndScreenSharing();
        } else {
            dialogController.showEndScreenSharingDialog();
        }
    }

    private void showScreenSharingEnabledNotification() {
        showScreenSharingNotificationUseCase.execute();
    }

    private void hideScreenSharingEnabledNotification() {
        removeScreenSharingNotificationUseCase.execute();
    }

    public void setGliaScreenSharingCallback(ViewCallback callback) {
        Logger.d(TAG, "setCallback");
        viewCallback = callback;
    }

    public interface ViewCallback {
        void onScreenSharingRequestError(GliaException ex);
    }
}
