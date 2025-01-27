package com.glia.widgets.view.snackbar.liveobservation

import com.glia.widgets.chat.domain.SiteInfoUseCase
import io.reactivex.rxjava3.core.Single

internal class LiveObservationPopupUseCase(private val siteInfoUseCase: SiteInfoUseCase) {
    operator fun invoke(): Single<Boolean> = Single.create {
        siteInfoUseCase { siteInfo, _ -> it.onSuccess((siteInfo?.isObservationIndicationEnabled ?: true && siteInfo?.isLiveObservationEnabled ?: true)) }
    }
}
