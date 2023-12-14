package com.glia.widgets.view.floatingvisitorvideoview.domain

import com.glia.widgets.core.visitor.GliaVisitorMediaRepository
import com.glia.widgets.helper.rx.Schedulers
import io.reactivex.Observable

internal class IsShowOnHoldUseCase(
    private val schedulers: Schedulers,
    private val visitorMediaRepository: GliaVisitorMediaRepository
) {
    operator fun invoke(): Observable<Boolean> = visitorMediaRepository.onHoldObserver
        .subscribeOn(schedulers.computationScheduler)
        .observeOn(schedulers.mainScheduler)
}
