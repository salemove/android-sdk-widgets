package com.glia.widgets.helper

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog
import com.glia.androidsdk.GliaException
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.callvisualizer.controller.CallVisualizerController
import com.glia.widgets.core.dialog.Dialog
import com.glia.widgets.core.dialog.DialogController
import com.glia.widgets.core.dialog.model.DialogState
import com.glia.widgets.core.notification.device.NotificationManager
import com.glia.widgets.core.screensharing.ScreenSharingController
import com.glia.widgets.view.Dialogs
import com.google.android.material.theme.overlay.MaterialThemeOverlay
import java.lang.ref.WeakReference

class ActivityWatcherForDialogs(
    private val callVisualizerController: CallVisualizerController,
    private val screenSharingController: ScreenSharingController,
    private val dialogController: DialogController,
) : Application.ActivityLifecycleCallbacks {

    companion object {
        private val TAG = ActivityWatcherForDialogs::class.java.simpleName
    }

    @VisibleForTesting
    var dialogCallback: DialogController.Callback? = null
    private var screenSharingViewCallback: ScreenSharingController.ViewCallback? = null

    @VisibleForTesting
    var alertDialog: AlertDialog? = null

    /**
     * Returns last activity that called [Activity.onResume], but didn't call [Activity.onPause] yet
     * @return Currently resumed activity.
     */
    @VisibleForTesting
    var resumedActivity: WeakReference<Activity?> = WeakReference(null)

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityDestroyed(activity: Activity) {}


    override fun onActivityResumed(activity: Activity) {
        resumedActivity = WeakReference(activity)
        addDialogCallback(resumedActivity)
        addScreenSharingCallback(resumedActivity)
    }

    override fun onActivityPaused(activity: Activity) {
        resumedActivity.clear()
        removeDialogCallback()
        removeScreenSharingCallback()
    }


    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    private fun addDialogCallback(resumedActivity: WeakReference<Activity?>) {
        // There are separate dialog callbacks for incoming media requests on Call and Chat screens.
        if (callVisualizerController.isCallOrChatScreenActiveUseCase(resumedActivity.get())) return

        setupDialogCallback(resumedActivity)
        dialogController.addCallback(dialogCallback)
    }

    private fun removeDialogCallback() {
        dialogController.removeCallback(dialogCallback)
    }

    private fun addScreenSharingCallback(resumedActivity: WeakReference<Activity?>) {
        val activity = resumedActivity.get() ?: return

        setupScreenSharingViewCallback(resumedActivity)
        screenSharingController.setViewCallback(screenSharingViewCallback)
        screenSharingController.onResume(activity)
    }

    private fun removeScreenSharingCallback() {
        screenSharingController.removeViewCallback(screenSharingViewCallback)
    }

    private fun setupScreenSharingViewCallback(resumedActivity: WeakReference<Activity?>) {
        screenSharingViewCallback = resumedActivity.get()?.let {
            ScreenSharingController.ViewCallback { exception: GliaException ->
                showToast(it, exception.debugMessage)
            }
        }
    }

    @VisibleForTesting
    fun setupDialogCallback(resumedActivity: WeakReference<Activity?>) {
        val activity = resumedActivity.get() ?: return

        dialogCallback = DialogController.Callback {
            when (it.mode) {
                Dialog.MODE_NONE -> dismissAlertDialog()
                Dialog.MODE_MEDIA_UPGRADE -> activity.runOnUiThread {
                    showUpgradeDialog(resumedActivity, it as DialogState.MediaUpgrade)
                }
                Dialog.MODE_OVERLAY_PERMISSION -> activity.runOnUiThread {
                    showOverlayPermissionsDialog(resumedActivity)
                }
                Dialog.MODE_START_SCREEN_SHARING -> activity.runOnUiThread {
                    showScreenSharingDialog(resumedActivity)
                }
                Dialog.MODE_ENABLE_NOTIFICATION_CHANNEL -> activity.runOnUiThread {
                    showAllowNotificationsDialog(resumedActivity)
                }
                Dialog.MODE_ENABLE_SCREEN_SHARING_NOTIFICATIONS_AND_START_SHARING -> resumedActivity?.runOnUiThread {
                    showAllowScreenSharingNotificationsAndStartSharingDialog(resumedActivity)
                }
                else -> {
                    Logger.d(TAG, "Unexpected dialog mode received")
                }
            }
        }
    }

    private fun showOverlayPermissionsDialog(resumedActivity: WeakReference<Activity?>) {
        val activity = resumedActivity.get() ?: return
        if (alertDialog != null && alertDialog!!.isShowing) {
            return
        }

        alertDialog = Dialogs.showOptionsDialog(
            prepareContextWithStyle(activity),
            UiTheme.UiThemeBuilder().build(),
            activity.getString(R.string.glia_dialog_overlay_permissions_title),
            activity.getString(R.string.glia_dialog_overlay_permissions_message),
            activity.getString(R.string.glia_dialog_overlay_permissions_ok),
            activity.getString(R.string.glia_dialog_overlay_permissions_no),
            {
                dismissAlertDialog()
                dialogController.dismissCurrentDialog()
                val overlayIntent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + activity.packageName)
                )
                overlayIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                activity.startActivity(overlayIntent)
            },
            {
                dismissAlertDialog()
                dialogController.dismissCurrentDialog()
            },
            { dialog: DialogInterface ->
                dialogController.dismissCurrentDialog()
                dialog.dismiss()
            })
    }

    private fun showAllowNotificationsDialog(resumedActivity: WeakReference<Activity?>) {
        val activity = resumedActivity.get() ?: return
        if (alertDialog != null && alertDialog!!.isShowing) {
            return
        }

        val contextWithStyle = prepareContextWithStyle(activity)
        alertDialog = Dialogs.showOptionsDialog(
            context = contextWithStyle,
            theme = UiTheme.UiThemeBuilder().build(),
            title = activity.getString(R.string.glia_dialog_allow_notifications_title),
            message = activity.getString(R.string.glia_dialog_allow_notifications_message),
            positiveButtonText = activity.getString(R.string.glia_dialog_allow_notifications_yes),
            negativeButtonText = activity.getString(R.string.glia_dialog_allow_notifications_no),
            positiveButtonClickListener = {
                dismissAlertDialog()
                dialogController.dismissCurrentDialog()
                NotificationManager.openNotificationChannelScreen(contextWithStyle)
            },
            negativeButtonClickListener = {
                dismissAlertDialog()
                dialogController.dismissCurrentDialog()
            },
            cancelListener = {
                it.dismiss()
                dialogController.dismissCurrentDialog()
            }
        )
    }

    private fun showAllowScreenSharingNotificationsAndStartSharingDialog(resumedActivity: WeakReference<Activity?>) {
        val activity = resumedActivity.get() ?: return
        if (alertDialog != null && alertDialog!!.isShowing) {
            return
        }

        Logger.d(TAG, "Show screen sharing dialog")
        val builder = UiTheme.UiThemeBuilder()
        val theme = builder.build()
        val contextWithStyle = prepareContextWithStyle(activity)
        alertDialog = Dialogs.showOptionsDialog(
            context = contextWithStyle,
            theme = theme,
            title = activity.getString(R.string.glia_dialog_screen_sharing_offer_enable_notifications_title),
            message = activity.getString(R.string.glia_dialog_screen_sharing_offer_enable_notifications_message),
            positiveButtonText = activity.getString(R.string.glia_dialog_screen_sharing_offer_enable_notifications_yes),
            negativeButtonText = activity.getString(R.string.glia_dialog_screen_sharing_offer_enable_notifications_no),
            positiveButtonClickListener = {
                dismissAlertDialog()
                dialogController.dismissCurrentDialog()
                NotificationManager.openNotificationChannelScreen(contextWithStyle)
            },
            negativeButtonClickListener = {
                dismissAlertDialog()
                dialogController.dismissCurrentDialog()
                screenSharingController.onScreenSharingDeclined()
            },
            cancelListener = {
                it.dismiss()
                dialogController.dismissCurrentDialog()
                screenSharingController.onScreenSharingDeclined()
            }
        )
    }

    private fun showScreenSharingDialog(resumedActivity: WeakReference<Activity?>) {
        val activity = resumedActivity.get() ?: return
        if (alertDialog != null && alertDialog!!.isShowing) {
            return
        }

        Logger.d(TAG, "Show screen sharing dialog")
        val builder = UiTheme.UiThemeBuilder()
        val theme = builder.build()
        val contextWithStyle = prepareContextWithStyle(activity)

        alertDialog = Dialogs.showScreenSharingDialog(
            contextWithStyle,
            theme,
            activity.getText(R.string.glia_dialog_screen_sharing_offer_title).toString(),
            activity.getText(R.string.glia_dialog_screen_sharing_offer_message).toString(),
            R.string.glia_dialog_screen_sharing_offer_accept,
            R.string.glia_dialog_screen_sharing_offer_decline,
            { screenSharingController.onScreenSharingAccepted(contextWithStyle) }
        ) { screenSharingController.onScreenSharingDeclined() }
    }

    private fun showUpgradeDialog(
        resumedActivity: WeakReference<Activity?>,
        mediaUpgrade: DialogState.MediaUpgrade
    ) {
        val activity = resumedActivity.get() ?: return
        if (alertDialog != null && alertDialog!!.isShowing) {
            return
        }

        Logger.d(TAG, "Show upgrade dialog")
        val builder = UiTheme.UiThemeBuilder()
        val theme = builder.build()
        val contextWithStyle = prepareContextWithStyle(activity)

        alertDialog = Dialogs.showUpgradeDialog(contextWithStyle, theme, mediaUpgrade, {
            dialogController.dismissCurrentDialog()
            // TODO: 07.02.2023 handle media request accepting in the scope of respective ticket
        }) {
            dialogController.dismissCurrentDialog()
        }
    }

    private fun prepareContextWithStyle(resumedActivity: Activity): Context {
        return MaterialThemeOverlay.wrap(
            resumedActivity,
            null,
            R.attr.gliaChatStyle,
            R.style.Application_Glia_Chat
        )
    }

    private fun dismissAlertDialog() {
        Logger.d(TAG, "Dismiss alert dialog")
        alertDialog?.dismiss()
        alertDialog = null
    }

    private fun showToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, message, duration).show()
    }
}
