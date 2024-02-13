package com.glia.widgets.view.dialog.screensharing

import android.view.LayoutInflater
import androidx.core.view.isGone
import com.glia.widgets.view.dialog.base.DialogPayload
import com.glia.widgets.view.dialog.base.DialogViewInflater
import com.glia.widgets.view.unifiedui.applyImageColorTheme
import com.glia.widgets.view.unifiedui.theme.AlertDialogConfiguration

internal open class BaseScreenSharingDialogViewInflater<T : BaseScreenSharingDialogViewBinding<*>>(
    binding: T,
    themeWrapper: AlertDialogConfiguration,
    payload: DialogPayload.ScreenSharing
) : DialogViewInflater<T, DialogPayload.ScreenSharing>(binding, themeWrapper, payload) {
    final override fun setup(binding: T, configuration: AlertDialogConfiguration, payload: DialogPayload.ScreenSharing) {
        val theme = configuration.theme

        binding.logoContainer.isGone = configuration.properties.whiteLabel ?: false
        binding.poweredByTv.text = payload.poweredByText
        configuration.icons.iconScreenSharingDialog?.also { binding.icon.setImageResource(it) }
        binding.icon.applyImageColorTheme(theme.titleImageColor)

        setupText(binding.messageTv, payload.message, theme.message, configuration.properties.typeface)
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
    }

}

internal class ScreenSharingDialogViewInflater(
    layoutInflater: LayoutInflater,
    themeWrapper: AlertDialogConfiguration,
    payload: DialogPayload.ScreenSharing
) : BaseScreenSharingDialogViewInflater<ScreenSharingDialogViewBinding>(ScreenSharingDialogViewBinding(layoutInflater), themeWrapper, payload)

internal class VerticalScreenSharingDialogViewInflater(
    layoutInflater: LayoutInflater,
    themeWrapper: AlertDialogConfiguration,
    payload: DialogPayload.ScreenSharing
) : BaseScreenSharingDialogViewInflater<VerticalScreenSharingDialogViewBinding>(
    VerticalScreenSharingDialogViewBinding(layoutInflater),
    themeWrapper,
    payload
)
