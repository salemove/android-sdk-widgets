package com.glia.widgets.helper

import android.app.Activity
import android.app.Application
import com.glia.widgets.callvisualizer.CallVisualizerRepository
import com.glia.widgets.callvisualizer.controller.CallVisualizerController
import com.glia.widgets.callvisualizer.domain.IsCallOrChatScreenActiveUseCase
import junit.framework.TestCase.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.mockito.Mockito.mock

internal class ActivityWatcherForDialogsTest {

    @Test
    fun resumedActivity_cleared_whenActivityPaused() {
        val app = mock(Application::class.java)
        val callVisualizerRepository = mock(CallVisualizerRepository::class.java)
        val controller = CallVisualizerController(callVisualizerRepository, IsCallOrChatScreenActiveUseCase())
        val activityWatcherForDialogs = ActivityWatcherForDialogs(app, controller)

        activityWatcherForDialogs.onActivityResumed(mock(Activity::class.java))
        activityWatcherForDialogs.onActivityPaused(mock(Activity::class.java))

        assertNull(activityWatcherForDialogs.resumedActivity.get())
    }

    @Test
    fun resumedActivity_saved_whenActivityResumed() {
        val app = mock(Application::class.java)
        val callVisualizerRepository = mock(CallVisualizerRepository::class.java)
        val controller = CallVisualizerController(callVisualizerRepository, IsCallOrChatScreenActiveUseCase())
        val activityWatcherForDialogs = ActivityWatcherForDialogs(app, controller)

        activityWatcherForDialogs.onActivityResumed(mock(Activity::class.java))

        assertNotNull(activityWatcherForDialogs.resumedActivity.get())
    }
}
