package com.glia.widgets.call

import com.glia.widgets.base.BaseController
import com.glia.widgets.base.BaseView
import com.glia.widgets.internal.dialog.model.ConfirmationDialogLinks
import com.glia.widgets.internal.dialog.model.Link
import com.glia.widgets.engagement.MediaType
import com.glia.widgets.locale.LocaleString

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
        fun startCall(mediaType: MediaType?, upgradeToCall: Boolean)

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
