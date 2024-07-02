package com.glia.widgets.chat.domain

import com.glia.widgets.core.dialog.domain.SetOverlayPermissionRequestDialogShownUseCase
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.subjects.CompletableSubject

internal interface DecideOnQueueingUseCase {
    operator fun invoke(): Completable
    fun onOverlayDialogShown()
    fun onQueueingRequested()
    fun markOverlayStepCompleted()
}

internal class DecideOnQueueingUseCaseImpl(
    private val setOverlayPermissionRequestDialogShownUseCase: SetOverlayPermissionRequestDialogShownUseCase
) : DecideOnQueueingUseCase {

    private val overlayStep: CompletableSubject = CompletableSubject.create()
    private val queueingStep: CompletableSubject = CompletableSubject.create()

    override fun invoke(): Completable = Completable.concat(listOf(overlayStep, queueingStep))

    override fun onOverlayDialogShown() {
        markOverlayStepCompleted()

        setOverlayPermissionRequestDialogShownUseCase()
    }

    override fun onQueueingRequested() {
        queueingStep.onComplete()
    }

    override fun markOverlayStepCompleted() {
        overlayStep.onComplete()
    }

}
