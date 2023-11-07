package com.glia.widgets.view.dialog.alert

import android.view.LayoutInflater
import androidx.core.view.isVisible
import com.glia.widgets.view.dialog.base.DialogPayload
import com.glia.widgets.view.dialog.base.DialogViewInflater
import com.glia.widgets.view.unifiedui.applyImageColorTheme
import com.glia.widgets.view.unifiedui.theme.AlertThemeWrapper

internal class AlertDialogViewInflater(
    layoutInflater: LayoutInflater,
    themeWrapper: AlertThemeWrapper,
    payload: DialogPayload.AlertDialog
) :
    DialogViewInflater<AlertDialogViewBinding, DialogPayload.AlertDialog>(
        AlertDialogViewBinding(layoutInflater),
        themeWrapper,
        payload
    ) {
    override fun setup(binding: AlertDialogViewBinding, themeWrapper: AlertThemeWrapper, payload: DialogPayload.AlertDialog) {
        val theme = themeWrapper.theme

        setupText(binding.messageTv, payload.message, theme.message, themeWrapper.typeface)
        binding.closeBtn.applyImageColorTheme(theme.closeButtonColor)
        binding.closeBtn.isVisible = payload.buttonVisible
        payload.buttonClickListener?.also(binding.closeBtn::setOnClickListener)
        payload.buttonDescription?.also { binding.closeBtn.contentDescription = it }
    }
}
