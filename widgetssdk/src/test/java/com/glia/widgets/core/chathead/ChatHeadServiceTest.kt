package com.glia.widgets.core.chathead

import android.mock
import android.os.Build
import android.os.Looper
import android.unMock
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import androidx.core.content.getSystemService
import com.glia.widgets.R
import com.glia.widgets.di.ControllerFactory
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.ResourceProvider
import com.glia.widgets.helper.wrapWithGliaTheme
import com.glia.widgets.locale.LocaleProvider
import com.glia.widgets.locale.StringKeyPair
import com.glia.widgets.internal.chathead.BubblePosition
import com.glia.widgets.internal.chathead.BubbleRenderTarget
import com.glia.widgets.internal.chathead.FakeChatBubbleController
import com.glia.widgets.view.head.ChatHeadView
import io.reactivex.rxjava3.core.Observable
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowApplication
import org.robolectric.shadows.ShadowWindowManagerImpl

/**
 * The bubble service is the only Glia component that wraps a `Context` in `attachBaseContext`, which is
 * the one place the theme cannot be resolved eagerly.
 *
 * The theme tests assert the resulting service theme, not the attach ordering: Robolectric's
 * `Service.attach` shadow does not reproduce the real framework's `mBase`-assignment order, so it
 * passes either way. The ordering itself is only observable on a device - see the note on
 * [wrapWithGliaTheme].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
internal class ChatHeadServiceTest {

    private lateinit var controller: FakeChatBubbleController

    @Before
    fun setUp() {
        Logger.mock()
        val application = RuntimeEnvironment.getApplication()

        Dependencies.resourceProvider = ResourceProvider(application)

        val localeProvider = mock<LocaleProvider>()
        whenever(localeProvider.getLocaleObservable()) doReturn Observable.never()
        whenever(localeProvider.getString(any<Int>(), any<List<StringKeyPair>>())) doReturn "Back to the Engagement. Floating Button."
        Dependencies.localeProvider = localeProvider

        controller = FakeChatBubbleController()
        val controllerFactory = mock<ControllerFactory>()
        whenever(controllerFactory.chatBubbleController) doReturn controller
        Dependencies.controllerFactory = controllerFactory

        ShadowApplication().grantPermissions(android.Manifest.permission.SYSTEM_ALERT_WINDOW)
    }

    @After
    fun tearDown() {
        Logger.unMock()
    }

    @Test
    fun `onCreate collects the bubble state`() {
        Robolectric.buildService(ChatHeadService::class.java).create()

        assertEquals(1, controller.state.subscriptionCount.value)
    }

    @Test
    fun `onDestroy stops collecting so the service is not leaked by the process-wide controller`() {
        val serviceController = Robolectric.buildService(ChatHeadService::class.java).create()

        serviceController.destroy()

        assertEquals(0, controller.state.subscriptionCount.value)
    }

    // region The overlay window, which the service owns independently of its own lifetime

    @Test
    fun `no window is added while the bubble does not render on the overlay`() {
        // Given the service is kept running while the app is foregrounded, so that it is already up
        // by the time the app is backgrounded - it must not draw over the app in the meantime
        controller.emit(target = BubbleRenderTarget.APPLICATION)

        Robolectric.buildService(ChatHeadService::class.java).create()
        idle()

        assertEquals(0, overlayViews().size)
    }

    @Test
    fun `the window is added when the bubble starts rendering on the overlay`() {
        // Given
        controller.emit(target = BubbleRenderTarget.APPLICATION)
        Robolectric.buildService(ChatHeadService::class.java).create()
        idle()

        // When the app goes to the background
        controller.emit(target = BubbleRenderTarget.SERVICE)
        idle()

        // Then
        assertEquals(1, overlayViews().size)
    }

    @Test
    fun `the window is removed when the bubble stops rendering on the overlay`() {
        // Given
        controller.emit(target = BubbleRenderTarget.SERVICE)
        Robolectric.buildService(ChatHeadService::class.java).create()
        idle()

        // When the app comes back to the foreground - the service keeps running
        controller.emit(target = BubbleRenderTarget.APPLICATION)
        idle()

        // Then
        assertEquals(0, overlayViews().size)
    }

    @Test
    fun `the window is added only once across repeated overlay states`() {
        // Given
        controller.emit(target = BubbleRenderTarget.SERVICE)
        Robolectric.buildService(ChatHeadService::class.java).create()
        idle()

        // When
        controller.emit(target = BubbleRenderTarget.SERVICE, unreadCount = 2)
        idle()

        // Then
        assertEquals(1, overlayViews().size)
    }

    @Test
    fun `the window is removed on destroy`() {
        // Given
        controller.emit(target = BubbleRenderTarget.SERVICE)
        val serviceController = Robolectric.buildService(ChatHeadService::class.java).create()
        idle()

        // When
        serviceController.destroy()

        // Then
        assertEquals(0, overlayViews().size)
    }

    @Test
    fun `the window opens at the shared bubble position`() {
        // Given the visitor dragged the bubble (on either surface) to the top-left limit
        controller.bubblePosition = BubblePosition(0f, 0f)
        controller.emit(target = BubbleRenderTarget.SERVICE)

        Robolectric.buildService(ChatHeadService::class.java).create()
        idle()

        // Then the fraction maps to this display's own range
        val params = overlayViews().first().layoutParams as WindowManager.LayoutParams
        assertEquals(0, params.x)
        assertEquals(0, params.y)
    }

    @Test
    fun `the window is positioned when it is added, not when the service starts`() {
        // Given the service has been running since long before the window first shows
        controller.emit(target = BubbleRenderTarget.APPLICATION)
        Robolectric.buildService(ChatHeadService::class.java).create()
        idle()

        // When the in-app bubble is dragged in the meantime and the app is then backgrounded
        controller.bubblePosition = BubblePosition(0f, 0f)
        controller.emit(target = BubbleRenderTarget.SERVICE)
        idle()

        // Then the window opens at the dragged position, not at a stale snapshot
        val params = overlayViews().first().layoutParams as WindowManager.LayoutParams
        assertEquals(0, params.x)
        assertEquals(0, params.y)
    }

    @Test
    fun `an untouched bubble opens at the default corner`() {
        // Given no drag has ever happened
        controller.emit(target = BubbleRenderTarget.SERVICE)

        Robolectric.buildService(ChatHeadService::class.java).create()
        idle()

        // Then the window is not at the top-left origin
        val params = overlayViews().first().layoutParams as WindowManager.LayoutParams
        assertTrue(params.x > 0)
        assertTrue(params.y > 0)
    }

    // endregion

    private fun idle() = shadowOf(Looper.getMainLooper()).idle()

    private fun overlayViews(): List<View> {
        val windowManager = RuntimeEnvironment.getApplication().getSystemService<WindowManager>()
        return (shadowOf(windowManager) as ShadowWindowManagerImpl).views.filterIsInstance<ChatHeadView>()
    }

    @Test
    fun `service is created and composes a Glia theme`() {
        val service = Robolectric.buildService(ChatHeadService::class.java).create().get()

        assertEquals(
            R.drawable.ic_person,
            TypedValue().let {
                service.theme.resolveAttribute(R.attr.gliaIconPlaceholder, it, true)
                it.resourceId
            }
        )
    }

    @Test
    fun `service theme carries the Glia defaults for the bubble views`() {
        val service = Robolectric.buildService(ChatHeadService::class.java).create().get()

        fun attr(id: Int): Int = TypedValue().let {
            service.theme.resolveAttribute(id, it, true)
            it.resourceId
        }

        assertEquals(R.color.glia_primary_color, attr(R.attr.gliaBrandPrimaryColor))
        assertEquals(R.style.Application_Glia_Chat_ChatHead, attr(R.attr.chatHeadStyle))
        // proves the wrapper resolved an AppCompat-descendant theme, which the bubble views require
        assertEquals(
            R.style.ThemeOverlay_Glia_Chat_AlertDialog,
            attr(com.google.android.material.R.attr.materialAlertDialogTheme)
        )
    }
}
