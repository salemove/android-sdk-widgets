package com.glia.widgets.view.dialog.base

internal sealed class DialogType(payload: DialogPayload, traceLabel: String) {
    data class Option(val payload: DialogPayload.Option, val traceLabel: String) : DialogType(payload, traceLabel)
    data class OptionWithNegativeNeutral(val payload: DialogPayload.Option) : DialogType(payload, "OptionWithNegativeNeutral")
    data class ReversedOption(val payload: DialogPayload.Option, val traceLabel: String) : DialogType(payload, traceLabel)
    data class Confirmation(val payload: DialogPayload.Confirmation) : DialogType(payload, "Engagement Start Confirmation")
    data class Upgrade(val payload: DialogPayload.Upgrade) : DialogType(payload, "Media Upgrade Request")
    data class ScreenSharing(val payload: DialogPayload.ScreenSharing) : DialogType(payload, "Screen Sharing Request")
    data class OperatorEndedEngagement(val payload: DialogPayload.OperatorEndedEngagement) : DialogType(payload, "Engagement Ended By Operator")
    data class AlertDialog(val payload: DialogPayload.AlertDialog, val traceLabel: String) : DialogType(payload, traceLabel)

    val trace: String = "Dialog: $traceLabel"
}
