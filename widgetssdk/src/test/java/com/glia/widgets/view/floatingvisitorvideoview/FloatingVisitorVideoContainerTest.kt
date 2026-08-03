package com.glia.widgets.view.floatingvisitorvideoview

import android.content.Context
import android.mockk
import android.unMockk
import android.view.View
import com.glia.androidsdk.comms.MediaState
import com.glia.androidsdk.comms.Video
import com.glia.androidsdk.comms.VideoView
import com.glia.telemetry_lib.GliaLogger
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.ResourceProvider
import com.glia.widgets.helper.wrapWithMaterialThemeOverlay
import com.glia.widgets.helper.wrapWithTheme
import com.glia.widgets.locale.LocaleProvider
import com.glia.widgets.locale.StringKeyPair
import io.reactivex.rxjava3.core.Observable
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
internal class FloatingVisitorVideoContainerTest {

    private lateinit var context: Context
    private lateinit var container: FloatingVisitorVideoContainer

    @Before
    fun setUp() {
        GliaLogger.mockk()

        val application = RuntimeEnvironment.getApplication()
        Dependencies.resourceProvider = ResourceProvider(application)
        Dependencies.localeProvider = mock<LocaleProvider>().also {
            whenever(it.getLocaleObservable()) doReturn Observable.never()
            whenever(it.getStringInternal(any<Int>(), any<List<StringKeyPair>>())) doReturn "stub"
        }

        context = application.wrapWithTheme().wrapWithMaterialThemeOverlay()
        container = FloatingVisitorVideoContainer(context)
    }

    @After
    fun tearDown() {
        GliaLogger.unMockk()
    }

    /**
     * Regression test for MOB-5469. A network reconnect during a video engagement emits a fresh
     * [MediaState], so [FloatingVisitorVideoContainer.show] creates a new [VideoView] while the
     * container is already visible. The replaced view has to be released, otherwise its
     * `EglRenderer` leaks for the rest of the engagement.
     */
    @Test
    fun `show with a new media state releases the video view created for the previous one`() {
        val beforeReconnect = FakeVideoView(context)
        val afterReconnect = FakeVideoView(context)

        container.show(mediaState(beforeReconnect))
        container.show(mediaState(afterReconnect))

        assertEquals(View.VISIBLE, container.visibility)
        assertEquals(1, beforeReconnect.releaseCount)
        assertEquals(0, afterReconnect.releaseCount)
    }

    @Test
    fun `show with the same media state instance keeps the current video view`() {
        val videoView = FakeVideoView(context)
        val mediaState = mediaState(videoView)

        container.show(mediaState)
        container.show(mediaState)

        assertEquals(0, videoView.releaseCount)
    }

    @Test
    fun `show with a null media state keeps the current video view`() {
        val videoView = FakeVideoView(context)

        container.show(mediaState(videoView))
        container.show(null)

        assertEquals(0, videoView.releaseCount)
    }

    @Test
    fun `hide releases and detaches the current video view exactly once`() {
        val videoView = FakeVideoView(context)

        container.show(mediaState(videoView))
        container.hide()

        assertEquals(View.GONE, container.visibility)
        assertEquals(1, videoView.releaseCount)
        assertNull(videoView.parent)
    }

    @Test
    fun `show after hide re-attaches the video view for the same media state`() {
        val videoView = FakeVideoView(context)
        val mediaState = mediaState(videoView)

        container.show(mediaState)
        container.hide()
        container.show(mediaState)

        // hide() resets the tracked media state id, so the same instance counts as new again
        assertNotNull(videoView.parent)
        assertEquals(View.VISIBLE, container.visibility)
        // released only by hide(), not a second time by the re-attach
        assertEquals(1, videoView.releaseCount)
    }

    private fun mediaState(videoView: VideoView): MediaState {
        val video = mock<Video>()
        whenever(video.createVideoView(anyOrNull())) doReturn videoView

        val mediaState = mock<MediaState>()
        whenever(mediaState.video) doReturn video

        return mediaState
    }

    private class FakeVideoView(context: Context) : VideoView(context) {
        var releaseCount: Int = 0

        override fun pauseRendering() {
            /* no-op */
        }

        override fun resumeRendering() {
            /* no-op */
        }

        override fun setZOrderMediaOverlay(isMediaOverlay: Boolean) {
            /* no-op */
        }

        override fun release() {
            releaseCount++
        }

        override fun setGravity(gravity: Int) {
            /* no-op */
        }

        override fun setScalingType(scalingType: ScalingType?) {
            /* no-op */
        }
    }
}
