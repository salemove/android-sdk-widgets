package com.glia.widgets.view.snackbar

import com.glia.widgets.chat.domain.SiteInfoUseCase
import io.reactivex.Single

internal class LiveObservationPopupUseCase(private val siteInfoUseCase: SiteInfoUseCase) {
    operator fun invoke(): Single<Boolean> = Single.create {
        siteInfoUseCase { siteInfo, _ -> it.onSuccess((siteInfo?.isObservationIndicationEnabled ?: true && siteInfo?.isLiveObservationEnabled ?: true)) }
    }
}
