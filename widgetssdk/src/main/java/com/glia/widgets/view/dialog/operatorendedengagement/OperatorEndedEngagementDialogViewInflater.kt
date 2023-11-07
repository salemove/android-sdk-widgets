package com.glia.widgets.view.dialog.operatorendedengagement

import android.view.LayoutInflater
import com.glia.widgets.view.dialog.base.DialogPayload
import com.glia.widgets.view.dialog.base.DialogViewInflater
import com.glia.widgets.view.unifiedui.theme.AlertThemeWrapper

internal class OperatorEndedEngagementDialogViewInflater(
    layoutInflater: LayoutInflater,
    themeWrapper: AlertThemeWrapper,
    payload: DialogPayload.OperatorEndedEngagement
) : DialogViewInflater<OperatorEndedEngagementDialogViewBinding, DialogPayload.OperatorEndedEngagement>(
    OperatorEndedEngagementDialogViewBinding(layoutInflater), themeWrapper, payload
) {
    override fun setup(
        binding: OperatorEndedEngagementDialogViewBinding,
        themeWrapper: AlertThemeWrapper,
        payload: DialogPayload.OperatorEndedEngagement
    ) {
        val theme = themeWrapper.theme

        setupText(binding.messageTv, payload.message, theme.message, themeWrapper.typeface)
        setupButton(binding.button, payload.buttonText, theme.positiveButton, themeWrapper.typeface, payload.buttonClickListener)
    }

}
