package com.glia.widgets.core.audio.domain

import com.glia.androidsdk.comms.VisitorMediaState
import com.glia.widgets.core.operator.GliaOperatorMediaRepository
import com.glia.widgets.core.visitor.GliaVisitorMediaRepository
import com.glia.widgets.core.visitor.VisitorMediaUpdatesListener
import io.reactivex.Observable

class OnAudioStartedUseCase(
    private val operatorMediaRepository: GliaOperatorMediaRepository,
    private val visitorMediaRepository: GliaVisitorMediaRepository
) {
    operator fun invoke(): Observable<Boolean> {
        val array = arrayOf(
            operatorMediaObservable(),
            visitorMediaObservable()
        )
        return Observable.combineLatest(array) {
            val isOperatorAudio = it[0] as Boolean
            val isVisitorAudio = it[1] as Boolean
            return@combineLatest isOperatorAudio || isVisitorAudio
        }
            .distinctUntilChanged()
    }

    private fun operatorMediaObservable() = Observable.create { observer ->
        operatorMediaRepository.addMediaStateListener {
            observer.onNext(it?.audio != null)
        }
    }

    private fun visitorMediaObservable() = Observable.create { observer ->
        visitorMediaRepository.addVisitorMediaStateListener(object : VisitorMediaUpdatesListener {
            override fun onNewVisitorMediaState(visitorMediaState: VisitorMediaState?) {
                observer.onNext(visitorMediaState?.audio != null)
            }

            override fun onHoldChanged(isOnHold: Boolean) {}
        })
    }
}