package com.glia.widgets.view.dialog.option

import android.view.LayoutInflater
import androidx.core.view.isGone
import com.glia.widgets.view.dialog.base.DialogPayload
import com.glia.widgets.view.dialog.base.DialogViewInflater
import com.glia.widgets.view.unifiedui.theme.AlertThemeWrapper

internal abstract class BaseOptionDialogViewInflater<T : BaseOptionDialogViewBinding<*>>(
    binding: T,
    themeWrapper: AlertThemeWrapper,
    payload: DialogPayload.Option
) : DialogViewInflater<T, DialogPayload.Option>(binding, themeWrapper, payload) {
    override fun setup(binding: T, themeWrapper: AlertThemeWrapper, payload: DialogPayload.Option) {
        val theme = themeWrapper.theme

        binding.logoContainer.isGone = themeWrapper.whiteLabel ?: false
        binding.poweredByTv.text = payload.poweredByText

        setupText(binding.messageTv, payload.message, theme.message, themeWrapper.typeface)
    }
}

internal open class DefaultOptionDialogViewInflater<T : DefaultOptionDialogViewBinding<*>>(
    binding: T,
    themeWrapper: AlertThemeWrapper,
    payload: DialogPayload.Option
) : BaseOptionDialogViewInflater<T>(binding, themeWrapper, payload) {
    final override fun setup(binding: T, themeWrapper: AlertThemeWrapper, payload: DialogPayload.Option) {
        super.setup(binding, themeWrapper, payload)
        val theme = themeWrapper.theme
        setupButton(
            binding.positiveButton,
            payload.positiveButtonText,
            theme.positiveButton,
            themeWrapper.typeface,
            payload.positiveButtonClickListener
        )
        setupButton(
            binding.negativeButton,
            payload.negativeButtonText,
            theme.negativeButton,
            themeWrapper.typeface,
            payload.negativeButtonClickListener
        )
    }
}

internal class OptionDialogViewInflater(layoutInflater: LayoutInflater, themeWrapper: AlertThemeWrapper, payload: DialogPayload.Option) :
    DefaultOptionDialogViewInflater<OptionDialogViewBinding>(OptionDialogViewBinding(layoutInflater), themeWrapper, payload)

internal class VerticalOptionDialogViewInflater(layoutInflater: LayoutInflater, themeWrapper: AlertThemeWrapper, payload: DialogPayload.Option) :
    DefaultOptionDialogViewInflater<VerticalOptionDialogViewBinding>(VerticalOptionDialogViewBinding(layoutInflater), themeWrapper, payload)

internal open class DefaultReversedOptionDialogViewInflater<T : DefaultReversedOptionDialogViewBinding<*>>(
    binding: T,
    themeWrapper: AlertThemeWrapper,
    payload: DialogPayload.Option
) : BaseOptionDialogViewInflater<T>(binding, themeWrapper, payload) {
    final override fun setup(binding: T, themeWrapper: AlertThemeWrapper, payload: DialogPayload.Option) {
        super.setup(binding, themeWrapper, payload)
        val theme = themeWrapper.theme
        setupButton(
            binding.positiveButton,
            payload.positiveButtonText,
            theme.negativeButton,// Since buttons are reversed, the positive button is actually GliaNegativeButton and should use NegativeButton theming
            themeWrapper.typeface,
            payload.positiveButtonClickListener
        )
        setupButton(
            binding.negativeButton,
            payload.negativeButtonText,
            theme.positiveButton,// Since buttons are reversed, the positive button is actually GliaNegativeButton and should use NegativeButton theming
            themeWrapper.typeface,
            payload.negativeButtonClickListener
        )
    }
}

internal class ReversedOptionDialogViewInflater(layoutInflater: LayoutInflater, themeWrapper: AlertThemeWrapper, payload: DialogPayload.Option) :
    DefaultReversedOptionDialogViewInflater<ReversedOptionDialogViewBinding>(ReversedOptionDialogViewBinding(layoutInflater), themeWrapper, payload)

internal class VerticalReversedOptionDialogViewInflater(
    layoutInflater: LayoutInflater,
    themeWrapper: AlertThemeWrapper,
    payload: DialogPayload.Option
) : DefaultReversedOptionDialogViewInflater<VerticalReversedOptionDialogViewBinding>(
    VerticalReversedOptionDialogViewBinding(layoutInflater),
    themeWrapper,
    payload
)
