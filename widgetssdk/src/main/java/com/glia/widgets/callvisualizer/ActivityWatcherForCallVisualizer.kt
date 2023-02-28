package com.glia.widgets.callvisualizer

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.Application
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.media.projection.MediaProjectionManager
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
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.comms.MediaDirection
import com.glia.widgets.GliaWidgets
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.call.CallActivity
import com.glia.widgets.call.Configuration
import com.glia.widgets.callvisualizer.controller.CallVisualizerController
import com.glia.widgets.chat.ChatView
import com.glia.widgets.core.dialog.Dialog
import com.glia.widgets.core.dialog.DialogController
import com.glia.widgets.core.dialog.model.DialogState
import com.glia.widgets.core.notification.device.NotificationManager
import com.glia.widgets.core.screensharing.ScreenSharingController
import com.glia.widgets.core.screensharing.data.GliaScreenSharingRepository.SKIP_ASKING_SCREEN_SHARING_PERMISSION_RESULT_CODE
import com.glia.widgets.di.Dependencies
import com.glia.widgets.filepreview.ui.FilePreviewView
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.Utils
import com.glia.widgets.view.Dialogs
import com.glia.widgets.view.head.controller.ServiceChatHeadController
import com.google.android.material.theme.overlay.MaterialThemeOverlay
import java.lang.ref.WeakReference

