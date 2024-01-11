package com.glia.widgets.core.dialog.model

import com.glia.androidsdk.comms.MediaUpgradeOffer

internal sealed interface DialogState {
    object None : DialogState
    object OverlayPermission : DialogState
    object ExitQueue : DialogState
    data class StartScreenSharing(val operatorName: String?) : DialogState
    object EnableNotificationChannel : DialogState
    object EnableScreenSharingNotificationsAndStartSharing : DialogState
    object VisitorCode : DialogState
    object MessageCenterUnavailable : DialogState
    object Unauthenticated : DialogState
    object Confirmation : DialogState
    object EndEngagement : DialogState
    object UnexpectedError : DialogState
    data class MediaUpgrade(val mediaUpgradeOffer: MediaUpgradeOffer, val operatorName: String?, val mode: MediaUpgradeMode) : DialogState
}

internal enum class MediaUpgradeMode {
    AUDIO, VIDEO_ONE_WAY, VIDEO_TWO_WAY
}
