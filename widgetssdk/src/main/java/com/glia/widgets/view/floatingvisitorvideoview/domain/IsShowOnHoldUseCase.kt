package com.glia.widgets.view.floatingvisitorvideoview.domain

import com.glia.widgets.helper.rx.Schedulers
import io.reactivex.Single

internal class IsShowOnHoldUseCase(private val schedulers: Schedulers) {
    operator fun invoke(isOnHold: Boolean): Single<Boolean> {
        return Single.just(isOnHold)
            .subscribeOn(schedulers.computationScheduler)
            .observeOn(schedulers.mainScheduler)
    }
}
