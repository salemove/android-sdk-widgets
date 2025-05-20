package com.glia.widgets.view.dialog

import android.assertCurrentValue
import com.glia.widgets.view.dialog.UiComponentsDispatcher.State.NotificationPermissionDialog
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.functions.Predicate
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.After
import org.junit.Before
import org.junit.Test

internal class UiComponentsDispatcherTest {
    lateinit var dispatcher: UiComponentsDispatcher

    @Before
    fun setUp() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        dispatcher = UiComponentsDispatcherImpl()
    }

    @After
    fun tearDown() {
        RxAndroidPlugins.reset()
    }

    @Test
    fun `showNotificationPermissionDialog should emit NotificationPermissionDialog state`() {
        val state = dispatcher.state.test()

        state.assertEmpty().assertNotComplete()

        val onAllow: () -> Unit = mockk(relaxed = true)
        val onCancel: () -> Unit = mockk(relaxed = true)

        dispatcher.showNotificationPermissionDialog(onAllow, onCancel)

        val currentState = state.values().last().value as NotificationPermissionDialog

        currentState.onAllow.invoke()
        verify { onAllow() }

        currentState.onCancel.invoke()
        verify { onCancel() }
    }

    @Test
    fun `dismissDialog will produce DismissDialog state`() {
        val state = dispatcher.state.test()

        state.assertEmpty().assertNotComplete()

        dispatcher.dismissDialog()

        state.assertCurrentValue(Predicate { it.value is UiComponentsDispatcher.State.DismissDialog })
    }

    @Test
    fun `showSnackBar will produce ShowSnackBar state`() {
        val state = dispatcher.state.test()

        state.assertEmpty().assertNotComplete()

        dispatcher.showSnackBar(1)

        state.assertCurrentValue(Predicate { it.value is UiComponentsDispatcher.State.ShowSnackBar && it.value.messageResId == 1 })
    }

    @Test
    fun `launchSCTranscriptActivity will produce LaunchSCTranscriptActivity state`() {
        val state = dispatcher.state.test()

        state.assertEmpty().assertNotComplete()

        dispatcher.launchSCTranscriptActivity()

        state.assertCurrentValue(Predicate { it.value is UiComponentsDispatcher.State.LaunchSCTranscriptActivity })
    }
}
