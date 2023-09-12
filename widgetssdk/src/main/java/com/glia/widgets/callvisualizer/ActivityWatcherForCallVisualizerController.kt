package com.glia.widgets.callvisualizer

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.VisibleForTesting
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.comms.MediaDirection
import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.widgets.callvisualizer.CallVisualizerSupportActivity.Companion.PERMISSION_TYPE_TAG
import com.glia.widgets.callvisualizer.controller.CallVisualizerController
import com.glia.widgets.core.dialog.Dialog.MODE_ENABLE_NOTIFICATION_CHANNEL
import com.glia.widgets.core.dialog.Dialog.MODE_ENABLE_SCREEN_SHARING_NOTIFICATIONS_AND_START_SHARING
import com.glia.widgets.core.dialog.Dialog.MODE_MEDIA_UPGRADE
import com.glia.widgets.core.dialog.Dialog.MODE_NONE
import com.glia.widgets.core.dialog.Dialog.MODE_OVERLAY_PERMISSION
import com.glia.widgets.core.dialog.Dialog.MODE_START_SCREEN_SHARING
import com.glia.widgets.core.dialog.Dialog.MODE_VISITOR_CODE
import com.glia.widgets.core.dialog.Dialog.Mode
import com.glia.widgets.core.dialog.domain.IsShowOverlayPermissionRequestDialogUseCase
import com.glia.widgets.core.dialog.model.DialogState
import com.glia.widgets.core.dialog.model.DialogState.MediaUpgrade
import com.glia.widgets.core.screensharing.ScreenSharingController
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG

