package com.glia.widgets.view.floatingvisitorvideoview

import android.content.Context
import android.mockk
import android.unMockk
import com.glia.androidsdk.comms.VideoView
import com.glia.telemetry_lib.GliaLogger
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.ResourceProvider
import com.glia.widgets.helper.wrapWithGliaTheme
import com.glia.widgets.locale.LocaleProvider
import com.glia.widgets.locale.StringKeyPair
import io.reactivex.rxjava3.core.Observable
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
internal class FloatingVisitorVideoViewTest {

    private lateinit var context: Context
    private lateinit var view: FloatingVisitorVideoView

    @Before
    fun setUp() {
        GliaLogger.mockk()

        val application = RuntimeEnvironment.getApplication()
        Dependencies.resourceProvider = ResourceProvider(application)
        Dependencies.localeProvider = mock<LocaleProvider>().also {
            whenever(it.getLocaleObservable()) doReturn Observable.never()
            whenever(it.getStringInternal(any<Int>(), any<List<StringKeyPair>>())) doReturn "stub"
        }

        context = application.wrapWithGliaTheme()
        view = FloatingVisitorVideoView(context)
    }

    @After
    fun tearDown() {
        GliaLogger.unMockk()
    }

    /**
     * Regression test for MOB-5469: a network reconnect during a video engagement produces a new
     * [com.glia.androidsdk.comms.MediaState], which makes [FloatingVisitorVideoContainer.show] build
     * a brand-new [VideoView] while the container is still visible. Before the fix the previously
     * rendered view was overwritten without being released, leaking its `EglRenderer`.
     */
    @Test
    fun `showVisitorVideo releases the previously shown video view`() {
        val first = FakeVideoView(context)
        val second = FakeVideoView(context)

        view.showVisitorVideo(first)
        view.showVisitorVideo(second)

        assertEquals(1, first.releaseCount)
        assertEquals(0, second.releaseCount)
    }

    @Test
    fun `showVisitorVideo releases every replaced video view across repeated reconnects`() {
        val videoViews = List(3) { FakeVideoView(context) }

        videoViews.forEach(view::showVisitorVideo)

        assertTrue(videoViews.dropLast(1).all { it.releaseCount == 1 })
        assertEquals(0, videoViews.last().releaseCount)
    }

    @Test
    fun `showVisitorVideo detaches the previously shown video view and adds the new one first`() {
        val first = FakeVideoView(context)
        val second = FakeVideoView(context)

        view.showVisitorVideo(first)
        view.showVisitorVideo(second)

        assertNull(first.parent)
        assertSame(second, view.getChildAt(0))
    }

    @Test
    fun `showVisitorVideo does not release the replaced video view more than once when it is later hidden`() {
        val first = FakeVideoView(context)
        val second = FakeVideoView(context)

        view.showVisitorVideo(first)
        view.showVisitorVideo(second)
        view.hideVisitorVideo()

        assertEquals(1, first.releaseCount)
        assertEquals(1, second.releaseCount)
    }

    @Test
    fun `showVisitorVideo does not release an already hidden video view again`() {
        val first = FakeVideoView(context)
        val second = FakeVideoView(context)

        view.showVisitorVideo(first)
        view.hideVisitorVideo()
        view.showVisitorVideo(second)

        assertEquals(1, first.releaseCount)
        assertEquals(0, second.releaseCount)
    }

    @Test
    fun `showVisitorVideo places the new video view as a media overlay`() {
        val videoView = FakeVideoView(context)

        view.showVisitorVideo(videoView)

        assertTrue(videoView.isMediaOverlay)
    }

    @Test
    fun `hideVisitorVideo releases the current video view only once`() {
        val videoView = FakeVideoView(context)

        view.showVisitorVideo(videoView)
        view.hideVisitorVideo()
        view.hideVisitorVideo()

        assertEquals(1, videoView.releaseCount)
        assertNull(videoView.parent)
    }

    @Test
    fun `showOnHold pauses and hideOnHold resumes the current video view`() {
        val videoView = FakeVideoView(context)
        view.showVisitorVideo(videoView)

        view.showOnHold()
        view.hideOnHold()

        assertEquals(1, videoView.pauseCount)
        assertEquals(1, videoView.resumeCount)
    }

    @Test
    fun `onPause and onResume are not forwarded to a released video view`() {
        val videoView = FakeVideoView(context)
        view.showVisitorVideo(videoView)
        view.hideVisitorVideo()

        view.onPause()
        view.onResume()

        assertEquals(0, videoView.pauseCount)
        assertEquals(0, videoView.resumeCount)
    }

    private class FakeVideoView(context: Context) : VideoView(context) {
        var releaseCount: Int = 0
        var pauseCount: Int = 0
        var resumeCount: Int = 0
        var isMediaOverlay: Boolean = false

        override fun pauseRendering() {
            pauseCount++
        }

        override fun resumeRendering() {
            resumeCount++
        }

        override fun setZOrderMediaOverlay(isMediaOverlay: Boolean) {
            this.isMediaOverlay = isMediaOverlay
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
