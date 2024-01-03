package com.glia.widgets.core.screensharing

import android.app.Activity
import android.content.Intent

internal interface ScreenSharingController {
    val isSharingScreen: Boolean
    fun onForceStopScreenSharing()
    fun hideScreenSharingEnabledNotification()
    fun onResume(activity: Activity, requestScreenSharingCallback: (() -> Unit)? = null)
    fun onScreenSharingDeclined()
    fun setViewCallback(screenSharingViewCallback: ViewCallback?)
    fun removeViewCallback(screenSharingViewCallback: ViewCallback?)
    fun onScreenSharingAcceptedAndPermissionAsked(activity: Activity)
    fun onScreenSharingAccepted(activity: Activity)
    fun onScreenSharingNotificationEndPressed()
    fun onActivityResultSkipPermissionRequest(resultCode: Int, data: Intent?)

    interface ViewCallback {
        fun onScreenSharingRequestError(message: String) {
            /* The function is an intentionally-blank to make it optional for implementation */
        }

        fun onScreenSharingRequestSuccess() {
            /* The function is an intentionally-blank to make it optional for implementation */
        }

        fun onScreenSharingStarted() {
            /* The function is an intentionally-blank to make it optional for implementation */
        }

        fun onScreenSharingEnded() {
            /* The function is an intentionally-blank to make it optional for implementation */
        }
    }
}
