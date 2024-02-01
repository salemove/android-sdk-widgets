package com.glia.widgets.callvisualizer

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.VisibleForTesting
import com.glia.widgets.callvisualizer.CallVisualizerSupportActivity.Companion.PERMISSION_TYPE_TAG
import com.glia.widgets.callvisualizer.controller.CallVisualizerContract
import com.glia.widgets.core.dialog.domain.ConfirmationDialogLinksUseCase
import com.glia.widgets.core.dialog.domain.IsShowOverlayPermissionRequestDialogUseCase
import com.glia.widgets.core.dialog.model.DialogState
import com.glia.widgets.core.dialog.model.Link
import com.glia.widgets.core.screensharing.ScreenSharingContract
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.unSafeSubscribe

@SuppressLint("CheckResult")
internal class ActivityWatcherForCallVisualizerController(
    private val callVisualizerController: CallVisualizerContract.Controller,
    private val screenSharingController: ScreenSharingContract.Controller,
    private val isShowOverlayPermissionRequestDialogUseCase: IsShowOverlayPermissionRequestDialogUseCase,
    private val confirmationDialogLinksUseCase: ConfirmationDialogLinksUseCase
) : ActivityWatcherForCallVisualizerContract.Controller {

    private var currentDialogState: DialogState = DialogState.None
    private var shouldWaitMediaProjectionResult: Boolean = false

    private lateinit var watcher: ActivityWatcherForCallVisualizerContract.Watcher

    override fun setWatcher(watcher: ActivityWatcherForCallVisualizerContract.Watcher) {
        this.watcher = watcher
    }

    internal var screenSharingViewCallback: ScreenSharingContract.ViewCallback? = null
    internal val startMediaProjectionLaunchers = mutableMapOf<String, ActivityResultLauncher<Intent>?>()

    init {
        callVisualizerController.apply {
            engagementStartFlow.unSafeSubscribe { watcher.engagementStarted() }
            engagementEndFlow.unSafeSubscribe { onEngagementEnded() }
        }
    }

    override fun onActivityResumed(activity: Activity) {
        Logger.d(TAG, "onActivityResumed(root)")
        addDialogCallback(activity)
        if (activity is CallVisualizerSupportActivity) {
            when (activity.intent.getParcelableExtra<PermissionType>(PERMISSION_TYPE_TAG)) {
                is ScreenSharing -> acquireMediaProjectionToken(activity)
                else -> { /* Shouldn't happen. No need to do anything. */
                }
            }
        }
        screenSharingController.onResume(activity) {
            startScreenSharing(activity)
        }
    }

    private fun onEngagementEnded() {
        watcher.removeDialogFromStack()
        currentDialogState = DialogState.None

        removeScreenSharingCallback()
    }

    override fun onActivityPaused() {
        Logger.d(TAG, "onActivityPaused()")
        watcher.dismissAlertDialog()
        watcher.removeDialogCallback()
    }

    override fun isCallOrChatActive(activity: Activity): Boolean {
        return callVisualizerController.isCallOrChatScreenActive(activity)
    }

    override fun removeMediaProjectionLaunchers(activityName: String?) {
        startMediaProjectionLaunchers.remove(activityName)
    }

    override fun startMediaProjectionLaunchers(activityName: String, launcher: ActivityResultLauncher<Intent>?) {
        startMediaProjectionLaunchers[activityName] = launcher
    }

    private fun addDialogCallback(activity: Activity) {
        // There are separate dialog callbacks for incoming media requests on Call and Chat screens.
        if (callVisualizerController.isCallOrChatScreenActive(activity)) return

        watcher.setupDialogCallback()
    }

    override fun onDialogControllerCallback(state: DialogState) {
        // This ensures that dialog state is set to MODE_NONE only by dismissing the dialog properly
        if (state != DialogState.None) {
            currentDialogState = state
        }
        if (watcher.isWebBrowserActivityOpen()) {
            // Prevent opening the support activity and show the dialog.
            // We need this if the user opens the links from the engagement confirmation dialog.
            return
        }
        if (state != DialogState.None && !watcher.isSupportActivityOpen()) {
            // This function is executed twice
            // First call opens CallVisualizerSupportActivity and exits the function.
            // After that, this function is called again when CallVisualizerSupportActivity is started.
            // Second call shows dialog on top of CallVisualizerSupportActivity
            watcher.openSupportActivity(None)
            return
        }
        when (state) {
            DialogState.None -> watcher.dismissAlertDialog()
            DialogState.OverlayPermission -> watcher.showOverlayPermissionsDialog()
            DialogState.EnableNotificationChannel -> watcher.showAllowNotificationsDialog()
            DialogState.EnableScreenSharingNotificationsAndStartSharing -> watcher.showAllowScreenSharingNotificationsAndStartSharingDialog()
            DialogState.VisitorCode -> watcher.showVisitorCodeDialog()
            is DialogState.StartScreenSharing -> watcher.showScreenSharingDialog(state.operatorName)
            DialogState.Confirmation -> watcher.showEngagementConfirmationDialog()
            else -> {
                Logger.d(TAG, "Unexpected dialog mode received - $state")
            }
        }
    }

    override fun getConfirmationDialogLinks() = confirmationDialogLinksUseCase()

    override fun onLinkClicked(link: Link) {
        Logger.d(TAG, "onLinkClicked")
        watcher.openWebBrowserActivity(link.title, link.url)
    }

    override fun onPositiveDialogButtonClicked(activity: Activity?) {
        Logger.d(TAG, "onPositiveButtonDialogButtonClicked() - $currentDialogState")
        when (currentDialogState) {
            DialogState.None -> Logger.e(TAG, "$currentDialogState should not have a dialog to click")
            DialogState.EnableScreenSharingNotificationsAndStartSharing -> watcher.openNotificationChannelScreen()
            is DialogState.StartScreenSharing -> activity?.run { startScreenSharing(this) }
            DialogState.EnableNotificationChannel -> watcher.openNotificationChannelScreen()
            DialogState.OverlayPermission -> {
                watcher.dismissOverlayDialog()
                watcher.openOverlayPermissionView()
            }

            DialogState.Confirmation -> callVisualizerController.onEngagementConfirmationDialogAllowed()
            else -> Logger.d(TAG, "Not relevant")
        }
        watcher.removeDialogFromStack()
        currentDialogState = DialogState.None
        watcher.destroySupportActivityIfExists()
    }

    override fun onNegativeDialogButtonClicked() {
        Logger.d(TAG, "onNegativeButtonDialogButtonClicked() - $currentDialogState")
        when (currentDialogState) {
            DialogState.None -> Logger.e(TAG, "$currentDialogState should not have a dialog to click")
            DialogState.EnableScreenSharingNotificationsAndStartSharing,
            is DialogState.StartScreenSharing -> screenSharingController.onScreenSharingDeclined()

            DialogState.OverlayPermission -> watcher.dismissOverlayDialog()
            DialogState.Confirmation -> callVisualizerController.onEngagementConfirmationDialogDeclined()
            DialogState.VisitorCode, DialogState.EnableNotificationChannel -> Logger.d(TAG, "$currentDialogState no operation")
            else -> Logger.d(TAG, "Not relevant")
        }
        watcher.removeDialogFromStack()
        currentDialogState = DialogState.None
    }

    override fun addScreenSharingCallback(activity: Activity) {
        // Call and Chat screens process screen sharing requests on their own.
        if (callVisualizerController.isCallOrChatScreenActive(activity)) return
        setupScreenSharingViewCallback()
        screenSharingController.setViewCallback(screenSharingViewCallback)
    }

    override fun mediaProjectionOnActivityResultSkipPermissionRequest(resultCode: Int, data: Intent?) {
        screenSharingController.onActivityResultSkipPermissionRequest(resultCode, data)
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
        screenSharingViewCallback = object : ScreenSharingContract.ViewCallback {
            override fun onScreenSharingRequestError(message: String) = watcher.showToast(message)

            override fun onScreenSharingStarted() {
                if (isShowOverlayPermissionRequestDialogUseCase.execute()) {
                    currentDialogState = DialogState.OverlayPermission
                    watcher.callOverlayDialog()
                }
            }
        }
    }

    override fun onMediaProjectionRejected() {
        screenSharingController.onScreenSharingDeclined()
    }

    override fun onMediaProjectionPermissionResult(isGranted: Boolean, activity: Activity) {
        Logger.i(TAG, "Media projection granted by visitor: $isGranted")
        if (isGranted) {
            screenSharingController.onScreenSharingAccepted(activity)
        } else {
            screenSharingController.onScreenSharingDeclined()
        }
    }

    override fun isWaitingMediaProjectionResult() = shouldWaitMediaProjectionResult

    override fun setIsWaitingMediaProjectionResult(isWaiting: Boolean) {
        shouldWaitMediaProjectionResult = isWaiting
    }
}
