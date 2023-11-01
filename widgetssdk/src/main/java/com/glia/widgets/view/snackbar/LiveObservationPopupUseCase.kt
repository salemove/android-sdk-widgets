package com.glia.widgets.view.snackbar

import com.glia.widgets.chat.domain.SiteInfoUseCase

internal class LiveObservationPopupUseCase(
    private val siteInfoUseCase: SiteInfoUseCase
) {
    operator fun invoke(callback: (shouldShow: Boolean) -> Unit) {
        siteInfoUseCase.execute { siteInfo, _ ->
            callback(siteInfo?.isObservationIndicationEnabled ?: true)
        }
    }
}
