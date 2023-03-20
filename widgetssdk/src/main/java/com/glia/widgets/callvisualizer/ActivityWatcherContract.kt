package com.glia.widgets.callvisualizer

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.widgets.core.dialog.model.DialogState

class ActivityWatcherContract {

    interface Controller {
        fun onActivityPaused()
        fun onActivityResumed(activity: Activity)
        fun isCallOrChatActive(activity: Activity): Boolean
        fun removeMediaProjectionLaunchers(activityName: String?)
        fun startMediaProjectionLaunchers(activityName: String, launcher: ActivityResultLauncher<Intent>?)
        fun setWatcher(watcher: Watcher)
        fun onPositiveDialogButtonClicked(activity: Activity? = null)
        fun onNegativeDialogButtonClicked()
        fun onDialogControllerCallback(state: DialogState)
        fun onMediaUpgradeReceived(mediaUpgrade: MediaUpgradeOffer)
        fun onInitialCameraPermissionResult(isGranted: Boolean, isComponentActivity: Boolean = true)
        fun onRequestedCameraPermissionResult(isGranted: Boolean)
    }

    interface Watcher {
        fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT)
        fun openCallActivity()
        fun dismissAlertDialog(manualDismiss: Boolean)
        fun showUpgradeDialog(mediaUpgrade: DialogState.MediaUpgrade)
        fun showOverlayPermissionsDialog()
        fun showScreenSharingDialog()
        fun showAllowNotificationsDialog()
        fun showAllowScreenSharingNotificationsAndStartSharingDialog()
        fun showVisitorCodeDialog()
        fun openNotificationChannelScreen()
        fun openOverlayPermissionView()
        fun setupDialogCallback()
        fun removeDialogCallback()
        fun requestCameraPermission()
        fun openComponentActivity()
        fun destroySupportActivityIfExists()
        fun checkCameraPermission()
    }
}
