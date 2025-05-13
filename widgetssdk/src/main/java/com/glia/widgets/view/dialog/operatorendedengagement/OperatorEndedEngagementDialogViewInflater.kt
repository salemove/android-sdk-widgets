package com.glia.widgets.view.dialog.operatorendedengagement

import android.view.LayoutInflater
import com.glia.widgets.view.dialog.base.DialogPayload
import com.glia.widgets.view.dialog.base.DialogViewInflater
import com.glia.widgets.view.unifiedui.theme.AlertDialogConfiguration

internal class OperatorEndedEngagementDialogViewInflater(
    layoutInflater: LayoutInflater,
    themeWrapper: AlertDialogConfiguration,
    payload: DialogPayload.OperatorEndedEngagement
) : DialogViewInflater<OperatorEndedEngagementDialogViewBinding, DialogPayload.OperatorEndedEngagement>(
    OperatorEndedEngagementDialogViewBinding(layoutInflater), themeWrapper, payload
) {
    override fun setup(
        binding: OperatorEndedEngagementDialogViewBinding,
        configuration: AlertDialogConfiguration,
        payload: DialogPayload.OperatorEndedEngagement
    ) {
        val alertTheme = configuration.theme.alertTheme

        setupText(binding.messageTv, payload.message, alertTheme?.message, configuration.properties.typeface)
        setupButton(binding.button, payload.buttonText, alertTheme?.positiveButton, configuration.properties.typeface, payload.buttonClickListener)
    }

}
