package com.glia.widgets.callvisualizer

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.widgets.core.dialog.model.ConfirmationDialogLinks
import com.glia.widgets.core.dialog.model.Link
import com.glia.widgets.core.dialog.model.DialogState

internal class ActivityWatcherForCallVisualizerContract {

    interface Controller {
        fun onActivityPaused()
        fun onActivityResumed(activity: Activity)
        fun isCallOrChatActive(activity: Activity): Boolean
        fun removeMediaProjectionLaunchers(activityName: String?)
        fun startMediaProjectionLaunchers(activityName: String, launcher: ActivityResultLauncher<Intent>?)
        fun setWatcher(watcher: Watcher)
        fun getConfirmationDialogLinks(): ConfirmationDialogLinks
        fun onLinkClicked(link: Link)
        fun onPositiveDialogButtonClicked(activity: Activity? = null)
        fun onNegativeDialogButtonClicked()
        fun onDialogControllerCallback(state: DialogState)
        fun onMediaUpgradeReceived(mediaUpgrade: MediaUpgradeOffer)
        fun onInitialCameraPermissionResult(isGranted: Boolean, isComponentActivity: Boolean = true)
        fun onRequestedCameraPermissionResult(isGranted: Boolean)
        fun onMediaProjectionPermissionResult(isGranted: Boolean, activity: Activity)
        fun onMediaProjectionRejected()
        fun isWaitingMediaProjectionResult(): Boolean
        fun setIsWaitingMediaProjectionResult(isWaiting: Boolean)
        fun addScreenSharingCallback(activity: Activity)
    }

    interface Watcher {
        fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT)
        fun openCallActivity()
        fun removeDialogFromStack()
        fun dismissAlertDialog()
        fun showUpgradeDialog(mediaUpgrade: DialogState.MediaUpgrade)
        fun showOverlayPermissionsDialog()
        fun showScreenSharingDialog(dialogState: DialogState.OperatorName)
        fun showAllowNotificationsDialog()
        fun showAllowScreenSharingNotificationsAndStartSharingDialog()
        fun showVisitorCodeDialog()
        fun openNotificationChannelScreen()
        fun openOverlayPermissionView()
        fun setupDialogCallback()
        fun removeDialogCallback()
        fun requestCameraPermission()
        fun requestOverlayPermission()
        fun openSupportActivity(permissionType: PermissionType)
        fun openWebBrowserActivity(title: String, url: String)
        fun destroySupportActivityIfExists()
        fun checkInitialCameraPermission()
        fun callOverlayDialog()
        fun dismissOverlayDialog()
        fun isSupportActivityOpen(): Boolean
        fun showEngagementConfirmationDialog()
        fun engagementStarted()
        fun isWebBrowserActivityOpen(): Boolean
    }
}
