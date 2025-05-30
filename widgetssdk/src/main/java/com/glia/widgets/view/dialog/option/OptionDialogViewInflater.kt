package com.glia.widgets.view.dialog.option

import android.view.LayoutInflater
import com.glia.widgets.helper.setText
import com.glia.widgets.view.dialog.base.DialogPayload
import com.glia.widgets.view.dialog.base.DialogViewInflater
import com.glia.widgets.view.unifiedui.applyWhiteLabel
import com.glia.widgets.view.unifiedui.theme.AlertDialogConfiguration

internal abstract class BaseOptionDialogViewInflater<T : BaseOptionDialogViewBinding<*>>(
    binding: T,
    themeWrapper: AlertDialogConfiguration,
    payload: DialogPayload.Option
) : DialogViewInflater<T, DialogPayload.Option>(binding, themeWrapper, payload) {
    override fun setup(binding: T, configuration: AlertDialogConfiguration, payload: DialogPayload.Option) {
        val theme = configuration.theme

        binding.logoContainer.applyWhiteLabel(theme.isWhiteLabel)
        binding.poweredByTv.setText(payload.poweredByText)

        setupText(binding.messageTv, payload.message, theme.alertTheme?.message, configuration.properties.typeface)
    }
}

internal open class DefaultOptionDialogViewInflater<T : DefaultOptionDialogViewBinding<*>>(
    binding: T,
    themeWrapper: AlertDialogConfiguration,
    payload: DialogPayload.Option
) : BaseOptionDialogViewInflater<T>(binding, themeWrapper, payload) {
    final override fun setup(binding: T, configuration: AlertDialogConfiguration, payload: DialogPayload.Option) {
        super.setup(binding, configuration, payload)
        val alertTheme = configuration.theme.alertTheme
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
    }
}

internal class OptionDialogViewInflater(layoutInflater: LayoutInflater, themeWrapper: AlertDialogConfiguration, payload: DialogPayload.Option) :
    DefaultOptionDialogViewInflater<OptionDialogViewBinding>(OptionDialogViewBinding(layoutInflater), themeWrapper, payload)

internal class VerticalOptionDialogViewInflater(
    layoutInflater: LayoutInflater,
    themeWrapper: AlertDialogConfiguration,
    payload: DialogPayload.Option
) :
    DefaultOptionDialogViewInflater<VerticalOptionDialogViewBinding>(VerticalOptionDialogViewBinding(layoutInflater), themeWrapper, payload)

internal open class DefaultReversedOptionDialogViewInflater<T : DefaultReversedOptionDialogViewBinding<*>>(
    binding: T,
    themeWrapper: AlertDialogConfiguration,
    payload: DialogPayload.Option
) : BaseOptionDialogViewInflater<T>(binding, themeWrapper, payload) {
    final override fun setup(binding: T, configuration: AlertDialogConfiguration, payload: DialogPayload.Option) {
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

internal class ReversedOptionDialogViewInflater(
    layoutInflater: LayoutInflater,
    themeWrapper: AlertDialogConfiguration,
    payload: DialogPayload.Option
) :
    DefaultReversedOptionDialogViewInflater<ReversedOptionDialogViewBinding>(ReversedOptionDialogViewBinding(layoutInflater), themeWrapper, payload)

internal class VerticalReversedOptionDialogViewInflater(
    layoutInflater: LayoutInflater,
    themeWrapper: AlertDialogConfiguration,
    payload: DialogPayload.Option
) : DefaultReversedOptionDialogViewInflater<VerticalReversedOptionDialogViewBinding>(
    VerticalReversedOptionDialogViewBinding(layoutInflater),
    themeWrapper,
    payload
)


internal class OptionWithNegativeNeutralDialogViewInflater(layoutInflater: LayoutInflater, themeWrapper: AlertDialogConfiguration, payload: DialogPayload.Option) :
    DefaultOptionWithNegativeNeutralDialogViewInflater<OptionDialogViewBinding>(OptionDialogViewBinding(layoutInflater), themeWrapper, payload)

internal class VerticalOptionWithNegativeNeutralDialogViewInflater(
    layoutInflater: LayoutInflater,
    themeWrapper: AlertDialogConfiguration,
    payload: DialogPayload.Option
) :
    DefaultOptionWithNegativeNeutralDialogViewInflater<VerticalOptionDialogViewBinding>(VerticalOptionDialogViewBinding(layoutInflater), themeWrapper, payload)

internal open class DefaultOptionWithNegativeNeutralDialogViewInflater<T : DefaultOptionDialogViewBinding<*>>(
    binding: T,
    themeWrapper: AlertDialogConfiguration,
    payload: DialogPayload.Option
) : BaseOptionDialogViewInflater<T>(binding, themeWrapper, payload) {
    final override fun setup(binding: T, configuration: AlertDialogConfiguration, payload: DialogPayload.Option) {
        super.setup(binding, configuration, payload)
        val alertTheme = configuration.theme.alertTheme
        setupButton(
            binding.negativeButton,      // Reversed button positions: negative button takes all properties of positive
            payload.positiveButtonText,
            alertTheme?.positiveButton,
            configuration.properties.typeface,
            payload.positiveButtonClickListener
        )
        setupButton(
            binding.positiveButton,      // Reversed button positions: positive button takes all properties of negative
            payload.negativeButtonText,
            alertTheme?.negativeNeutralButton, // Not 'negative' theme but from 'negative neutral' theme
            configuration.properties.typeface,
            payload.negativeButtonClickListener
        )
    }
}
