package com.glia.widgets.core.dialog;

import com.glia.androidsdk.comms.MediaUpgradeOffer;
import com.glia.widgets.core.dialog.domain.SetEnableCallNotificationChannelDialogShownUseCase;
import com.glia.widgets.core.dialog.domain.SetOverlayPermissionRequestDialogShownUseCase;
import com.glia.widgets.core.dialog.model.DialogState;
import com.glia.widgets.helper.Logger;

import java.util.HashSet;
import java.util.Set;

public class DialogController {
    private final static String TAG = "DialogController";

    private final Set<Callback> viewCallbacks = new HashSet<>();

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
        Logger.i(TAG, "Show Exit Queue Dialog");
        dialogManager.addAndEmit(new DialogState(Dialog.MODE_EXIT_QUEUE));
    }

    public void showExitChatDialog(String operatorName) {
        Logger.i(TAG, "Show End Engagement Dialog");
        dialogManager.addAndEmit(new DialogState.OperatorName(Dialog.MODE_END_ENGAGEMENT, operatorName));
    }

    public void showUpgradeAudioDialog(MediaUpgradeOffer mediaUpgradeOffer, String operatorName) {
        Logger.i(TAG, "Show Upgrade Audio Dialog");
        dialogManager.addAndEmit(new DialogState.MediaUpgrade(mediaUpgradeOffer, operatorName, DialogState.MediaUpgrade.MODE_AUDIO));
    }

    public void showUpgradeVideoDialog2Way(MediaUpgradeOffer mediaUpgradeOffer, String operatorName) {
        Logger.i(TAG, "Show Upgrade 2WayVideo Dialog");
        dialogManager.addAndEmit(new DialogState.MediaUpgrade(mediaUpgradeOffer, operatorName, DialogState.MediaUpgrade.MODE_VIDEO_TWO_WAY));
    }

    public void showUpgradeVideoDialog1Way(MediaUpgradeOffer mediaUpgradeOffer, String operatorName) {
        Logger.i(TAG, "Show Upgrade 1WayVide Dialog");
        dialogManager.addAndEmit(new DialogState.MediaUpgrade(mediaUpgradeOffer, operatorName, DialogState.MediaUpgrade.MODE_VIDEO_ONE_WAY));
    }

    public void showVisitorCodeDialog() {
        Logger.i(TAG, "Show Visitor Code Dialog");
        if (isOverlayDialogShown() || isExitQueueDialogShown()) {
            dialogManager.add(new DialogState(Dialog.MODE_VISITOR_CODE));
        } else {
            dialogManager.addAndEmit(new DialogState(Dialog.MODE_VISITOR_CODE));
        }
    }

    public void dismissVisitorCodeDialog() {
        Logger.i(TAG, "Dismiss Visitor Code Dialog");
        dialogManager.remove(new DialogState(Dialog.MODE_VISITOR_CODE));
    }

    public void showNoMoreOperatorsAvailableDialog() {
        Logger.i(TAG, "Show No More Operators Dialog");
        if (isOverlayDialogShown() || isExitQueueDialogShown()) {
            dialogManager.add(new DialogState(Dialog.MODE_NO_MORE_OPERATORS));
        } else {
            dialogManager.addAndEmit(new DialogState(Dialog.MODE_NO_MORE_OPERATORS));
        }
    }

    public void showEngagementEndedDialog() {
        Logger.i(TAG, "Show Engagement Ended Dialog");
        dialogManager.addAndEmit(new DialogState(Dialog.MODE_ENGAGEMENT_ENDED));
    }

    public void showUnexpectedErrorDialog() {
        // PRIORITISE THIS ERROR AS IT IS ENGAGEMENT FATAL ERROR INDICATOR (eg. GliaException:{"details":"Queue is closed","error":"Unprocessable entity"}) for example
        Logger.i(TAG, "Show Unexpected error Dialog");
        dialogManager.addAndEmit(new DialogState(Dialog.MODE_UNEXPECTED_ERROR));
    }

    public void showOverlayPermissionsDialog() {
        Logger.i(TAG, "Show Overlay permissions Dialog");
        setOverlayPermissionRequestDialogShownUseCase.execute();
        dialogManager.addAndEmit(new DialogState(Dialog.MODE_OVERLAY_PERMISSION));
    }

    public void dismissOverlayPermissionsDialog() {
        Logger.d(TAG, "Dismiss Overlay Permissions Dialog");
        dialogManager.remove(new DialogState(Dialog.MODE_OVERLAY_PERMISSION));
    }

    public void dismissMessageCenterUnavailableDialog() {
        Logger.d(TAG, "Dismiss Message Center Unavailable Dialog");
        dialogManager.remove(new DialogState(Dialog.MODE_MESSAGE_CENTER_UNAVAILABLE));
    }

    public void showStartScreenSharingDialog() {
        Logger.i(TAG, "Show Start Screen Sharing Dialog");
        dialogManager.addAndEmit(new DialogState(Dialog.MODE_START_SCREEN_SHARING));
    }

    public void showEnableCallNotificationChannelDialog() {
        Logger.i(TAG, "Show Enable Call Notification Channel Dialog");
        setEnableCallNotificationChannelDialogShownUseCase.execute();
        dialogManager.addAndEmit(new DialogState(Dialog.MODE_ENABLE_NOTIFICATION_CHANNEL));
    }

    public void showEnableScreenSharingNotificationsAndStartSharingDialog() {
        Logger.i(TAG, "Show Enable Screen Sharing Notifications And Start Screen Sharing Dialog");
        dialogManager.addAndEmit(new DialogState(Dialog.MODE_ENABLE_SCREEN_SHARING_NOTIFICATIONS_AND_START_SHARING));
    }

    public void showMessageCenterUnavailableDialog() {
        Logger.i(TAG, "Show Message Center Unavailable Dialog");
        dialogManager.addAndEmit(new DialogState(Dialog.MODE_MESSAGE_CENTER_UNAVAILABLE));
    }

    public void showUnauthenticatedDialog() {
        Logger.i(TAG, "Show Unauthenticated Dialog");
        dialogManager.addAndEmit(new DialogState(Dialog.MODE_UNAUTHENTICATED));
    }

    public void showEngagementConfirmationDialog() {
        Logger.d(TAG, "Show Live Observation Opt In Dialog");
        dialogManager.addAndEmit(new DialogState(Dialog.MODE_LIVE_OBSERVATION_OPT_IN));
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

    public boolean isEnableScreenSharingNotificationsAndStartSharingDialogShown() {
        return Dialog.MODE_ENABLE_SCREEN_SHARING_NOTIFICATIONS_AND_START_SHARING == dialogManager.getCurrentMode();
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
