package com.glia.widgets.core.dialog;

import com.glia.androidsdk.comms.MediaUpgradeOffer;
import com.glia.widgets.core.dialog.domain.SetEnableCallNotificationChannelDialogShownUseCase;
import com.glia.widgets.core.dialog.domain.SetOverlayPermissionRequestDialogShownUseCase;
import com.glia.widgets.core.dialog.model.DialogState;
import com.glia.widgets.helper.Logger;

import java.util.ArrayList;
import java.util.List;

public class DialogController {
    private final static String TAG = "DialogController";

    private final List<Callback> viewCallbacks = new ArrayList<>();

    private final SetOverlayPermissionRequestDialogShownUseCase setOverlayPermissionRequestDialogShownUseCase;
    private final SetEnableCallNotificationChannelDialogShownUseCase setEnableCallNotificationChannelDialogShownUseCase;
    private final DialogManager dialogManager;

    public DialogController(
            SetOverlayPermissionRequestDialogShownUseCase setOverlayPermissionRequestDialogShownUseCase,
            SetEnableCallNotificationChannelDialogShownUseCase setEnableCallNotificationChannelDialogShownUseCase
    ) {
        this.setOverlayPermissionRequestDialogShownUseCase = setOverlayPermissionRequestDialogShownUseCase;
        this.setEnableCallNotificationChannelDialogShownUseCase = setEnableCallNotificationChannelDialogShownUseCase;
        dialogManager = new DialogManager(this::emitDialogState);
    }

    public boolean isShowingChatEnderDialog() {
        int mode = dialogManager.getCurrentMode();

        return mode == Dialog.MODE_NO_MORE_OPERATORS || mode == Dialog.MODE_UNEXPECTED_ERROR;
    }

    public void dismissCurrentDialog() {
        Logger.d(TAG, "Dismiss current dialog");
        dialogManager.dismissCurrent();
    }

    public void dismissDialogs() {
        Logger.d(TAG, "Dismiss dialogs");
        dialogManager.dismissAll();
    }

    private void emitDialogState(DialogState dialogState) {
        Logger.d(TAG, "Emit dialog state:\n" + dialogState);
        for (Callback callback : viewCallbacks) {
            callback.emitDialogState(dialogState);
        }
    }

    public void showExitQueueDialog() {
        Logger.d(TAG, "Show Exit Queue Dialog");
        dialogManager.addAndEmit(new DialogState(Dialog.MODE_EXIT_QUEUE));
    }

    public void showExitChatDialog(String operatorName) {
        Logger.d(TAG, "Show Exit Chat Dialog");
        dialogManager.addAndEmit(new DialogState.OperatorName(Dialog.MODE_END_ENGAGEMENT, operatorName));
    }

    public void showUpgradeAudioDialog(MediaUpgradeOffer mediaUpgradeOffer, String operatorName) {
        Logger.d(TAG, "Show Upgrade Audio Dialog");
        dialogManager.addAndEmit(new DialogState.MediaUpgrade(mediaUpgradeOffer, operatorName, DialogState.MediaUpgrade.MODE_AUDIO));
    }

    public void showUpgradeVideoDialog2Way(MediaUpgradeOffer mediaUpgradeOffer, String operatorName) {
        Logger.d(TAG, "Show Upgrade 2WayVideo Dialog");
        dialogManager.addAndEmit(new DialogState.MediaUpgrade(mediaUpgradeOffer, operatorName, DialogState.MediaUpgrade.MODE_VIDEO_TWO_WAY));
    }

    public void showUpgradeVideoDialog1Way(MediaUpgradeOffer mediaUpgradeOffer, String operatorName) {
        Logger.d(TAG, "Show Upgrade 1WayVide Dialog");
        dialogManager.addAndEmit(new DialogState.MediaUpgrade(mediaUpgradeOffer, operatorName, DialogState.MediaUpgrade.MODE_VIDEO_ONE_WAY));
    }

    public void showNoMoreOperatorsAvailableDialog() {
        Logger.d(TAG, "Show No More Operators Dialog");
        if (isOverlayDialogShown() || isExitQueueDialogShown()) {
            dialogManager.add(new DialogState(Dialog.MODE_NO_MORE_OPERATORS));
        } else {
            dialogManager.addAndEmit(new DialogState(Dialog.MODE_NO_MORE_OPERATORS));
        }
    }

    public void showEngagementEndedDialog() {
        Logger.d(TAG, "Show Engagement EngagementEndedEvent Dialog");
        dialogManager.addAndEmit(new DialogState(Dialog.MODE_ENGAGEMENT_ENDED));
    }

    public void showUnexpectedErrorDialog() {
        // PRIORITISE THIS ERROR AS IT IS ENGAGEMENT FATAL ERROR INDICATOR (eg. GliaException:{"details":"Queue is closed","error":"Unprocessable entity"}) for example
        Logger.d(TAG, "Show Unexpected error Dialog");
        dialogManager.addAndEmit(new DialogState(Dialog.MODE_UNEXPECTED_ERROR));
    }

    public void showOverlayPermissionsDialog() {
        Logger.d(TAG, "Show Overlay permissions Dialog");
        setOverlayPermissionRequestDialogShownUseCase.execute();
        dialogManager.addAndEmit(new DialogState(Dialog.MODE_OVERLAY_PERMISSION));
    }

    public void showStartScreenSharingDialog() {
        Logger.d(TAG, "Show Start Screen Sharing Dialog");
        dialogManager.addAndEmit(new DialogState(Dialog.MODE_START_SCREEN_SHARING));
    }

    public void showEnableCallNotificationChannelDialog() {
        Logger.d(TAG, "Show Enable Notification Channel Dialog");
        setEnableCallNotificationChannelDialogShownUseCase.execute();
        dialogManager.addAndEmit(new DialogState(Dialog.MODE_ENABLE_NOTIFICATION_CHANNEL));
    }

    public void showEnableScreenSharingNotificationsAndStartSharingDialog() {
        Logger.d(TAG, "Show Enable Notification Channel Dialog");
        dialogManager.addAndEmit(new DialogState(Dialog.MODE_ENABLE_SCREEN_SHARING_NOTIFICATIONS_AND_START_SHARING));
    }

    public void showMessageCenterUnavailableDialog() {
        Logger.d(TAG, "Show Message Center Unavailable Dialog");
        dialogManager.addAndEmit(new DialogState(Dialog.MODE_MESSAGE_CENTER_UNAVAILABLE));
    }

    public void showUnauthenticatedDialog() {
        Logger.d(TAG, "Show Unauthenticated Dialog");
        dialogManager.addAndEmit(new DialogState(Dialog.MODE_UNAUTHENTICATED));
    }

    public void addCallback(Callback callback) {
        Logger.d(TAG, "addCallback");
        viewCallbacks.add(callback);
        if (viewCallbacks.size() == 1) {
            dialogManager.showNext();
        }
    }

    public void removeCallback(Callback callback) {
        Logger.d(TAG, "removeCallback");
        viewCallbacks.remove(callback);
    }

    private boolean isOverlayDialogShown() {
        return Dialog.MODE_OVERLAY_PERMISSION == dialogManager.getCurrentMode();
    }

    private boolean isExitQueueDialogShown() {
        return Dialog.MODE_EXIT_QUEUE == dialogManager.getCurrentMode();
    }

    public interface Callback {
        void emitDialogState(DialogState dialogState);
    }
}
