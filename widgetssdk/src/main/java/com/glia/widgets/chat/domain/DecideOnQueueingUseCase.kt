package com.glia.widgets.chat.domain

import com.glia.widgets.core.dialog.domain.IsShowOverlayPermissionRequestDialogUseCase
import com.glia.widgets.core.dialog.domain.SetOverlayPermissionRequestDialogShownUseCase
import io.reactivex.Completable
import io.reactivex.subjects.CompletableSubject

internal interface DecideOnQueueingUseCase {
    operator fun invoke(): Completable
    fun onOverlayDialogShown()
    fun onQueueingRequested()

}

internal class DecideOnQueueingUseCaseImpl(
    private val isShowOverlayPermissionRequestDialogUseCase: IsShowOverlayPermissionRequestDialogUseCase,
    private val setOverlayPermissionRequestDialogShownUseCase: SetOverlayPermissionRequestDialogShownUseCase
) : DecideOnQueueingUseCase {

    private val overlayShown: CompletableSubject = CompletableSubject.create()
    private val queueingRequested: CompletableSubject = CompletableSubject.create()

    override fun invoke(): Completable = Completable.concat(listOf(overlayShown, queueingRequested))

    init {
        checkOverlayShown()
    }

    private fun checkOverlayShown() {
        if (!isShowOverlayPermissionRequestDialogUseCase()) {
            overlayShown.onComplete()
        }
    }

    override fun onOverlayDialogShown() {
        overlayShown.onComplete()

        setOverlayPermissionRequestDialogShownUseCase()
    }

    override fun onQueueingRequested() {
        queueingRequested.onComplete()
    }

}
