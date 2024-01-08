package com.glia.widgets.call

import com.glia.androidsdk.Engagement
import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.androidsdk.screensharing.ScreenSharing
import com.glia.widgets.core.dialog.model.ConfirmationDialogLinks
import com.glia.widgets.core.dialog.model.Link

internal interface CallController {
    val confirmationDialogLinks: ConfirmationDialogLinks

    fun onBackClicked()
    fun leaveChatClicked()
    fun leaveChatQueueClicked()
    fun chatButtonClicked()
    fun onSpeakerButtonPressed()
    fun minimizeButtonClicked()
    fun muteButtonClicked()
    fun videoButtonClicked()
    fun startCall(
        companyName: String,
        queueId: String?,
        visitorContextAssetId: String?,
        mediaType: Engagement.MediaType,
        useOverlays: Boolean,
        screenSharingMode: ScreenSharing.Mode,
        upgradeToCall: Boolean
    )

    fun onResume()
    fun onPause()
    fun setViewCallback(callViewCallback: CallViewCallback?)
    fun onLiveObservationDialogRequested()
    fun endEngagementDialogYesClicked()
    fun endEngagementDialogDismissed()
    fun notificationsDialogDismissed()
    fun acceptUpgradeOfferClicked(mediaUpgradeOffer: MediaUpgradeOffer)
    fun declineUpgradeOfferClicked(mediaUpgradeOffer: MediaUpgradeOffer)
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
