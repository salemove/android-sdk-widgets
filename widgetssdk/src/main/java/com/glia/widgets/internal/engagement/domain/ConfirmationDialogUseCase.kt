package com.glia.widgets.internal.engagement.domain

import com.glia.widgets.chat.domain.SiteInfoUseCase

internal class ConfirmationDialogUseCase(
    private val siteInfoUseCase: SiteInfoUseCase
) {
    operator fun invoke(callback: (shouldShow: Boolean) -> Unit) {
        siteInfoUseCase { siteInfo, _ ->
            callback(siteInfo?.isConfirmationDialogEnabled ?: true)
        }
    }
}
