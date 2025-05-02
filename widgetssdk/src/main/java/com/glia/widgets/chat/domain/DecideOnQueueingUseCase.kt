package com.glia.widgets.chat.domain

import com.glia.widgets.internal.dialog.domain.SetOverlayPermissionRequestDialogShownUseCase
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.processors.BehaviorProcessor

internal interface DecideOnQueueingUseCase {
    operator fun invoke(): Flowable<Unit>
    fun onOverlayDialogShown()
    fun onQueueingRequested()
    fun markOverlayStepCompleted()
}

internal class DecideOnQueueingUseCaseImpl(
    private val setOverlayPermissionRequestDialogShownUseCase: SetOverlayPermissionRequestDialogShownUseCase
) : DecideOnQueueingUseCase {

    private val overlayStep: BehaviorProcessor<Boolean> = BehaviorProcessor.createDefault(false)
    private val queueingStep: BehaviorProcessor<Boolean> = BehaviorProcessor.createDefault(false)

    override fun invoke(): Flowable<Unit> = Flowable.combineLatest(overlayStep, queueingStep) { overlay, queueing ->
        //make sure that we have completed both steps
        overlay && queueing
    }
        .filter { it } //filter out until both steps are completed
        .map { }// map to unit
        .doOnNext {
            //reset only the queueing step, because the overlay step is checked once per session
            queueingStep.onNext(false)
        }

    override fun onOverlayDialogShown() {
        markOverlayStepCompleted()

        setOverlayPermissionRequestDialogShownUseCase()
    }

    override fun onQueueingRequested() {
        queueingStep.onNext(true)
    }

    override fun markOverlayStepCompleted() {
        overlayStep.onNext(true)
    }

}
