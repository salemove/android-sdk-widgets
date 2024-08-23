package com.glia.widgets.call

import com.glia.androidsdk.Engagement
import com.glia.androidsdk.screensharing.ScreenSharing
import com.glia.widgets.locale.LocaleString
import com.glia.widgets.base.BaseController
import com.glia.widgets.base.BaseView
import com.glia.widgets.core.dialog.model.ConfirmationDialogLinks
import com.glia.widgets.core.dialog.model.Link

internal interface CallContract {
    interface Controller : BaseController {
        val confirmationDialogLinks: ConfirmationDialogLinks

        fun onBackClicked()
        fun leaveChatClicked()
        fun leaveChatQueueClicked()
        fun chatButtonClicked()
        fun onSpeakerButtonPressed()
        fun minimizeButtonClicked()
        fun muteButtonClicked()
        fun videoButtonClicked()
        fun flipVideoButtonClicked()
        fun startCall(
            companyName: String,
            queueIds: List<String>?,
            visitorContextAssetId: String?,
            mediaType: Engagement.MediaType?,
            screenSharingMode: ScreenSharing.Mode,
            upgradeToCall: Boolean
        )

        fun onResume()
        fun onPause()
        fun setView(view: View)
        fun onLiveObservationDialogRequested()
        fun endEngagementDialogYesClicked()
        fun endEngagementDialogDismissed()
        fun noMoreOperatorsAvailableDismissed()
        fun unexpectedErrorDialogDismissed()
        fun onLiveObservationDialogAllowed()
        fun onLiveObservationDialogRejected()
        fun onLinkClicked(link: Link)
        fun overlayPermissionsDialogDismissed()
        fun onUserInteraction()
        fun shouldShowMediaEngagementView(upgradeToCall: Boolean): Boolean
        fun onDestroy(retained: Boolean)
        fun stopScreenSharingClicked()
    }

    interface View : BaseView<Controller> {
        fun emitState(callState: CallState)
        fun navigateToChat()
        fun destroyView()
        fun minimizeView()
        fun showMissingPermissionsDialog()
        fun showEngagementConfirmationDialog()
        fun navigateToWebBrowserActivity(title: LocaleString, url: String)
        fun showToast(message: String)
    }
}
