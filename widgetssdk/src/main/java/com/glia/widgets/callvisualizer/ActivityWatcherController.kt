package com.glia.widgets.callvisualizer

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import com.glia.androidsdk.Glia
import com.glia.androidsdk.GliaException
import com.glia.widgets.callvisualizer.controller.CallVisualizerController
import com.glia.widgets.core.dialog.Dialog.*
import com.glia.widgets.core.dialog.model.DialogState
import com.glia.widgets.core.dialog.model.DialogState.MediaUpgrade
import com.glia.widgets.core.screensharing.ScreenSharingController
import com.glia.widgets.helper.Logger
import com.glia.widgets.view.head.controller.ServiceChatHeadController
import com.glia.widgets.view.unifiedui.extensions.wrapWithMaterialThemeOverlay

internal class ActivityWatcherController(
    private val callVisualizerController: CallVisualizerController,
    private val screenSharingController: ScreenSharingController,
    private var serviceChatHeadController: ServiceChatHeadController
) : ActivityWatcherContract.Controller {

    companion object {
        private val TAG = ActivityWatcherController::class.java.simpleName
    }

    @Mode
    private var currentDialogMode: Int = MODE_NONE

    private lateinit var watcher: ActivityWatcherContract.Watcher

    override fun setWatcher(watcher: ActivityWatcherContract.Watcher) {
        this.watcher = watcher
    }

    internal var screenSharingViewCallback: ScreenSharingController.ViewCallback? = null
    internal val startMediaProjectionLaunchers = mutableMapOf<String, ActivityResultLauncher<Intent>?>()

    override fun onActivityResumed(activity: Activity) {
        Logger.d(TAG, "onActivityResumed(root)")
        addDialogCallback(activity)
        addScreenSharingCallback(activity)
        serviceChatHeadController.onResume(watcher.fetchGliaOrRootView())
    }

    override fun onActivityPaused() {
        Logger.d(TAG, "onActivityPaused()")
        watcher.dismissAlertDialog(false)
        watcher.removeDialogCallback()
        removeScreenSharingCallback()
        serviceChatHeadController.onPause(watcher.fetchGliaOrRootView())
    }

    override fun onActivityDestroyed() {
        Logger.d(TAG, "onActivityDestroyed()")
        serviceChatHeadController.onDestroy()
    }

    override fun isCallOrChatActive(activity: Activity): Boolean {
        return callVisualizerController.isCallOrChatScreenActiveUseCase(activity)
    }

    override fun removeMediaProjectionLaunchers(activityName: String?) {
        startMediaProjectionLaunchers.remove(activityName)
    }

    override fun startMediaProjectionLaunchers(
        activityName: String,
        launcher: ActivityResultLauncher<Intent>?
    ) {
        startMediaProjectionLaunchers[activityName] = launcher
    }

    private fun addDialogCallback(activity: Activity) {
        // There are separate dialog callbacks for incoming media requests on Call and Chat screens.
        if (callVisualizerController.isCallOrChatScreenActiveUseCase(activity)) return

        watcher.setupDialogCallback()
    }

    override fun onDialogControllerCallback(state: DialogState) {
        // This ensures that dialog state is set to MODE_NONE only by dismissing the dialog properly
        if (state.mode != MODE_NONE) {
            currentDialogMode = state.mode
        }
        when (state.mode) {
            MODE_NONE -> watcher.dismissAlertDialog(true)
            MODE_MEDIA_UPGRADE -> watcher.showUpgradeDialog(state as MediaUpgrade)
            MODE_OVERLAY_PERMISSION -> watcher.showOverlayPermissionsDialog()
            MODE_START_SCREEN_SHARING -> watcher.showScreenSharingDialog()
            MODE_ENABLE_NOTIFICATION_CHANNEL -> watcher.showAllowNotificationsDialog()
            MODE_ENABLE_SCREEN_SHARING_NOTIFICATIONS_AND_START_SHARING -> watcher.showAllowScreenSharingNotificationsAndStartSharingDialog()
            MODE_VISITOR_CODE -> watcher.showVisitorCodeDialog()
            else -> {
                Logger.d(TAG, "Unexpected dialog mode received")
            }
        }
    }

    override fun onPositiveDialogButtonClicked(activity: Activity?) {
        Logger.d(TAG, "onPositiveButtonDialogButtonClicked() - $currentDialogMode")
        watcher.dismissAlertDialog(true)
        when (currentDialogMode) {
            MODE_NONE -> Logger.e(TAG, "$currentDialogMode should not have a dialog to click")
            MODE_ENABLE_SCREEN_SHARING_NOTIFICATIONS_AND_START_SHARING -> watcher.openNotificationChannelScreen()
            MODE_START_SCREEN_SHARING -> activity?.run { startScreenSharing(this) }
            MODE_MEDIA_UPGRADE -> watcher.openCallActivity()
            MODE_OVERLAY_PERMISSION -> watcher.openOverlayPermissionView()
            MODE_ENABLE_NOTIFICATION_CHANNEL -> watcher.openNotificationChannelScreen()
            else -> Logger.d(TAG, "Not relevant")
        }
        currentDialogMode = MODE_NONE
    }

    override fun onNegativeDialogButtonClicked() {
        Logger.d(TAG, "onNegativeButtonDialogButtonClicked() - $currentDialogMode")
        watcher.dismissAlertDialog(true)
        when (currentDialogMode) {
            MODE_NONE -> Logger.e(TAG, "$currentDialogMode should not have a dialog to click")
            MODE_ENABLE_SCREEN_SHARING_NOTIFICATIONS_AND_START_SHARING,
            MODE_START_SCREEN_SHARING -> screenSharingController.onScreenSharingDeclined()
            MODE_MEDIA_UPGRADE,
            MODE_OVERLAY_PERMISSION,
            MODE_VISITOR_CODE,
            MODE_ENABLE_NOTIFICATION_CHANNEL -> Logger.d(TAG, "$currentDialogMode no operation")
        }
        currentDialogMode = MODE_NONE
    }

    private fun addScreenSharingCallback(activity: Activity) {
        // Call and Chat screens process screen sharing requests on their own.
        if (callVisualizerController.isCallOrChatScreenActiveUseCase(activity)) return

        setupScreenSharingViewCallback()
        screenSharingController.setViewCallback(screenSharingViewCallback)
        screenSharingController.onResume(activity)
    }

    private fun removeScreenSharingCallback() {
        screenSharingController.removeViewCallback(screenSharingViewCallback)
    }

    private fun startScreenSharing(activity: Activity) {
        val contextWithStyle = activity.wrapWithMaterialThemeOverlay()
        if (activity is ComponentActivity) {
            acquireMediaProjectionToken(activity)
            screenSharingController.onScreenSharingAcceptedAndPermissionAsked(
                contextWithStyle
            )
        } else {
            screenSharingController.onScreenSharingAccepted(contextWithStyle)
        }
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

    private fun setupScreenSharingViewCallback() {
        screenSharingViewCallback = object : ScreenSharingController.ViewCallback {
            override fun onScreenSharingRequestError(exception: GliaException?) {
                exception?.run { watcher.showToast(exception.debugMessage) }
            }

            override fun onScreenSharingStarted() {
                if (Glia.isInitialized()) {
                    serviceChatHeadController.init()
                }
                serviceChatHeadController.onResume(watcher.fetchGliaOrRootView())
            }

        }
    }
}