internal class ActivityWatcherForCallVisualizerController(
    private val callVisualizerController: CallVisualizerController,
    private val screenSharingController: ScreenSharingController,
    private val isShowOverlayPermissionRequestDialogUseCase: IsShowOverlayPermissionRequestDialogUseCase
) : ActivityWatcherForCallVisualizerContract.Controller {

    @VisibleForTesting
    internal var mediaUpgradeOffer: MediaUpgradeOffer? = null

    @Mode
    private var currentDialogMode: Int = MODE_NONE
    private var shouldWaitMediaProjectionResult: Boolean = false

    private lateinit var watcher: ActivityWatcherForCallVisualizerContract.Watcher

    override fun setWatcher(watcher: ActivityWatcherForCallVisualizerContract.Watcher) {
        this.watcher = watcher
    }

    internal var screenSharingViewCallback: ScreenSharingController.ViewCallback? = null
    internal val startMediaProjectionLaunchers =
        mutableMapOf<String, ActivityResultLauncher<Intent>?>()

    override fun onActivityResumed(activity: Activity) {
        Logger.d(TAG, "onActivityResumed(root)")
        addDialogCallback(activity)
        addScreenSharingCallback(activity)
        callVisualizerController.setOnEngagementEndedCallback(::addEngagementEndedCallback)
        if (activity is CallVisualizerSupportActivity) {
            when (activity.intent.getParcelableExtra<PermissionType>(PERMISSION_TYPE_TAG)) {
                is ScreenSharing -> acquireMediaProjectionToken(activity)
                is Camera -> watcher.requestCameraPermission()
                else -> { /* Shouldn't happen. No need to do anything. */ }
            }
        }
    }

    private fun addEngagementEndedCallback() {
        watcher.removeDialogFromStack()
        currentDialogMode = MODE_NONE
    }

    override fun onActivityPaused() {
        Logger.d(TAG, "onActivityPaused()")
        watcher.dismissAlertDialog()
        watcher.removeDialogCallback()
        callVisualizerController.removeOnEngagementEndedCallback()
        removeScreenSharingCallback()
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
        if (state.mode != MODE_NONE && !watcher.isSupportActivityOpen()) {
            // This function is executed twice
            // First call opens CallVisualizerSupportActivity and exits the function.
            // After that this function is called again when CallVisualizerSupportActivity is started.
            // Second call shows dialog on top of CallVisualizerSupportActivity
            watcher.openSupportActivity(None)
            return
        }
        when (state.mode) {
            MODE_NONE -> watcher.dismissAlertDialog()
            MODE_MEDIA_UPGRADE -> watcher.showUpgradeDialog(state as MediaUpgrade)
            MODE_OVERLAY_PERMISSION -> watcher.showOverlayPermissionsDialog()
            MODE_ENABLE_NOTIFICATION_CHANNEL -> watcher.showAllowNotificationsDialog()
            MODE_ENABLE_SCREEN_SHARING_NOTIFICATIONS_AND_START_SHARING -> watcher.showAllowScreenSharingNotificationsAndStartSharingDialog()
            MODE_VISITOR_CODE -> watcher.showVisitorCodeDialog()
            MODE_START_SCREEN_SHARING -> watcher.showScreenSharingDialog()
            else -> {
                Logger.d(TAG, "Unexpected dialog mode received - ${state.mode}")
            }
        }
    }

    override fun onPositiveDialogButtonClicked(activity: Activity?) {
        Logger.d(TAG, "onPositiveButtonDialogButtonClicked() - $currentDialogMode")
        when (currentDialogMode) {
            MODE_NONE -> Logger.e(TAG, "$currentDialogMode should not have a dialog to click")
            MODE_ENABLE_SCREEN_SHARING_NOTIFICATIONS_AND_START_SHARING -> watcher.openNotificationChannelScreen()
            MODE_START_SCREEN_SHARING -> activity?.run { startScreenSharing(this) }
            MODE_MEDIA_UPGRADE -> watcher.openCallActivity()
            MODE_ENABLE_NOTIFICATION_CHANNEL -> watcher.openNotificationChannelScreen()
            MODE_OVERLAY_PERMISSION -> {
                watcher.dismissOverlayDialog()
                watcher.openOverlayPermissionView()
            }
            else -> Logger.d(TAG, "Not relevant")
        }
        watcher.removeDialogFromStack()
        currentDialogMode = MODE_NONE
        watcher.destroySupportActivityIfExists()
    }

    override fun onNegativeDialogButtonClicked() {
        Logger.d(TAG, "onNegativeButtonDialogButtonClicked() - $currentDialogMode")
        when (currentDialogMode) {
            MODE_NONE -> Logger.e(TAG, "$currentDialogMode should not have a dialog to click")
            MODE_ENABLE_SCREEN_SHARING_NOTIFICATIONS_AND_START_SHARING,
            MODE_START_SCREEN_SHARING -> screenSharingController.onScreenSharingDeclined()
            MODE_OVERLAY_PERMISSION -> {
                watcher.dismissOverlayDialog()
            }
            MODE_MEDIA_UPGRADE -> {
                mediaUpgradeOffer?.decline { error ->
                    onMediaUpgradeDecline(error)
                }
            }
            MODE_VISITOR_CODE,
            MODE_ENABLE_NOTIFICATION_CHANNEL -> Logger.d(TAG, "$currentDialogMode no operation")
        }
        watcher.removeDialogFromStack()
        currentDialogMode = MODE_NONE
    }

    private fun addScreenSharingCallback(activity: Activity) {
        // Call and Chat screens process screen sharing requests on their own.
        if (callVisualizerController.isCallOrChatScreenActiveUseCase(activity)) return
        setupScreenSharingViewCallback()
        screenSharingController.setViewCallback(screenSharingViewCallback)
        screenSharingController.onResume(activity) {
            startScreenSharing(activity)
        }
    }

    private fun removeScreenSharingCallback() {
        screenSharingController.removeViewCallback(screenSharingViewCallback)
    }

    @VisibleForTesting
    internal fun startScreenSharing(activity: Activity) {
        if (activity is ComponentActivity) {
            acquireMediaProjectionToken(activity)
        } else {
            watcher.openSupportActivity(ScreenSharing)
        }
        screenSharingController.onScreenSharingAcceptedAndPermissionAsked(activity)
    }

    private fun acquireMediaProjectionToken(activity: Activity) {
        Logger.d(TAG, "Acquiring Media Projection Token")
        startMediaProjectionLaunchers[activity::class.simpleName].let { startMediaProjection ->
            activity.getSystemService(MediaProjectionManager::class.java)
                ?.let { mediaProjectionManager ->
                    setIsWaitingMediaProjectionResult(true)
                    startMediaProjection?.launch(mediaProjectionManager.createScreenCaptureIntent())
                    Logger.d(TAG, "Acquire a media projection token: launching permission request")
                }
        }
    }

    @VisibleForTesting
    internal fun setupScreenSharingViewCallback() {
        screenSharingViewCallback = object : ScreenSharingController.ViewCallback {
            override fun onScreenSharingRequestError(ex: GliaException) {
                watcher.showToast(ex.debugMessage)
            }

            override fun onScreenSharingStarted() {
                if (isShowOverlayPermissionRequestDialogUseCase.execute()) {
                    currentDialogMode = MODE_OVERLAY_PERMISSION
                    watcher.callOverlayDialog()
                }
            }
        }
    }

    override fun onInitialCameraPermissionResult(isGranted: Boolean, isComponentActivity: Boolean) {
        if (!isGranted) {
            if (isComponentActivity) {
                watcher.requestCameraPermission()
            } else {
                watcher.openSupportActivity(Camera)
            }
        } else {
            mediaUpgradeOffer?.accept { onMediaUpgradeAccept(it) }
        }
    }

    override fun onRequestedCameraPermissionResult(isGranted: Boolean) {
        watcher.destroySupportActivityIfExists()
        if (isGranted) {
            mediaUpgradeOffer?.accept { onMediaUpgradeAccept(it) }
        } else {
            mediaUpgradeOffer?.decline { onMediaUpgradeDecline(it) }
        }
    }

    override fun onMediaProjectionRejected() {
        screenSharingController.onScreenSharingDeclined()
    }

    override fun onMediaProjectionPermissionResult(isGranted: Boolean, activity: Activity) {
        if (isGranted) {
            screenSharingController.onScreenSharingAccepted(activity)
        } else {
            screenSharingController.onScreenSharingDeclined()
        }
    }

    @VisibleForTesting
    internal fun onMediaUpgradeAccept(error: GliaException?) {
        error?.let {
            Logger.e(TAG, it.debugMessage, error)
        } ?: run {
            if (mediaUpgradeOffer?.video != null && mediaUpgradeOffer?.video != MediaDirection.NONE) {
                onPositiveDialogButtonClicked()
            } else {
                Logger.e(
                    TAG,
                    "Audio upgrade offer in call visualizer",
                    Exception("Audio upgrade offer in call visualizer")
                )
                return
            }
        }
    }

    @VisibleForTesting
    internal fun onMediaUpgradeDecline(error: GliaException?) {
        error?.let {
            Logger.e(TAG, it.debugMessage, error)
        } ?: run {
            currentDialogMode = MODE_NONE
            mediaUpgradeOffer = null
        }
    }

    override fun onMediaUpgradeReceived(mediaUpgrade: MediaUpgradeOffer) {
        this.mediaUpgradeOffer = mediaUpgrade
        val isPermissionRequired = mediaUpgradeOffer?.video == MediaDirection.TWO_WAY
        if (isPermissionRequired) {
            watcher.checkInitialCameraPermission()
        } else {
            mediaUpgradeOffer?.accept { error -> onMediaUpgradeAccept(error) }
        }
    }

    override fun isWaitingMediaProjectionResult() = shouldWaitMediaProjectionResult

    override fun setIsWaitingMediaProjectionResult(isWaiting: Boolean) {
        shouldWaitMediaProjectionResult = isWaiting
    }
}
