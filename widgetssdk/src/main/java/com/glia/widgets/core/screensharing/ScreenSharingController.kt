package com.glia.widgets.core.screensharing

import android.app.Activity
import com.glia.androidsdk.GliaException

internal interface ScreenSharingController {
    val isSharingScreen: Boolean
    fun onScreenSharingRequest()
    fun onScreenSharingStarted()
    fun onScreenSharingEnded()
    fun onScreenSharingRequestError(exception: GliaException)
    fun onScreenSharingRequestSuccess()
    fun onForceStopScreenSharing()
    fun hideScreenSharingEnabledNotification()
    fun init()
    fun onResume(activity: Activity, requestScreenSharingCallback: (() -> Unit)? = null)
    fun onScreenSharingDeclined()
    fun setViewCallback(screenSharingViewCallback: ViewCallback?)
    fun removeViewCallback(screenSharingViewCallback: ViewCallback?)
    fun onScreenSharingAcceptedAndPermissionAsked(activity: Activity)
    fun onScreenSharingAccepted(activity: Activity)
    fun onScreenSharingNotificationEndPressed()

    interface ViewCallback {
        fun onScreenSharingRequestError(ex: GliaException) {
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
