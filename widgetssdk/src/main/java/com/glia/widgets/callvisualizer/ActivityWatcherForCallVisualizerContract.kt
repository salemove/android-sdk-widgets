package com.glia.widgets.callvisualizer

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import com.glia.widgets.core.dialog.model.ConfirmationDialogLinks
import com.glia.widgets.core.dialog.model.DialogState
import com.glia.widgets.core.dialog.model.Link

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
        fun onMediaProjectionPermissionResult(isGranted: Boolean, activity: Activity)
        fun onMediaProjectionRejected()
        fun isWaitingMediaProjectionResult(): Boolean
        fun setIsWaitingMediaProjectionResult(isWaiting: Boolean)
        fun addScreenSharingCallback(activity: Activity)
        fun mediaProjectionOnActivityResultSkipPermissionRequest(resultCode: Int, data: Intent?)
    }

    interface Watcher {
        fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT)
        fun removeDialogFromStack()
        fun dismissAlertDialog()
        fun showOverlayPermissionsDialog()
        fun showScreenSharingDialog(operatorName: String?)
        fun showAllowNotificationsDialog()
        fun showAllowScreenSharingNotificationsAndStartSharingDialog()
        fun showVisitorCodeDialog()
        fun openNotificationChannelScreen()
        fun openOverlayPermissionView()
        fun setupDialogCallback()
        fun removeDialogCallback()
        fun requestOverlayPermission()
        fun openSupportActivity(permissionType: PermissionType)
        fun openWebBrowserActivity(title: String, url: String)
        fun destroySupportActivityIfExists()
        fun callOverlayDialog()
        fun dismissOverlayDialog()
        fun isSupportActivityOpen(): Boolean
        fun showEngagementConfirmationDialog()
        fun engagementStarted()
        fun isWebBrowserActivityOpen(): Boolean
    }
}
