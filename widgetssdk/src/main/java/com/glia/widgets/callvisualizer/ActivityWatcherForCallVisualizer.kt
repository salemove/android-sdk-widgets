package com.glia.widgets.callvisualizer

import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.glia.androidsdk.Glia
import com.glia.widgets.GliaWidgets
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.base.BaseActivityWatcher
import com.glia.widgets.call.CallActivity
import com.glia.widgets.call.Configuration
import com.glia.widgets.callvisualizer.CallVisualizerSupportActivity.Companion.PERMISSION_TYPE_TAG
import com.glia.widgets.core.dialog.DialogController
import com.glia.widgets.core.dialog.model.DialogState
import com.glia.widgets.core.notification.openNotificationChannelScreen
import com.glia.widgets.core.screensharing.data.GliaScreenSharingRepository
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.Utils
import com.glia.widgets.helper.getFullHybridTheme
import com.glia.widgets.helper.wrapWithMaterialThemeOverlay
import com.glia.widgets.view.Dialogs
import java.lang.ref.WeakReference

internal class ActivityWatcherForCallVisualizer(
    private val dialogController: DialogController,
    val controller: ActivityWatcherForCallVisualizerContract.Controller
) : BaseActivityWatcher(), ActivityWatcherForCallVisualizerContract.Watcher {

    init {
        topActivityObserver.subscribe(
            {activity -> resumedActivity = WeakReference(activity)},
            {error -> Logger.e(TAG, "Observable monitoring top activity FAILED", error)}
        )
        controller.setWatcher(this)
    }

    var alertDialog: AlertDialog? = null
    val dialogCallback: DialogController.Callback = DialogController.Callback {
        controller.onDialogControllerCallback(it)
    }
    var cameraPermissionLauncher: ActivityResultLauncher<String>? = null
    var overlayPermissionLauncher: ActivityResultLauncher<String>? = null

    /**
     * Returns last activity that called [Activity.onResume], but didn't call [Activity.onPause] yet
     * @return Currently resumed activity.
     */
    var resumedActivity: WeakReference<Activity?> = WeakReference(null)

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        super.onActivityCreated(activity, savedInstanceState)
        if (controller.isCallOrChatActive(activity)) {
            // Call and Chat screens process screen sharing requests on their own
            controller.removeMediaProjectionLaunchers(activity::class.simpleName)
        } else {
            registerForMediaProjectionPermissionResult(activity)
            registerForCameraPermissionResult(activity)
            registerForOverlayPermissionResult(activity)
        }
        super.onActivityCreated(activity, savedInstanceState)
    }

    override fun onActivityResumed(activity: Activity) {
        super.onActivityResumed(activity)
        controller.onActivityResumed(activity)
    }

    override fun onActivityPaused(activity: Activity) {
        super.onActivityPaused(activity)
        controller.onActivityPaused()
    }

    override fun checkInitialCameraPermission() {
        resumedActivity.get()?.run {
            controller.onInitialCameraPermissionResult(
                isGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PERMISSION_GRANTED,
                isComponentActivity = this is ComponentActivity
            )
        }
    }

    override fun requestCameraPermission() {
        if (resumedActivity.get() is ComponentActivity) {
            cameraPermissionLauncher?.run { this.launch(Manifest.permission.CAMERA) }
        }
    }

    override fun requestOverlayPermission() {
        if (resumedActivity.get() is ComponentActivity) {
            overlayPermissionLauncher?.run { this.launch(Settings.ACTION_MANAGE_OVERLAY_PERMISSION) }
        }
    }

    override fun openOverlayPermissionView() {
        val activity = resumedActivity.get() ?: return
        val overlayIntent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${activity.packageName}")
        )
        overlayIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        activity.startActivity(overlayIntent)
    }

    private fun registerForCameraPermissionResult(activity: Activity) {
        (activity as? ComponentActivity?)?.let { componentActivity ->
            cameraPermissionLauncher = componentActivity.registerForActivityResult(RequestPermission()) {
                isGranted: Boolean ->
                controller.onRequestedCameraPermissionResult(isGranted)
            }
        }
    }

    override fun callOverlayDialog() {
        dialogController.showOverlayPermissionsDialog()
    }

    private fun registerForOverlayPermissionResult(activity: Activity) {
        (activity as? ComponentActivity?)?.let { componentActivity ->
            overlayPermissionLauncher = componentActivity.registerForActivityResult(RequestPermission()) {
                    isGranted: Boolean ->
                controller.onMediaProjectionPermissionResult(isGranted, componentActivity)
            }
        }
    }

    private fun registerForMediaProjectionPermissionResult(activity: Activity) {
        // Request a token that grants the app the ability to capture the display contents
        // See https://developer.android.com/guide/topics/large-screens/media-projection
        val componentActivity = activity as? ComponentActivity?
        if (componentActivity == null) {
            Logger.d(
                TAG, "Activity does not support ActivityResultRegistry APIs, " +
                        "legacy onActivityResult() should be used to acquire a media projection token"
            )
            controller.removeMediaProjectionLaunchers(activity::class.java.simpleName)
            return
        }

        val launcher: ActivityResultLauncher<Intent> = componentActivity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            Logger.d(TAG, "Acquire a media projection token: result received")
            if (result.resultCode == RESULT_OK && result.data != null) {
                Logger.d(TAG, "Acquire a media projection token: RESULT_OK, passing data to Glia Core SDK")
                Glia.getCurrentEngagement().ifPresent { engagement ->
                    engagement.onActivityResult( // Requires MediaProjectionService running already
                        GliaScreenSharingRepository.SKIP_ASKING_SCREEN_SHARING_PERMISSION_RESULT_CODE,
                        result.resultCode,
                        result.data
                    )
                }
            } else if (result.resultCode == RESULT_CANCELED) {
                Logger.d(TAG, "Acquire a media projection token: RESULT_CANCELED")
                // Visitor rejected system permission required for screen sharing
                controller.onMediaProjectionRejected()
            }
            destroySupportActivityIfExists(componentActivity)
        }
        activity::class.simpleName?.let {
            controller.startMediaProjectionLaunchers(it, launcher)
        }
    }

    override fun openCallActivity() {
        resumedActivity.get()?.let {
            val intent = CallActivity.getIntent(
                it,
                getConfigurationBuilder().setMediaType(Utils.toMediaType(GliaWidgets.MEDIA_TYPE_VIDEO))
                    .setIsUpgradeToCall(true)
                    .build()
            )
            it.startActivity(intent)
            if (it is EndScreenSharingActivity) {
                it.finish()
            }
        }
    }

    private fun getConfigurationBuilder(): Configuration.Builder {
        val configuration = Dependencies.getSdkConfigurationManager().createWidgetsConfiguration()
        return Configuration.Builder().setWidgetsConfiguration(configuration)
    }

    override fun showToast(message: String, duration: Int) {
        resumedActivity.get()?.let {
            Toast.makeText(it, message, duration).show()
        }
    }

    override fun dismissAlertDialog() {
        Logger.d(TAG, "Dismiss alert dialog")
        alertDialog?.dismiss()
        alertDialog = null
    }

    override fun removeDialogFromStack() {
        dialogController.dismissCurrentDialog()
    }

    override fun dismissOverlayDialog() {
        dialogController.dismissOverlayPermissionsDialog()
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
        resumedActivity.get()?.openNotificationChannelScreen()
    }

    override fun showAllowScreenSharingNotificationsAndStartSharingDialog() {
        val activity = resumedActivity.get() ?: return
        if (alertDialog != null && alertDialog!!.isShowing) {
            return
        }
        Logger.d(TAG, "Show screen sharing and notifications dialog")
        val theme = UiTheme.UiThemeBuilder().build()
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
        activity.runOnUiThread {
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
    }

    override fun showScreenSharingDialog() {
        val activity = resumedActivity.get() ?: return
        if (alertDialog != null && alertDialog!!.isShowing) {
            return
        }
        activity.runOnUiThread {
            Logger.d(TAG, "Show screen sharing dialog")
            val theme = getRuntimeTheme(activity)
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

    override fun showUpgradeDialog(mediaUpgrade: DialogState.MediaUpgrade) {
        val activity = resumedActivity.get() ?: return
        if (alertDialog != null && alertDialog!!.isShowing) {
            return
        }
        activity.runOnUiThread {
            Logger.d(TAG, "Show upgrade dialog")
            val theme = getRuntimeTheme(activity)
            val contextWithStyle = activity.wrapWithMaterialThemeOverlay()

            alertDialog = Dialogs.showUpgradeDialog(contextWithStyle, theme, mediaUpgrade, {
                dialogController.dismissCurrentDialog()
                controller.onMediaUpgradeReceived(mediaUpgrade.mediaUpgradeOffer)
            }) {
                controller.onNegativeDialogButtonClicked()
            }
        }
    }

    override fun setupDialogCallback() {
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
        val theme = UiTheme.UiThemeBuilder().build()
        val contextWithStyle = activity.wrapWithMaterialThemeOverlay()

        alertDialog = Dialogs.showVisitorCodeDialog(contextWithStyle, theme)
    }

    private fun getRuntimeTheme(activity: Activity) : UiTheme {
        val themeFromIntent: UiTheme? = activity.intent?.getParcelableExtra(GliaWidgets.UI_THEME)
        val themeFromGlobalSetting = Dependencies.getSdkConfigurationManager().uiTheme
        return themeFromGlobalSetting.getFullHybridTheme(themeFromIntent)
    }

    override fun openSupportActivity(permissionType: PermissionType) {
        resumedActivity.get()?.run {
            val intent = Intent(this, CallVisualizerSupportActivity::class.java)
            intent.putExtra(PERMISSION_TYPE_TAG, permissionType)
            startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
        }
    }

    override fun destroySupportActivityIfExists() {
        resumedActivity.get()?.let { destroySupportActivityIfExists(it) }
    }

    private fun destroySupportActivityIfExists(activity: Activity) {
        if (activity is CallVisualizerSupportActivity) {
            activity.finish()
        }
    }
}
