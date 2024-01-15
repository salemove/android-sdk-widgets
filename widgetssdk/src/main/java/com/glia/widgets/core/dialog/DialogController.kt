package com.glia.widgets.core.dialog

import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.widgets.core.dialog.domain.SetEnableCallNotificationChannelDialogShownUseCase
import com.glia.widgets.core.dialog.domain.SetOverlayPermissionRequestDialogShownUseCase
import com.glia.widgets.core.dialog.model.DialogState
import com.glia.widgets.core.dialog.model.MediaUpgradeMode
import com.glia.widgets.helper.Logger

private const val TAG = "DialogController"

internal interface DialogContract {
    interface Controller {
        val isShowingUnexpectedErrorDialog: Boolean
        val isEnableScreenSharingNotificationsAndStartSharingDialogShown: Boolean

        fun dismissCurrentDialog()
        fun dismissDialogs()
        fun showExitQueueDialog()
        fun showExitChatDialog()
        fun showUpgradeAudioDialog(mediaUpgradeOffer: MediaUpgradeOffer, operatorName: String?)
        fun showUpgradeVideoDialog2Way(mediaUpgradeOffer: MediaUpgradeOffer, operatorName: String?)
        fun showUpgradeVideoDialog1Way(mediaUpgradeOffer: MediaUpgradeOffer, operatorName: String?)
        fun showVisitorCodeDialog()
        fun dismissVisitorCodeDialog()
        fun showUnexpectedErrorDialog()
        fun showOverlayPermissionsDialog()
        fun dismissOverlayPermissionsDialog()
        fun dismissMessageCenterUnavailableDialog()
        fun showStartScreenSharingDialog(operatorName: String?)
        fun showEnableCallNotificationChannelDialog()
        fun showEnableScreenSharingNotificationsAndStartSharingDialog()
        fun showMessageCenterUnavailableDialog()
        fun showUnauthenticatedDialog()
        fun showEngagementConfirmationDialog()
        fun addCallback(callback: Callback)
        fun removeCallback(callback: Callback)
        fun interface Callback {
            fun emitDialogState(dialogState: DialogState)
        }
    }
}

