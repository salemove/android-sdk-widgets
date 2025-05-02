package com.glia.widgets.chat.domain

import com.glia.widgets.internal.dialog.domain.SetOverlayPermissionRequestDialogShownUseCase
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
    fun `invoke emits when both steps are completed`() {
        useCase = DecideOnQueueingUseCaseImpl(setOverlayPermissionRequestDialogShownUseCase)

        val testCompletable = useCase().test()
        testCompletable.assertNoValues()

        useCase.markOverlayStepCompleted()
        testCompletable.assertNoValues()

        useCase.onQueueingRequested()
        testCompletable.assertNotComplete().assertValue(Unit)
    }

    @Test
    fun `onOverlayDialogShown marks overlay dialog shown when called`() {
        useCase = DecideOnQueueingUseCaseImpl(setOverlayPermissionRequestDialogShownUseCase)

        val testCompletable = useCase().test()
        testCompletable.assertNotComplete()

        useCase.onQueueingRequested()
        testCompletable.assertNoValues()

        useCase.onOverlayDialogShown()
        verify { setOverlayPermissionRequestDialogShownUseCase() }
        testCompletable.assertNotComplete().assertValue(Unit)
    }


    @Test
    fun `invoke resets the queueingRequested step after each emission`() {
        useCase = DecideOnQueueingUseCaseImpl(setOverlayPermissionRequestDialogShownUseCase)

        val testCompletable = useCase().test()
        testCompletable.assertNoValues()

        useCase.markOverlayStepCompleted()
        testCompletable.assertNoValues()

        useCase.onQueueingRequested()
        testCompletable.assertNotComplete().assertValue(Unit)

        useCase.onQueueingRequested()
        testCompletable.assertNotComplete().assertValues(Unit, Unit)
    }

}
