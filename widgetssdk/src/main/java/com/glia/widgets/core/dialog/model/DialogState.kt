package com.glia.widgets.core.dialog.model

import com.glia.widgets.engagement.domain.MediaUpgradeOfferData

internal sealed interface DialogState {
    object None : DialogState
    object OverlayPermission : DialogState
    object CVOverlayPermission : DialogState
    object ExitQueue : DialogState
    object StartScreenSharing : DialogState
    object EnableNotificationChannel : DialogState
    object EnableScreenSharingNotificationsAndStartSharing : DialogState
    object VisitorCode : DialogState
    object MessageCenterUnavailable : DialogState
    object Unauthenticated : DialogState
    object Confirmation : DialogState
    object CVConfirmation : DialogState
    object EndEngagement : DialogState
    object UnexpectedError : DialogState

    data class MediaUpgrade(val data: MediaUpgradeOfferData) : DialogState
}