internal class ActivityWatcherForCallVisualizer(
    private val callVisualizerController: CallVisualizerController,
    private val screenSharingController: ScreenSharingController,
    private val dialogController: DialogController,
    private var serviceChatHeadController: ServiceChatHeadController,
) : Application.ActivityLifecycleCallbacks {

    companion object {
        private val TAG = ActivityWatcherForCallVisualizer::class.java.simpleName
    }

    private val isGliaActivity: Boolean
        get() = callVisualizerController.isGliaActivity(resumedActivity)

    @VisibleForTesting
    var dialogCallback: DialogController.Callback? = null
    private var screenSharingViewCallback: ScreenSharingController.ViewCallback? = null

    @VisibleForTesting
    val startMediaProjectionLaunchers = mutableMapOf<String, ActivityResultLauncher<Intent>?>()

    @VisibleForTesting
    var alertDialog: AlertDialog? = null

    /**
     * Returns last activity that called [Activity.onResume], but didn't call [Activity.onPause] yet
     * @return Currently resumed activity.
     */
    private var _resumedActivity: WeakReference<Activity> = WeakReference(null)
    val resumedActivity: Activity? get() = _resumedActivity.get()

    override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (callVisualizerController.isGliaActivity(activity)) {
            // Call and Chat screens process screen sharing requests on their own
            startMediaProjectionLaunchers.remove(activity::class.simpleName)
            return
        }
        registerForMediaProjectionPermissionResult(activity)
        super.onActivityPreCreated(activity, savedInstanceState)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityDestroyed(activity: Activity) {
        if (activity.isFinishing) serviceChatHeadController.onDestroy()
    }

    override fun onActivityResumed(activity: Activity) {
        _resumedActivity = WeakReference(activity)
        addDialogCallback()
        addScreenSharingCallback()
        val gliaOrRootView: View? = getGliaViewOrRootView(activity)
        serviceChatHeadController.onResume(gliaOrRootView)
    }

    @VisibleForTesting
    fun getGliaViewOrRootView(activity: Activity): View? {
        return activity.findViewById(R.id.call_view)
            ?: activity.findViewById<FilePreviewView>(R.id.file_preview_view)
            ?: activity.findViewById<ChatView>(R.id.chat_view)
            ?: activity.findViewById<EndScreenSharingView>(R.id.screen_sharing_screen_view)
            ?: activity.findViewById(android.R.id.content)
            ?: activity.window.decorView.findViewById(android.R.id.content)
    }

    override fun onActivityPaused(activity: Activity) {
        _resumedActivity.clear()
        removeDialogCallback()
        removeScreenSharingCallback()
        val gliaOrRootView: View? = getGliaViewOrRootView(activity)
        serviceChatHeadController.onPause(gliaOrRootView)
    }


    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    @Suppress("RedundantNullableReturnType")
    @VisibleForTesting
    fun registerForMediaProjectionPermissionResult(activity: Activity) {
        // Request a token that grants the app the ability to capture the display contents
        // See https://developer.android.com/guide/topics/large-screens/media-projection
        val componentActivity = activity as? ComponentActivity
        if (componentActivity == null) {
            Logger.d(
                TAG, "Activity does not support ActivityResultRegistry APIs, " +
                        "legacy onActivityResult() will be used to acquire a media projection token"
            )
            startMediaProjectionLaunchers.remove(activity::class.simpleName)
            return
        }

        val launcher : ActivityResultLauncher<Intent>? = componentActivity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            Logger.d(TAG, "Acquire a media projection token: result received")
            if (result.resultCode == RESULT_OK && result.data != null) {
                Logger.d(
                    TAG,
                    "Acquire a media projection token: RESULT_OK, passing data to Glia Core SDK"
                )
                Glia.getCurrentEngagement().ifPresent { engagement ->
                    engagement.onActivityResult(
                        SKIP_ASKING_SCREEN_SHARING_PERMISSION_RESULT_CODE,
                        result.resultCode,
                        result.data
                    )
                }
            }
        }
        activity::class.simpleName?.let { startMediaProjectionLaunchers.put(it, launcher) }
    }

    private fun addDialogCallback() {
        // There are separate dialog callbacks for incoming media requests on Call and Chat screens.
        if (isGliaActivity) return

        setupDialogCallback()
        dialogController.addCallback(dialogCallback)
    }

    private fun removeDialogCallback() {
        dialogController.removeCallback(dialogCallback)
    }

    private fun addScreenSharingCallback() {
        // Call and Chat screens process screen sharing requests on their own.
        if (isGliaActivity) return
        val activity = resumedActivity ?: return

        setupScreenSharingViewCallback()
        screenSharingController.setViewCallback(screenSharingViewCallback)
        screenSharingController.onResume(activity)
    }

    private fun removeScreenSharingCallback() {
        screenSharingController.removeViewCallback(screenSharingViewCallback)
    }

    private fun setupScreenSharingViewCallback() {
        resumedActivity?.let {
            screenSharingViewCallback = object : ScreenSharingController.ViewCallback {
                override fun onScreenSharingRequestError(exception: GliaException?) {
                    exception?.run { showToast(it, exception.debugMessage) }
                }

                override fun onScreenSharingStarted() {
                    if (Glia.isInitialized()) {
                        serviceChatHeadController.init()
                    }
                    val gliaOrRootView: View? = getGliaViewOrRootView(it)
                    serviceChatHeadController.onResume(gliaOrRootView)
                }
            }
        }
    }

    @VisibleForTesting
    fun setupDialogCallback() {
        val activity = resumedActivity ?: return

        dialogCallback = DialogController.Callback {
            when (it.mode) {
                Dialog.MODE_NONE -> dismissAlertDialog()
                Dialog.MODE_MEDIA_UPGRADE -> activity.runOnUiThread {
                    showUpgradeDialog(it as DialogState.MediaUpgrade)
                }
                Dialog.MODE_OVERLAY_PERMISSION -> activity.runOnUiThread {
                    showOverlayPermissionsDialog()
                }
                Dialog.MODE_START_SCREEN_SHARING -> activity.runOnUiThread {
                    showScreenSharingDialog()
                }
                Dialog.MODE_ENABLE_NOTIFICATION_CHANNEL -> activity.runOnUiThread {
                    showAllowNotificationsDialog()
                }
                Dialog.MODE_ENABLE_SCREEN_SHARING_NOTIFICATIONS_AND_START_SHARING ->
                    activity.runOnUiThread {
                        showAllowScreenSharingNotificationsAndStartSharingDialog()
                    }
                Dialog.MODE_VISITOR_CODE ->
                    activity.runOnUiThread {
                        showVisitorCodeDialog(resumedActivity)
                    }
                else -> {
                    Logger.d(TAG, "Unexpected dialog mode received")
                }
            }
        }
    }

    private fun showOverlayPermissionsDialog() {
        val activity = resumedActivity ?: return
        if (alertDialog != null && alertDialog!!.isShowing) {
            return
        }

        Logger.d(TAG, "Show overlay permissions dialog")
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

    private fun showAllowNotificationsDialog() {
        val activity = resumedActivity ?: return
        if (alertDialog != null && alertDialog!!.isShowing) {
            return
        }

        Logger.d(TAG, "Show allow notifications dialog")
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

    private fun showAllowScreenSharingNotificationsAndStartSharingDialog() {
        val activity = resumedActivity ?: return
        if (alertDialog != null && alertDialog!!.isShowing) {
            return
        }

        Logger.d(TAG, "Show screen sharing and notifications dialog")
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

    private fun showVisitorCodeDialog(resumedActivity: WeakReference<Activity?>) {
        val activity = resumedActivity.get() ?: return
        if (alertDialog != null && alertDialog!!.isShowing) {
            return
        }

        Logger.d(TAG, "Show visitor code dialog")
        val builder = UiTheme.UiThemeBuilder()
        val theme = builder.build()
        val contextWithStyle = prepareContextWithStyle(activity)

        alertDialog = Dialogs.showVisitorCodeDialog(contextWithStyle, theme)
    }

    private fun showScreenSharingDialog() {
        val activity = resumedActivity ?: return
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
            activity.getString(R.string.glia_dialog_screen_sharing_offer_title),
            activity.getString(R.string.glia_dialog_screen_sharing_offer_message),
            R.string.glia_dialog_screen_sharing_offer_accept,
            R.string.glia_dialog_screen_sharing_offer_decline,
            {
                if (activity is ComponentActivity) {
                    acquireMediaProjectionToken(activity)
                    screenSharingController.onScreenSharingAcceptedAndPermissionAsked(contextWithStyle)
                } else {
                    screenSharingController.onScreenSharingAccepted(contextWithStyle)
                }
            }
        ) { screenSharingController.onScreenSharingDeclined() }
    }

    private fun acquireMediaProjectionToken(activity: Activity) {
        startMediaProjectionLaunchers[activity::class.simpleName].let { startMediaProjection ->
            activity.getSystemService(MediaProjectionManager::class.java)
                ?.let { mediaProjectionManager ->
                    startMediaProjection?.launch(mediaProjectionManager.createScreenCaptureIntent())
                    Logger.d(
                        TAG,
                        "Acquire a media projection token: launching permission request"
                    )
                }
        }
    }

    private fun showUpgradeDialog(mediaUpgrade: DialogState.MediaUpgrade) {
        val activity = resumedActivity ?: return
        if (alertDialog != null && alertDialog!!.isShowing) {
            return
        }

        Logger.d(TAG, "Show upgrade dialog")
        val builder = UiTheme.UiThemeBuilder()
        val theme = builder.build()
        val contextWithStyle = prepareContextWithStyle(activity)

        alertDialog = Dialogs.showUpgradeDialog(contextWithStyle, theme, mediaUpgrade, {
            dialogController.dismissCurrentDialog()
            mediaUpgrade.mediaUpgradeOffer.accept { error ->
                error?.let {
                    Logger.e(TAG, error.message, error)
                } ?: run {
                    if (mediaUpgrade.mediaUpgradeOffer.video != null && mediaUpgrade.mediaUpgradeOffer.video != MediaDirection.NONE) {
                        openCallActivity(contextWithStyle)
                    } else {
                        Logger.e(TAG, "Audio upgrade offer in call visualizer", Exception("Audio upgrade offer in call visualizer"))
                        return@accept
                    }
                }
            }
        }) {
            dialogController.dismissCurrentDialog()
        }
    }

    private fun openCallActivity(contextWithStyle: Context) {
        val intent = CallActivity.getIntent(contextWithStyle,
            getConfigurationBuilder().setMediaType(Utils.toMediaType(GliaWidgets.MEDIA_TYPE_VIDEO))
                .setIsUpgradeToCall(true)
                .build())
        contextWithStyle.startActivity(intent)
    }

    private fun getConfigurationBuilder(): Configuration.Builder {
        return Configuration.Builder().setWidgetsConfiguration(Dependencies.getSdkConfigurationManager().createWidgetsConfiguration())
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
