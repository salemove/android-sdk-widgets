package com.glia.widgets.view.dialog.confirmation

import android.view.LayoutInflater
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.glia.widgets.helper.setText
import com.glia.widgets.view.dialog.base.DialogPayload
import com.glia.widgets.view.dialog.base.DialogViewInflater
import com.glia.widgets.view.unifiedui.applyWhiteLabel
import com.glia.widgets.view.unifiedui.theme.AlertDialogConfiguration

internal abstract class BaseConfirmationDialogViewInflater<T : BaseConfirmationDialogViewBinding<*>>(
    binding: T,
    themeWrapper: AlertDialogConfiguration,
    payload: DialogPayload.Confirmation
) : DialogViewInflater<T, DialogPayload.Confirmation>(binding, themeWrapper, payload) {
    override fun setup(binding: T, configuration: AlertDialogConfiguration, payload: DialogPayload.Confirmation) {
        val theme = configuration.theme

        binding.logoContainer.applyWhiteLabel(theme.isWhiteLabel)
        binding.poweredByTv.setText(payload.poweredByText)

        setupText(binding.messageTv, payload.message, theme.alertTheme?.message, configuration.properties.typeface)
    }
}

internal open class DefaultConfirmationDialogViewInflater<T : DefaultConfirmationDialogViewBinding<*>>(
    binding: T,
    themeWrapper: AlertDialogConfiguration,
    payload: DialogPayload.Confirmation
) : BaseConfirmationDialogViewInflater<T>(binding, themeWrapper, payload) {
    final override fun setup(binding: T, configuration: AlertDialogConfiguration, payload: DialogPayload.Confirmation) {
        super.setup(binding, configuration, payload)
        val alertTheme = configuration.theme.alertTheme
        setupButton(
            binding.link1Button,
            payload.link1,
            alertTheme?.linkButton,
            configuration.properties.typeface,
            payload.link1ClickListener
        )
        setupButton(
            binding.link2Button,
            payload.link2,
            alertTheme?.linkButton,
            configuration.properties.typeface,
            payload.link2ClickListener
        )
        setupButton(
            binding.positiveButton,
            payload.positiveButtonText,
            alertTheme?.positiveButton,
            configuration.properties.typeface,
            payload.positiveButtonClickListener
        )
        setupButton(
            binding.negativeButton,
            payload.negativeButtonText,
            alertTheme?.negativeButton,
            configuration.properties.typeface,
            payload.negativeButtonClickListener
        )
        binding.additionalButtonsSpace.isGone = binding.link1Button.isVisible || binding.link2Button.isVisible
    }
}

internal class ConfirmationDialogViewInflater(
    layoutInflater: LayoutInflater,
    themeWrapper: AlertDialogConfiguration,
    payload: DialogPayload.Confirmation
) :
    DefaultConfirmationDialogViewInflater<ConfirmationDialogViewBinding>(ConfirmationDialogViewBinding(layoutInflater), themeWrapper, payload)

internal class VerticalConfirmationDialogViewInflater(
    layoutInflater: LayoutInflater,
    themeWrapper: AlertDialogConfiguration,
    payload: DialogPayload.Confirmation
) :
    DefaultConfirmationDialogViewInflater<VerticalConfirmationDialogViewBinding>(
        VerticalConfirmationDialogViewBinding(layoutInflater),
        themeWrapper,
        payload
    )

internal open class DefaultReversedConfirmationDialogViewInflater<T : DefaultReversedConfirmationDialogViewBinding<*>>(
    binding: T,
    themeWrapper: AlertDialogConfiguration,
    payload: DialogPayload.Confirmation
) : BaseConfirmationDialogViewInflater<T>(binding, themeWrapper, payload) {
    final override fun setup(binding: T, configuration: AlertDialogConfiguration, payload: DialogPayload.Confirmation) {
        super.setup(binding, configuration, payload)
        val alertTheme = configuration.theme.alertTheme
        setupButton(
            binding.positiveButton,
            payload.positiveButtonText,
            alertTheme?.negativeButton,// Since buttons are reversed, the positive button is actually GliaNegativeButton and should use NegativeButton theming
            configuration.properties.typeface,
            payload.positiveButtonClickListener
        )
        setupButton(
            binding.negativeButton,
            payload.negativeButtonText,
            alertTheme?.positiveButton,// Since buttons are reversed, the positive button is actually GliaNegativeButton and should use NegativeButton theming
            configuration.properties.typeface,
            payload.negativeButtonClickListener
        )
    }
}