internal class DialogController(
    private val setOverlayPermissionRequestDialogShownUseCase: SetOverlayPermissionRequestDialogShownUseCase,
    private val setEnableCallNotificationChannelDialogShownUseCase: SetEnableCallNotificationChannelDialogShownUseCase
) : DialogContract.Controller {
    private val viewCallbacks: MutableSet<DialogContract.Controller.Callback> = HashSet()
    private val dialogManager: DialogManager by lazy { DialogManager(::emitDialogState) }
    override val isShowingUnexpectedErrorDialog: Boolean
        get() = dialogManager.currentDialogState is DialogState.UnexpectedError

    override val isEnableScreenSharingNotificationsAndStartSharingDialogShown: Boolean
        get() = DialogState.EnableScreenSharingNotificationsAndStartSharing == dialogManager.currentDialogState
    private val isOverlayDialogShown: Boolean
        get() = DialogState.OverlayPermission == dialogManager.currentDialogState

    override fun dismissCurrentDialog() {
        Logger.d(TAG, "Dismiss current dialog")
        dialogManager.dismissCurrent()
    }

    override fun dismissDialogs() {
        Logger.d(TAG, "Dismiss dialogs")
        dialogManager.dismissAll()
    }

    private fun emitDialogState(dialogState: DialogState) {
        Logger.d(TAG, "Emit dialog state:\n$dialogState")
        viewCallbacks.forEach { it.emitDialogState(dialogState) }
    }

    override fun showExitQueueDialog() {
        Logger.i(TAG, "Show Exit Queue Dialog")
        dialogManager.addAndEmit(DialogState.ExitQueue)
    }

    override fun showExitChatDialog() {
        Logger.i(TAG, "Show End Engagement Dialog")
        dialogManager.addAndEmit(DialogState.EndEngagement)
    }

    override fun showUpgradeAudioDialog(mediaUpgradeOffer: MediaUpgradeOffer, operatorName: String?) {
        Logger.i(TAG, "Show Upgrade Audio Dialog")
        dialogManager.addAndEmit(DialogState.MediaUpgrade(mediaUpgradeOffer, operatorName, MediaUpgradeMode.AUDIO))
    }

    override fun showUpgradeVideoDialog2Way(mediaUpgradeOffer: MediaUpgradeOffer, operatorName: String?) {
        Logger.i(TAG, "Show Upgrade 2WayVideo Dialog")
        dialogManager.addAndEmit(DialogState.MediaUpgrade(mediaUpgradeOffer, operatorName, MediaUpgradeMode.VIDEO_TWO_WAY))
    }

    override fun showUpgradeVideoDialog1Way(mediaUpgradeOffer: MediaUpgradeOffer, operatorName: String?) {
        Logger.i(TAG, "Show Upgrade 1WayVideo Dialog")
        dialogManager.addAndEmit(DialogState.MediaUpgrade(mediaUpgradeOffer, operatorName, MediaUpgradeMode.VIDEO_ONE_WAY))
    }

    override fun showVisitorCodeDialog() {
        Logger.i(TAG, "Show Visitor Code Dialog")
        if (isOverlayDialogShown) {
            dialogManager.add(DialogState.VisitorCode)
        } else {
            dialogManager.addAndEmit(DialogState.VisitorCode)
        }
    }

    override fun dismissVisitorCodeDialog() {
        Logger.i(TAG, "Dismiss Visitor Code Dialog")
        dialogManager.remove(DialogState.VisitorCode)
    }

    override fun showUnexpectedErrorDialog() {
        // Prioritise this error as it is engagement fatal error indicator
        // (e.g., GliaException:{"details":"Queue is closed","error":"Unprocessable entity"}) for example
        Logger.i(TAG, "Show Unexpected error Dialog")
        dialogManager.addAndEmit(DialogState.UnexpectedError)
    }

    override fun showOverlayPermissionsDialog() {
        Logger.i(TAG, "Show Overlay permissions Dialog")
        setOverlayPermissionRequestDialogShownUseCase.execute()
        dialogManager.addAndEmit(DialogState.OverlayPermission)
    }

    override fun dismissOverlayPermissionsDialog() {
        Logger.d(TAG, "Dismiss Overlay Permissions Dialog")
        dialogManager.remove(DialogState.OverlayPermission)
    }

    override fun dismissMessageCenterUnavailableDialog() {
        Logger.d(TAG, "Dismiss Message Center Unavailable Dialog")
        dialogManager.remove(DialogState.MessageCenterUnavailable)
    }

    override fun showStartScreenSharingDialog(operatorName: String?) {
        Logger.i(TAG, "Show Start Screen Sharing Dialog")
        dialogManager.addAndEmit(DialogState.StartScreenSharing(operatorName))
    }

    override fun showEnableCallNotificationChannelDialog() {
        Logger.i(TAG, "Show Enable Call Notification Channel Dialog")
        setEnableCallNotificationChannelDialogShownUseCase.execute()
        dialogManager.addAndEmit(DialogState.EnableNotificationChannel)
    }

    override fun showEnableScreenSharingNotificationsAndStartSharingDialog() {
        Logger.i(TAG, "Show Enable Screen Sharing Notifications And Start Screen Sharing Dialog")
        dialogManager.addAndEmit(DialogState.EnableScreenSharingNotificationsAndStartSharing)
    }

    override fun showMessageCenterUnavailableDialog() {
        Logger.i(TAG, "Show Message Center Unavailable Dialog")
        dialogManager.addAndEmit(DialogState.MessageCenterUnavailable)
    }

    override fun showUnauthenticatedDialog() {
        Logger.i(TAG, "Show Unauthenticated Dialog")
        dialogManager.addAndEmit(DialogState.Unauthenticated)
    }

    override fun showEngagementConfirmationDialog() {
        Logger.d(TAG, "Show Live Observation Opt In Dialog")
        if (isOverlayDialogShown) {
            dialogManager.add(DialogState.Confirmation)
        } else {
            dialogManager.addAndEmit(DialogState.Confirmation)
        }
    }

    override fun addCallback(callback: DialogContract.Controller.Callback) {
        Logger.d(TAG, "addCallback")
        viewCallbacks.add(callback)
        if (viewCallbacks.size == 1) {
            dialogManager.showNext()
        }
    }

    override fun removeCallback(callback: DialogContract.Controller.Callback) {
        Logger.d(TAG, "removeCallback")
        viewCallbacks.remove(callback)
    }

}
