package com.glia.widgets.view.dialog.singlebuttonoption

import android.view.LayoutInflater
import com.glia.widgets.helper.setText
import com.glia.widgets.view.dialog.base.DialogPayload
import com.glia.widgets.view.dialog.base.DialogViewInflater
import com.glia.widgets.view.dialog.option.SingleButtonOptionDialogViewBinding
import com.glia.widgets.view.unifiedui.applyWhiteLabel
import com.glia.widgets.view.unifiedui.theme.AlertDialogConfiguration

internal class SingleButtonOptionDialogViewInflater(
    layoutInflater: LayoutInflater,
    themeWrapper: AlertDialogConfiguration,
    payload: DialogPayload.SingleButtonOption
) : DialogViewInflater<SingleButtonOptionDialogViewBinding, DialogPayload.SingleButtonOption>(
    SingleButtonOptionDialogViewBinding(layoutInflater), themeWrapper, payload
) {
    override fun setup(
        binding: SingleButtonOptionDialogViewBinding,
        configuration: AlertDialogConfiguration,
        payload: DialogPayload.SingleButtonOption
    ) {
        val theme = configuration.theme
        binding.logoContainer.applyWhiteLabel(theme.isWhiteLabel)
        binding.poweredByTv.setText(payload.poweredByText)
        setupText(binding.messageTv, payload.message, theme.alertTheme?.message, configuration.properties.typeface)
        setupButton(
            binding.positiveButton,
            payload.positiveButtonText,
            theme.alertTheme?.positiveButton,
            configuration.properties.typeface,
            payload.positiveButtonClickListener,
            payload.positiveButtonAccessibilityHint
        )
    }
}
