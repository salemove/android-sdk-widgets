package com.glia.widgets.callvisualizer

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog
import com.glia.androidsdk.Glia
import com.glia.androidsdk.comms.MediaDirection
import com.glia.widgets.GliaWidgets
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.call.CallActivity
import com.glia.widgets.call.Configuration
import com.glia.widgets.chat.ChatView
import com.glia.widgets.core.dialog.DialogController
import com.glia.widgets.core.dialog.model.DialogState
import com.glia.widgets.core.notification.device.NotificationManager
import com.glia.widgets.core.screensharing.data.GliaScreenSharingRepository
import com.glia.widgets.di.Dependencies
import com.glia.widgets.filepreview.ui.FilePreviewView
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.Utils
import com.glia.widgets.view.Dialogs
import com.glia.widgets.view.unifiedui.exstensions.wrapWithMaterialThemeOverlay
import java.lang.ref.WeakReference

class ActivityWatcherForCallVisualizer(
    private val dialogController: DialogController,
    val controller: ActivityWatcherContract.Controller
    ) : Application.ActivityLifecycleCallbacks, ActivityWatcherContract.Watcher {

    companion object {
        private val TAG = ActivityWatcherForCallVisualizer::class.java.simpleName
    }

    init {
        controller.setWatcher(this)
    }

    var alertDialog: AlertDialog? = null
    var dialogCallback: DialogController.Callback? = null

    /**
     * Returns last activity that called [Activity.onResume], but didn't call [Activity.onPause] yet
     * @return Currently resumed activity.
     */
    var resumedActivity: WeakReference<Activity?> = WeakReference(null)

    override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (controller.isCallOrChatActive(activity)) {
            // Call and Chat screens process screen sharing requests on their own
            controller.removeMediaProjectionLaunchers(activity::class.simpleName)
            return
        }
        registerForMediaProjectionPermissionResult(activity)
        super.onActivityPreCreated(activity, savedInstanceState)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityDestroyed(activity: Activity) {
        if (activity.isFinishing) {
            controller.onActivityDestroyed()
        }
    }

    override fun onActivityResumed(activity: Activity) {
        resumedActivity = WeakReference(activity)
        controller.onActivityResumed(activity)
    }

    override fun onActivityPaused(activity: Activity) {
        controller.onActivityPaused()
        resumedActivity.clear()
    }


    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    @Suppress("RedundantNullableReturnType")
    fun registerForMediaProjectionPermissionResult(activity: Activity) {
        // Request a token that grants the app the ability to capture the display contents
        // See https://developer.android.com/guide/topics/large-screens/media-projection
        val componentActivity = activity as? ComponentActivity?
        if (componentActivity == null) {
            Logger.d(
                TAG, "Activity does not support ActivityResultRegistry APIs, " +
                        "legacy onActivityResult() will be used to acquire a media projection token"
            )
            controller.removeMediaProjectionLaunchers(activity::class.java.simpleName)
            return
        }

        val launcher: ActivityResultLauncher<Intent>? = componentActivity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            Logger.d(TAG, "Acquire a media projection token: result received")
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                Logger.d(
                    TAG,
                    "Acquire a media projection token: RESULT_OK, passing data to Glia Core SDK"
                )
                Glia.getCurrentEngagement().ifPresent { engagement ->
                    engagement.onActivityResult(
                        GliaScreenSharingRepository.SKIP_ASKING_SCREEN_SHARING_PERMISSION_RESULT_CODE,
                        result.resultCode,
                        result.data
                    )
                }
            }
        }
        activity::class.simpleName?.let {
            controller.startMediaProjectionLaunchers(it, launcher)
        }
    }

    override fun openCallActivity() {
        resumedActivity.get()?.let {
            val contextWithStyle = it.wrapWithMaterialThemeOverlay()
            val intent = CallActivity.getIntent(contextWithStyle,
                getConfigurationBuilder().setMediaType(Utils.toMediaType(GliaWidgets.MEDIA_TYPE_VIDEO))
                    .setIsUpgradeToCall(true)
                    .build())
            contextWithStyle.startActivity(intent)
        }
    }

    private fun getConfigurationBuilder(): Configuration.Builder {
        return Configuration.Builder().setWidgetsConfiguration(Dependencies.getSdkConfigurationManager().createWidgetsConfiguration())
    }

    override fun showToast(message: String, duration: Int) {
        resumedActivity.get()?.let {
            Toast.makeText(it, message, duration).show()
        }
    }

    override fun dismissAlertDialog(manualDismiss: Boolean) {
        Logger.d(TAG, "Dismiss alert dialog")
        alertDialog?.dismiss()
        alertDialog = null
    }

    override fun showAllowNotificationsDialog() {
        val activity = resumedActivity.get() ?: return
        if (alertDialog != null && alertDialog!!.isShowing) {
            return
        }
        Logger.d(TAG, "Show allow notifications dialog")
        val contextWithStyle = activity.wrapWithMaterialThemeOverlay()
        alertDialog = Dialogs.showOptionsDialog(
            context = contextWithStyle,
            theme = UiTheme.UiThemeBuilder().build(),
            title = activity.getString(R.string.glia_dialog_allow_notifications_title),
            message = activity.getString(R.string.glia_dialog_allow_notifications_message),
            positiveButtonText = activity.getString(R.string.glia_dialog_allow_notifications_yes),
            negativeButtonText = activity.getString(R.string.glia_dialog_allow_notifications_no),
            positiveButtonClickListener = {
                controller.onPositiveDialogButtonClicked()
            },
            negativeButtonClickListener = {
                controller.onNegativeDialogButtonClicked()
            },
            cancelListener = {
                controller.onNegativeDialogButtonClicked()
            }
        )
    }

    override fun openNotificationChannelScreen() {
        val activity = resumedActivity.get() ?: return
        val contextWithStyle = activity.wrapWithMaterialThemeOverlay()
        NotificationManager.openNotificationChannelScreen(contextWithStyle)
    }

    override fun showAllowScreenSharingNotificationsAndStartSharingDialog() {
        val activity = resumedActivity.get() ?: return
        if (alertDialog != null && alertDialog!!.isShowing) {
            return
        }
        Logger.d(TAG, "Show screen sharing and notifications dialog")
        val builder = UiTheme.UiThemeBuilder()
        val theme = builder.build()
        val contextWithStyle = activity.wrapWithMaterialThemeOverlay()
        alertDialog = Dialogs.showOptionsDialog(
            context = contextWithStyle,
            theme = theme,
            title = activity.getString(R.string.glia_dialog_screen_sharing_offer_enable_notifications_title),
            message = activity.getString(R.string.glia_dialog_screen_sharing_offer_enable_notifications_message),
            positiveButtonText = activity.getString(R.string.glia_dialog_screen_sharing_offer_enable_notifications_yes),
            negativeButtonText = activity.getString(R.string.glia_dialog_screen_sharing_offer_enable_notifications_no),
            positiveButtonClickListener = {
                controller.onPositiveDialogButtonClicked()
            },
            negativeButtonClickListener = {
                controller.onNegativeDialogButtonClicked()
            },
            cancelListener = {
                controller.onNegativeDialogButtonClicked()
            }
        )
    }

    override fun showOverlayPermissionsDialog() {
        val activity = resumedActivity.get() ?: return
        if (alertDialog != null && alertDialog!!.isShowing) {
            return
        }
        Logger.d(TAG, "Show overlay permissions dialog")
        alertDialog = Dialogs.showOptionsDialog(
            activity.wrapWithMaterialThemeOverlay(),
            UiTheme.UiThemeBuilder().build(),
            activity.getString(R.string.glia_dialog_overlay_permissions_title),
            activity.getString(R.string.glia_dialog_overlay_permissions_message),
            activity.getString(R.string.glia_dialog_overlay_permissions_ok),
            activity.getString(R.string.glia_dialog_overlay_permissions_no),
            {
                controller.onPositiveDialogButtonClicked()
            },
            {
                controller.onNegativeDialogButtonClicked()
            },
            {
                controller.onNegativeDialogButtonClicked()
            }
        )
    }

    override fun showScreenSharingDialog() {
        val activity = resumedActivity.get() ?: return
        if (alertDialog != null && alertDialog!!.isShowing) {
            return
        }
        activity.runOnUiThread {
            Logger.d(TAG, "Show screen sharing dialog")
            val builder = UiTheme.UiThemeBuilder()
            val theme = builder.build()
            val contextWithStyle = activity.wrapWithMaterialThemeOverlay()

            alertDialog = Dialogs.showScreenSharingDialog(
                contextWithStyle,
                theme,
                activity.getString(R.string.glia_dialog_screen_sharing_offer_title),
                activity.getString(R.string.glia_dialog_screen_sharing_offer_message),
                R.string.glia_dialog_screen_sharing_offer_accept,
                R.string.glia_dialog_screen_sharing_offer_decline,
                {
                    controller.onPositiveDialogButtonClicked(activity)
                }
            ) { controller.onNegativeDialogButtonClicked() }
        }
    }

    override fun openOverlayPermissionView() {
        val activity = resumedActivity.get() ?: return
        val overlayIntent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:" + activity.packageName)
        )
        overlayIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        activity.startActivity(overlayIntent)
    }

    override fun showUpgradeDialog(mediaUpgrade: DialogState.MediaUpgrade) {
        val activity = resumedActivity.get() ?: return
        if (alertDialog != null && alertDialog!!.isShowing) {
            return
        }
        activity.runOnUiThread {
            Logger.d(TAG, "Show upgrade dialog")
            val builder = UiTheme.UiThemeBuilder()
            val theme = builder.build()
            val contextWithStyle = activity.wrapWithMaterialThemeOverlay()

            alertDialog = Dialogs.showUpgradeDialog(contextWithStyle, theme, mediaUpgrade, {
                dialogController.dismissCurrentDialog()
                mediaUpgrade.mediaUpgradeOffer.accept { error ->
                    error?.let {
                        Logger.e(TAG, error.message, error)
                    } ?: run {
                        if (mediaUpgrade.mediaUpgradeOffer.video != null && mediaUpgrade.mediaUpgradeOffer.video != MediaDirection.NONE) {
                            controller.onPositiveDialogButtonClicked()
                        } else {
                            Logger.e(TAG, "Audio upgrade offer in call visualizer", Exception("Audio upgrade offer in call visualizer"))
                            return@accept
                        }
                    }
                }
            }) {
                controller.onNegativeDialogButtonClicked()
            }
        }
    }

    override fun setupDialogCallback() {
        dialogCallback = DialogController.Callback {
            controller.onDialogControllerCallback(it)
        }
        dialogController.addCallback(dialogCallback)
    }

    override fun removeDialogCallback() {
        dialogController.removeCallback(dialogCallback)
    }

    override fun showVisitorCodeDialog() {
        val activity = resumedActivity.get() ?: return
        if (alertDialog != null && alertDialog!!.isShowing) {
            return
        }
        Logger.d(TAG, "Show visitor code dialog")
        val builder = UiTheme.UiThemeBuilder()
        val theme = builder.build()
        val contextWithStyle = activity.wrapWithMaterialThemeOverlay()

        alertDialog = Dialogs.showVisitorCodeDialog(contextWithStyle, theme)
    }

    override fun fetchGliaOrRootView(): View? {
        return resumedActivity.get()?.let {
            return it.findViewById(R.id.call_view)
                ?: it.findViewById<FilePreviewView>(R.id.file_preview_view)
                ?: it.findViewById<ChatView>(R.id.chat_view)
                ?: it.findViewById<EndScreenSharingView>(R.id.screen_sharing_screen_view)
                ?: it.findViewById(android.R.id.content)
                ?: it.window.decorView.findViewById(android.R.id.content)
        }
    }
}
