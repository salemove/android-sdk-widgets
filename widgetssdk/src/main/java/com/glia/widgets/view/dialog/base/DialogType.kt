package com.glia.widgets.view.dialog.base

internal sealed class DialogType(payload: DialogPayload) {
    data class Option(val payload: DialogPayload.Option) : DialogType(payload)
    data class OptionWithNegativeNeutral(val payload: DialogPayload.Option) : DialogType(payload)
    data class ReversedOption(val payload: DialogPayload.Option) : DialogType(payload)
    data class Confirmation(val payload: DialogPayload.Confirmation) : DialogType(payload)
    data class Upgrade(val payload: DialogPayload.Upgrade) : DialogType(payload)
    data class ScreenSharing(val payload: DialogPayload.ScreenSharing) : DialogType(payload)
    data class OperatorEndedEngagement(val payload: DialogPayload.OperatorEndedEngagement) : DialogType(payload)
    data class AlertDialog(val payload: DialogPayload.AlertDialog) : DialogType(payload)
}
