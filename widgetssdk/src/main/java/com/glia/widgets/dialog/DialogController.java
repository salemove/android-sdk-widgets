package com.glia.widgets.dialog;

import com.glia.androidsdk.comms.MediaUpgradeOffer;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.model.DialogsState;
import com.glia.widgets.view.DialogOfferType;

import java.util.ArrayList;
import java.util.List;

public class DialogController {
    private final static String TAG = "DialogController";

    private volatile DialogsState dialogsState;

    private final List<Callback> viewCallbacks = new ArrayList<>();

    public DialogController() {
        this.dialogsState = new DialogsState.NoDialog();
    }

    private synchronized boolean setDialogState(DialogsState dialogsState) {
        if (this.dialogsState.equals(dialogsState)) return false;
        this.dialogsState = dialogsState;
        return true;
    }

    public boolean isShowingChatEnderDialog() {
        return
                this.dialogsState instanceof DialogsState.NoMoreOperatorsDialog ||
                        this.dialogsState instanceof DialogsState.UnexpectedErrorDialog;
    }

    public boolean isNoDialogShown() {
        return dialogsState instanceof DialogsState.NoDialog;
    }

    public void dismissDialogs() {
        Logger.d(TAG, "Dismiss dialogs");
        emitDialogState(new DialogsState.NoDialog());
    }

    private synchronized void emitDialogState(DialogsState state) {
        if (setDialogState(state)) {
            Logger.d(TAG, "Emit dialog state:\n" + dialogsState.toString());
            for (Callback callback : viewCallbacks) {
                callback.emitDialog(dialogsState);
            }
        }
    }

    public void showExitQueueDialog() {
        if (isNoDialogShown()) {
            Logger.d(TAG, "Show Exit Queue Dialog");
            emitDialogState(new DialogsState.ExitQueueDialog());
        }
    }

    public void showExitChatDialog(String operatorName) {
        if (isNoDialogShown()) {
            Logger.d(TAG, "Show Exit Chat Dialog");
            emitDialogState(new DialogsState.EndEngagementDialog(operatorName));
        }
    }

    public void showUpgradeAudioDialog(MediaUpgradeOffer mediaUpgradeOffer, String operatorName) {
        if (isNoDialogShown()) {
            Logger.d(TAG, "Show Upgrade Audio Dialog");
            emitDialogState(
                    new DialogsState.UpgradeDialog(
                            new DialogOfferType.AudioUpgradeOffer(
                                    mediaUpgradeOffer,
                                    operatorName
                            )
                    )
            );
        }
    }

    public void showUpgradeVideoDialog2Way(MediaUpgradeOffer mediaUpgradeOffer, String operatorName) {
        if (isNoDialogShown()) {
            Logger.d(TAG, "Show Upgrade Video 2way Dialog");
            emitDialogState(
                    new DialogsState.UpgradeDialog(
                            new DialogOfferType.VideoUpgradeOffer2Way(
                                    mediaUpgradeOffer,
                                    operatorName
                            )
                    )
            );
        }
    }

    public void showUpgradeVideoDialog1Way(MediaUpgradeOffer mediaUpgradeOffer, String operatorName) {
        if (isNoDialogShown()) {
            Logger.d(TAG, "Show Upgrade Video 1way Dialog");
            emitDialogState(
                    new DialogsState.UpgradeDialog(
                            new DialogOfferType.VideoUpgradeOffer1Way(
                                    mediaUpgradeOffer,
                                    operatorName
                            )
                    )
            );
        }
    }

    public void showNoMoreOperatorsAvailableDialog() {
        if (isNoDialogShown()) {
            Logger.d(TAG, "Show No More Operators Dialog");
            emitDialogState(new DialogsState.NoMoreOperatorsDialog());
        }
    }

    public void showUnexpectedErrorDialog() {
        if (isNoDialogShown()) {
            Logger.d(TAG, "Show Unexpected error Dialog");
            emitDialogState(new DialogsState.UnexpectedErrorDialog());
        }
    }

    public void showOverlayPermissionsDialog() {
        if (isNoDialogShown()) {
            Logger.d(TAG, "Show Overlay permissions Dialog");
            emitDialogState(new DialogsState.OverlayPermissionsDialog());
        }
    }

    public void showStartScreenSharingDialog() {
        if (isNoDialogShown()) {
            Logger.d(TAG, "Show Start Screen Sharing Dialog");
            emitDialogState(new DialogsState.StartScreenSharingDialog());
        }
    }

    public void showEndScreenSharingDialog() {
        if (isNoDialogShown()) {
            Logger.d(TAG, "Show End Screen Sharing Dialog");
            emitDialogState(new DialogsState.EndScreenSharingDialog());
        }
    }

    public void showEnableNotificationChannelDialog() {
        if (isNoDialogShown()) {
            Logger.d(TAG, "Show Enable Notification Channel Dialog");
            emitDialogState(new DialogsState.EnableNotificationChannelDialog());
        }
    }

    public void showEnableScreenSharingNotificationsAndStartSharingDialog() {
        if (isNoDialogShown()) {
            Logger.d(TAG, "Show Enable Notification Channel Dialog");
            emitDialogState(new DialogsState.EnableScreenSharingNotificationsAndStartSharingDialog());
        }
    }

    public void addCallback(Callback callback) {
        Logger.d(TAG, "addCallback");
        callback.emitDialog(dialogsState);
        viewCallbacks.add(callback);
    }

    public void removeCallback(Callback callback) {
        Logger.d(TAG, "removeCallback");
        viewCallbacks.remove(callback);
    }

    public interface Callback {
        void emitDialog(DialogsState dialogsState);
    }
}
