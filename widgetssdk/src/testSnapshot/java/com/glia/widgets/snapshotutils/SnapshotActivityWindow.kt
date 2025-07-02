package com.glia.widgets.snapshotutils

import android.app.Activity
import android.content.Context
import android.view.Window
import androidx.core.graphics.toColorInt
import androidx.core.view.WindowInsetsControllerCompat
import com.glia.widgets.helper.asActivity
import com.glia.widgets.helper.hideKeyboard
import com.glia.widgets.helper.requireActivity
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

interface SnapshotActivityWindow : SnapshotTestLifecycle {

    data class Mock(
        var activityMock: Activity,
        var windowMock: Window
    )

    fun activityWindowMock(statusBarColor: Int = "#123456".toColorInt()): Mock {
        val activityMock = mock<Activity>()
        val windowMock = mock<Window>()
        whenever(activityMock.window).thenReturn(windowMock)
        whenever(windowMock.statusBarColor).thenReturn(statusBarColor)
        mockkStatic("com.glia.widgets.helper.ContextExtensions")
        every { any<Context>().requireActivity() } returns activityMock
        every { any<Context>().asActivity() } returns activityMock
        mockkStatic("com.glia.widgets.helper.Insets")
        every { any<WindowInsetsControllerCompat>().hideKeyboard() } returns Unit

        setOnEndListener {
            unmockkStatic("com.glia.widgets.helper.ContextExtensions")
            unmockkStatic("com.glia.widgets.helper.Insets")
        }

        return Mock(activityMock, windowMock)
    }
}
