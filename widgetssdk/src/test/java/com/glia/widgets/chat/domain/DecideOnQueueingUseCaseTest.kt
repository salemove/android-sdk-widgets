package com.glia.widgets.chat.domain

import com.glia.widgets.core.dialog.domain.SetOverlayPermissionRequestDialogShownUseCase
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class DecideOnQueueingUseCaseTest {
    private lateinit var setOverlayPermissionRequestDialogShownUseCase: SetOverlayPermissionRequestDialogShownUseCase
    private lateinit var useCase: DecideOnQueueingUseCase

    @Before
    fun setUp() {
        setOverlayPermissionRequestDialogShownUseCase = mockk(relaxUnitFun = true)
    }

    @After
    fun tearDown() {
        confirmVerified(setOverlayPermissionRequestDialogShownUseCase)
    }

    @Test
    fun `invoke completes when both completable are completed`() {
        useCase = DecideOnQueueingUseCaseImpl(setOverlayPermissionRequestDialogShownUseCase)

        val testCompletable = useCase().test()
        testCompletable.assertNotComplete()

        useCase.markOverlayStepCompleted()
        testCompletable.assertNotComplete()

        useCase.onQueueingRequested()
        testCompletable.assertComplete()
    }

    @Test
    fun `onOverlayDialogShown marks overlay dialog shown when called`() {
        useCase = DecideOnQueueingUseCaseImpl(setOverlayPermissionRequestDialogShownUseCase)

        val testCompletable = useCase().test()
        testCompletable.assertNotComplete()

        useCase.onQueueingRequested()
        testCompletable.assertNotComplete()

        useCase.onOverlayDialogShown()
        verify { setOverlayPermissionRequestDialogShownUseCase() }
        testCompletable.assertComplete()
    }

}
