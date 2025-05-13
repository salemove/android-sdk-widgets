package com.glia.widgets.view.dialog.alert

import android.view.LayoutInflater
import androidx.core.view.isVisible
import com.glia.widgets.helper.setContentDescription
import com.glia.widgets.view.dialog.base.DialogPayload
import com.glia.widgets.view.dialog.base.DialogViewInflater
import com.glia.widgets.view.unifiedui.applyImageColorTheme
import com.glia.widgets.view.unifiedui.theme.AlertDialogConfiguration

internal class AlertDialogViewInflater(
    layoutInflater: LayoutInflater,
    themeWrapper: AlertDialogConfiguration,
    payload: DialogPayload.AlertDialog
) : DialogViewInflater<AlertDialogViewBinding, DialogPayload.AlertDialog>(
    AlertDialogViewBinding(layoutInflater),
    themeWrapper,
    payload
) {
    override fun setup(binding: AlertDialogViewBinding, configuration: AlertDialogConfiguration, payload: DialogPayload.AlertDialog) {
        val alertTheme = configuration.theme.alertTheme

        setupText(binding.messageTv, payload.message, alertTheme?.message, configuration.properties.typeface)
        binding.closeBtn.applyImageColorTheme(alertTheme?.closeButtonColor)
        binding.closeBtn.isVisible = payload.buttonVisible
        payload.buttonClickListener?.also(binding.closeBtn::setOnClickListener)
        payload.buttonDescription?.also { binding.closeBtn.setContentDescription(it) }
        configuration.icons.iconLeaveQueue?.also { binding.closeBtn.setImageResource(it) }
    }
}
