package com.glia.widgets.chat.domain

import com.glia.widgets.core.dialog.domain.IsShowOverlayPermissionRequestDialogUseCase
import com.glia.widgets.core.dialog.domain.SetOverlayPermissionRequestDialogShownUseCase
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class DecideOnQueueingUseCaseTest {
    private lateinit var isShowOverlayPermissionRequestDialogUseCase: IsShowOverlayPermissionRequestDialogUseCase
    private lateinit var setOverlayPermissionRequestDialogShownUseCase: SetOverlayPermissionRequestDialogShownUseCase
    private lateinit var useCase: DecideOnQueueingUseCase

    @Before
    fun setUp() {
        isShowOverlayPermissionRequestDialogUseCase = mockk()
        setOverlayPermissionRequestDialogShownUseCase = mockk {
            every { execute() } just Runs
        }
    }

    @After
    fun tearDown() {
        confirmVerified(isShowOverlayPermissionRequestDialogUseCase)
        confirmVerified(setOverlayPermissionRequestDialogShownUseCase)
    }

    @Test
    fun `invoke completes when both completable are completed`() {
        every { isShowOverlayPermissionRequestDialogUseCase.execute() } returns false
        useCase = DecideOnQueueingUseCaseImpl(isShowOverlayPermissionRequestDialogUseCase, setOverlayPermissionRequestDialogShownUseCase)
        verify { isShowOverlayPermissionRequestDialogUseCase.execute() }

        val testCompletable = useCase().test()
        testCompletable.assertNotComplete()

        useCase.onQueueingRequested()
        testCompletable.assertComplete()
    }

    @Test
    fun `initialization not complete overlay completable when overlay dialog is not yet shown`() {
        every { isShowOverlayPermissionRequestDialogUseCase.execute() } returns true
        useCase = DecideOnQueueingUseCaseImpl(isShowOverlayPermissionRequestDialogUseCase, setOverlayPermissionRequestDialogShownUseCase)
        verify { isShowOverlayPermissionRequestDialogUseCase.execute() }

        val testCompletable = useCase().test()
        testCompletable.assertNotComplete()

        useCase.onQueueingRequested()
        testCompletable.assertNotComplete()

        useCase.onOverlayDialogShown()
        verify { setOverlayPermissionRequestDialogShownUseCase.execute() }
        testCompletable.assertComplete()
    }

}
