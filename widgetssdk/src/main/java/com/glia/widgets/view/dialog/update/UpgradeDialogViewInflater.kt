package com.glia.widgets.view.dialog.update

import android.view.LayoutInflater
import androidx.core.view.isGone
import com.glia.widgets.view.dialog.base.DialogPayload
import com.glia.widgets.view.dialog.base.DialogViewInflater
import com.glia.widgets.view.unifiedui.applyImageColorTheme
import com.glia.widgets.view.unifiedui.theme.AlertThemeWrapper

internal open class BaseUpgradeDialogViewInflater<T : BaseUpgradeDialogViewBinding<*>>(
    binding: T,
    themeWrapper: AlertThemeWrapper,
    payload: DialogPayload.Upgrade
) : DialogViewInflater<T, DialogPayload.Upgrade>(binding, themeWrapper, payload) {
    final override fun setup(binding: T, themeWrapper: AlertThemeWrapper, payload: DialogPayload.Upgrade) {
        val theme = themeWrapper.theme

        binding.logoContainer.isGone = payload.whiteLabel ?: false
        binding.poweredByTv.text = payload.poweredByText

        setupButton(binding.positiveBtn, payload.positiveButtonText, theme.positiveButton, themeWrapper.typeface, payload.positiveButtonClickListener)
        setupButton(binding.negativeBtn, payload.negativeButtonText, theme.negativeButton, themeWrapper.typeface, payload.negativeButtonClickListener)
        binding.titleIcon.apply {
            setImageResource(payload.iconRes)
            applyImageColorTheme(theme.titleImageColor)
        }
    }
}


internal class UpgradeDialogViewInflater(layoutInflater: LayoutInflater, themeWrapper: AlertThemeWrapper, payload: DialogPayload.Upgrade) :
    BaseUpgradeDialogViewInflater<UpgradeDialogViewBinding>(UpgradeDialogViewBinding(layoutInflater), themeWrapper, payload)

internal class VerticalUpgradeDialogViewInflater(layoutInflater: LayoutInflater, themeWrapper: AlertThemeWrapper, payload: DialogPayload.Upgrade) :
    BaseUpgradeDialogViewInflater<VerticalUpgradeDialogViewBinding>(VerticalUpgradeDialogViewBinding(layoutInflater), themeWrapper, payload)
