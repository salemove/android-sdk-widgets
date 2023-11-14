package com.glia.widgets.view.dialog.screensharing

import android.view.LayoutInflater
import androidx.core.view.isGone
import com.glia.widgets.view.dialog.base.DialogPayload
import com.glia.widgets.view.dialog.base.DialogViewInflater
import com.glia.widgets.view.unifiedui.applyImageColorTheme
import com.glia.widgets.view.unifiedui.theme.AlertThemeWrapper

internal open class BaseScreenSharingDialogViewInflater<T : BaseScreenSharingDialogViewBinding<*>>(
    binding: T,
    themeWrapper: AlertThemeWrapper,
    payload: DialogPayload.ScreenSharing
) : DialogViewInflater<T, DialogPayload.ScreenSharing>(binding, themeWrapper, payload) {
    final override fun setup(binding: T, themeWrapper: AlertThemeWrapper, payload: DialogPayload.ScreenSharing) {
        val theme = themeWrapper.theme

        binding.logoContainer.isGone = themeWrapper.whiteLabel ?: false
        binding.poweredByTv.text = payload.poweredByText
        binding.icon.applyImageColorTheme(theme.titleImageColor)

        setupText(binding.messageTv, payload.message, theme.message, themeWrapper.typeface)
        setupButton(binding.positiveBtn, payload.positiveButtonText, theme.positiveButton, themeWrapper.typeface, payload.positiveButtonClickListener)
        setupButton(binding.negativeBtn, payload.negativeButtonText, theme.negativeButton, themeWrapper.typeface, payload.negativeButtonClickListener)
    }

}

internal class ScreenSharingDialogViewInflater(
    layoutInflater: LayoutInflater,
    themeWrapper: AlertThemeWrapper,
    payload: DialogPayload.ScreenSharing
) : BaseScreenSharingDialogViewInflater<ScreenSharingDialogViewBinding>(ScreenSharingDialogViewBinding(layoutInflater), themeWrapper, payload)

internal class VerticalScreenSharingDialogViewInflater(
    layoutInflater: LayoutInflater,
    themeWrapper: AlertThemeWrapper,
    payload: DialogPayload.ScreenSharing
) : BaseScreenSharingDialogViewInflater<VerticalScreenSharingDialogViewBinding>(
    VerticalScreenSharingDialogViewBinding(layoutInflater),
    themeWrapper,
    payload
)
