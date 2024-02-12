package com.glia.widgets.callvisualizer

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Context
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
import com.glia.widgets.GliaWidgets
import com.glia.widgets.UiTheme
import com.glia.widgets.base.BaseActivityWatcher
import com.glia.widgets.call.CallActivity
import com.glia.widgets.call.Configuration
import com.glia.widgets.callvisualizer.CallVisualizerSupportActivity.Companion.PERMISSION_TYPE_TAG
import com.glia.widgets.core.dialog.DialogContract
import com.glia.widgets.core.dialog.model.DialogState
import com.glia.widgets.core.notification.openNotificationChannelScreen
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.Utils
import com.glia.widgets.helper.WeakReferenceDelegate
import com.glia.widgets.helper.withRuntimeTheme
import com.glia.widgets.view.Dialogs
import com.glia.widgets.webbrowser.WebBrowserActivity

@SuppressLint("CheckResult")
internal class ActivityWatcherForCallVisualizer(
    private val dialogController: DialogContract.Controller,
    val controller: ActivityWatcherForCallVisualizerContract.Controller
) : BaseActivityWatcher(), ActivityWatcherForCallVisualizerContract.Watcher {

    init {
        topActivityObserver.subscribe(
            { resumedActivity = it },
            { error -> Logger.e(TAG, "Observable monitoring top activity FAILED", error) }
        )
        controller.setWatcher(this)
    }

    private var alertDialog: AlertDialog? = null
    private val dialogCallback: DialogContract.Controller.Callback = DialogContract.Controller.Callback {
        controller.onDialogControllerCallback(it)
    }
    private var cameraPermissionLauncher: ActivityResultLauncher<String>? = null
    private var overlayPermissionLauncher: ActivityResultLauncher<String>? = null

    /**
     * Returns last activity that called [Activity.onResume], but didn't call [Activity.onPause] yet
     * @return Currently resumed activity.
     */
    private var resumedActivity: Activity? by WeakReferenceDelegate()

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
        resumedActivity?.run {
            controller.onInitialCameraPermissionResult(
                isGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PERMISSION_GRANTED,
                isComponentActivity = this is ComponentActivity
            )
        }
    }

    override fun requestCameraPermission() {
        if (resumedActivity is ComponentActivity) {
            cameraPermissionLauncher?.run {
                controller.setIsWaitingMediaProjectionResult(true)
                this.launch(Manifest.permission.CAMERA)
            }
        }
    }

    override fun requestOverlayPermission() {
        if (resumedActivity is ComponentActivity) {
            overlayPermissionLauncher?.run {
                controller.setIsWaitingMediaProjectionResult(true)
                this.launch(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            }
        }
    }

    override fun openOverlayPermissionView() {
        val activity = resumedActivity ?: return
        val overlayIntent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${activity.packageName}")
        )
        overlayIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        activity.startActivity(overlayIntent)
    }

    private fun registerForCameraPermissionResult(activity: Activity) {
        (activity as? ComponentActivity?)?.let { componentActivity ->
            cameraPermissionLauncher = componentActivity.registerForActivityResult(RequestPermission()) { isGranted: Boolean ->
                controller.setIsWaitingMediaProjectionResult(false)
                controller.onRequestedCameraPermissionResult(isGranted)
            }
        }
    }

    override fun callOverlayDialog() {
        dialogController.showOverlayPermissionsDialog()
    }

    private fun registerForOverlayPermissionResult(activity: Activity) {
        (activity as? ComponentActivity?)?.let { componentActivity ->
            overlayPermissionLauncher = componentActivity.registerForActivityResult(RequestPermission()) { isGranted: Boolean ->
                controller.setIsWaitingMediaProjectionResult(false)
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
                TAG,
                "Activity does not support ActivityResultRegistry APIs, " +
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
                controller.mediaProjectionOnActivityResultSkipPermissionRequest(result.resultCode, result.data) // Requires MediaProjectionService running already
            } else if (result.resultCode == RESULT_CANCELED) {
                Logger.d(TAG, "Acquire a media projection token: RESULT_CANCELED")
                // Visitor rejected system permission required for screen sharing
                controller.onMediaProjectionRejected()
            }
            controller.setIsWaitingMediaProjectionResult(false)
            destroySupportActivityIfExists(componentActivity)
        }
        activity::class.simpleName?.let {
            controller.startMediaProjectionLaunchers(it, launcher)
        }
    }

    override fun openCallActivity() {
        resumedActivity?.let {
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

    override fun openWebBrowserActivity(title: String, url: String) {
        resumedActivity?.let {
            val intent = WebBrowserActivity.intent(it, title, url)
            it.startActivity(intent)
        }
    }

    private fun getConfigurationBuilder(): Configuration.Builder {
        val configuration = Dependencies.getSdkConfigurationManager().createWidgetsConfiguration()
        return Configuration.Builder().setWidgetsConfiguration(configuration)
    }

    override fun showToast(message: String, duration: Int) {
        resumedActivity?.let {
            Toast.makeText(it, message, duration).show()
        }
    }

    override fun dismissAlertDialog() {
        Logger.d(TAG, "Dismiss alert dialog")
        dismissAlertDialogSilently()
        destroySupportActivityIfExists()
    }

    private fun dismissAlertDialogSilently() {
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
        showAlertDialogOnUiThreadWithStyledContext("Show allow notifications dialog") { context, uiTheme, _ ->
            Dialogs.showAllowNotificationsDialog(
                context = context,
                uiTheme = uiTheme,
                positiveButtonClickListener = {
                    controller.onPositiveDialogButtonClicked()
                },
                negativeButtonClickListener = {
                    controller.onNegativeDialogButtonClicked()
                }
            )
        }
    }

    override fun openNotificationChannelScreen() {
        resumedActivity?.openNotificationChannelScreen()
    }

    override fun showAllowScreenSharingNotificationsAndStartSharingDialog() {
        showAlertDialogOnUiThreadWithStyledContext(
            "Show screen sharing and notifications dialog"
        ) { context, uiTheme, _ ->
            Dialogs.showAllowScreenSharingNotificationsAndStartSharingDialog(
                context = context,
                uiTheme = uiTheme,
                positiveButtonClickListener = {
                    controller.onPositiveDialogButtonClicked()
                },
                negativeButtonClickListener = {
                    controller.onNegativeDialogButtonClicked()
                }
            )
        }
    }

    override fun showOverlayPermissionsDialog() {
        showAlertDialogOnUiThreadWithStyledContext("Show overlay permissions dialog") { context, uiTheme, _ ->
            Dialogs.showOverlayPermissionsDialog(
                context = context,
                uiTheme = uiTheme,
                positiveButtonClickListener = {
                    controller.onPositiveDialogButtonClicked()
                },
                negativeButtonClickListener = {
                    controller.onNegativeDialogButtonClicked()
                }
            )
        }
    }

    override fun showScreenSharingDialog(operatorName: String?) {
        showAlertDialogOnUiThreadWithStyledContext("Show screen sharing dialog") { context, uiTheme, activity ->
            Dialogs.showScreenSharingDialog(
                context = context,
                theme = uiTheme,
                operatorName = operatorName,
                positiveButtonClickListener = {
                    controller.onPositiveDialogButtonClicked(activity)
                },
                negativeButtonClickListener = {
                    controller.onNegativeDialogButtonClicked()
                }
            )
        }
    }

    override fun showUpgradeDialog(mediaUpgradeState: DialogState.MediaUpgrade) {
        showAlertDialogOnUiThreadWithStyledContext("Show upgrade dialog") { context, uiTheme, _ ->
            Dialogs.showUpgradeDialog(context, uiTheme, mediaUpgradeState, {
                controller.onMediaUpgradeReceived(mediaUpgradeState.mediaUpgradeOffer)
                dialogController.dismissCurrentDialog()
            }) {
                controller.onNegativeDialogButtonClicked()
            }
        }
    }

    private fun showAlertDialogOnUiThreadWithStyledContext(
        logMessage: String? = null,
        callback: (Context, UiTheme, Activity) -> AlertDialog
    ) {
        resumedActivity?.apply {
            runOnUiThread {
                logMessage?.let { Logger.d(TAG, it) }
                dismissAlertDialogSilently()
                withRuntimeTheme { themedContext, uiTheme ->
                    alertDialog = callback(themedContext, uiTheme, this)
                }
            }
        }
    }

    override fun showEngagementConfirmationDialog() {
        showAlertDialogOnUiThreadWithStyledContext("Show live observation opt in dialog") { context, uiTheme, _ ->
            Dialogs.showEngagementConfirmationDialog(
                context = context,
                theme = uiTheme,
                links = controller.getConfirmationDialogLinks(),
                positiveButtonClickListener = { controller.onPositiveDialogButtonClicked() },
                negativeButtonClickListener = { controller.onNegativeDialogButtonClicked() },
                linkClickListener = { controller.onLinkClicked(it) }
            )
        }
    }

    override fun engagementStarted() {
        controller.addScreenSharingCallback(resumedActivity ?: return)
    }

    override fun setupDialogCallback() {
        dialogController.addCallback(dialogCallback)
    }

    override fun removeDialogCallback() {
        dialogController.removeCallback(dialogCallback)
    }

    override fun showVisitorCodeDialog() {
        showAlertDialogOnUiThreadWithStyledContext("Show visitor code dialog") { context, _, _ ->
            Dialogs.showVisitorCodeDialog(context)
        }
    }

    override fun isSupportActivityOpen(): Boolean {
        val activity = resumedActivity
        return (activity == null || activity is CallVisualizerSupportActivity)
    }

    override fun isWebBrowserActivityOpen(): Boolean {
        val activity = resumedActivity
        return (activity == null || activity is WebBrowserActivity)
    }

    override fun openSupportActivity(permissionType: PermissionType) {
        resumedActivity?.run {
            val intent = Intent(this, CallVisualizerSupportActivity::class.java)
            intent.putExtra(PERMISSION_TYPE_TAG, permissionType)
            startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
        }
    }

    override fun destroySupportActivityIfExists() {
        resumedActivity?.let {
            if (!it.isFinishing && !controller.isWaitingMediaProjectionResult()) destroySupportActivityIfExists(it)
        }
    }

    private fun destroySupportActivityIfExists(activity: Activity) {
        if (activity is CallVisualizerSupportActivity) {
            activity.finish()
            activity.overridePendingTransition(0, 0)
        }
    }
}
