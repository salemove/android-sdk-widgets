package com.glia.widgets.internal.dialog.model

import com.glia.widgets.engagement.domain.MediaUpgradeOfferData

internal sealed interface DialogState {
    data object None : DialogState
    data object OverlayPermission : DialogState
    data object CVOverlayPermission : DialogState
    data object ExitQueue : DialogState
    data object VisitorCode : DialogState
    data object MessageCenterUnavailable : DialogState
    data object Unauthenticated : DialogState
    data object Confirmation : DialogState
    data object CVConfirmation : DialogState
    data object EndEngagement : DialogState
    data object UnexpectedError : DialogState

    data class MediaUpgrade(val data: MediaUpgradeOfferData) : DialogState
    data class LeaveCurrentConversation(val action: LeaveDialogAction) : DialogState
}
