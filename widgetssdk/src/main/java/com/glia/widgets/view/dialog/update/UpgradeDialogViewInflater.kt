package com.glia.widgets.view.dialog.update

import android.view.LayoutInflater
import androidx.core.view.isGone
import com.glia.widgets.view.dialog.base.DialogPayload
import com.glia.widgets.view.dialog.base.DialogViewInflater
import com.glia.widgets.view.unifiedui.applyImageColorTheme
import com.glia.widgets.view.unifiedui.theme.AlertDialogConfiguration

internal open class BaseUpgradeDialogViewInflater<T : BaseUpgradeDialogViewBinding<*>>(
    binding: T,
    themeWrapper: AlertDialogConfiguration,
    payload: DialogPayload.Upgrade
) : DialogViewInflater<T, DialogPayload.Upgrade>(binding, themeWrapper, payload) {
    final override fun setup(binding: T, configuration: AlertDialogConfiguration, payload: DialogPayload.Upgrade) {
        val theme = configuration.theme

        binding.logoContainer.isGone = configuration.properties.whiteLabel ?: false
        binding.poweredByTv.text = payload.poweredByText

        setupButton(
            binding.positiveBtn,
            payload.positiveButtonText,
            theme.positiveButton,
            configuration.properties.typeface,
            payload.positiveButtonClickListener
        )
        setupButton(
            binding.negativeBtn,
            payload.negativeButtonText,
            theme.negativeButton,
            configuration.properties.typeface,
            payload.negativeButtonClickListener
        )
        binding.titleIcon.apply {
            setImageResource(payload.iconRes)
            applyImageColorTheme(theme.titleImageColor)
        }
    }
}


internal class UpgradeDialogViewInflater(layoutInflater: LayoutInflater, themeWrapper: AlertDialogConfiguration, payload: DialogPayload.Upgrade) :
    BaseUpgradeDialogViewInflater<UpgradeDialogViewBinding>(UpgradeDialogViewBinding(layoutInflater), themeWrapper, payload)

internal class VerticalUpgradeDialogViewInflater(
    layoutInflater: LayoutInflater,
    themeWrapper: AlertDialogConfiguration,
    payload: DialogPayload.Upgrade
) :
    BaseUpgradeDialogViewInflater<VerticalUpgradeDialogViewBinding>(VerticalUpgradeDialogViewBinding(layoutInflater), themeWrapper, payload)
