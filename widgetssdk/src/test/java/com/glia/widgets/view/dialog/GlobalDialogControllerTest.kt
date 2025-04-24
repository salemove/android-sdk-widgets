package com.glia.widgets.view.dialog

import android.assertCurrentValue
import com.glia.widgets.view.dialog.GlobalDialogController.State.NotificationPermissionDialog
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.functions.Predicate
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.After
import org.junit.Before
import org.junit.Test

internal class GlobalDialogControllerTest {
    lateinit var controller: GlobalDialogController

    @Before
    fun setUp() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        controller = GlobalDialogControllerImpl()
    }

    @After
    fun tearDown() {
        RxAndroidPlugins.reset()
    }

    @Test
    fun `showNotificationPermissionDialog should emit NotificationPermissionDialog state`() {
        val state = controller.state.test()

        state.assertEmpty().assertNotComplete()

        val onAllow: () -> Unit = mockk(relaxed = true)
        val onCancel: () -> Unit = mockk(relaxed = true)

        controller.showNotificationPermissionDialog(onAllow, onCancel)

        val currentState = state.values().last().value as NotificationPermissionDialog

        currentState.onAllow.invoke()
        verify { onAllow() }

        currentState.onCancel.invoke()
        verify { onCancel() }
    }

    @Test
    fun `dismissDialog will produce DismissDialog state`() {
        val state = controller.state.test()

        state.assertEmpty().assertNotComplete()

        controller.dismissDialog()

        state.assertCurrentValue(Predicate { it.value is GlobalDialogController.State.DismissDialog })
    }
}
