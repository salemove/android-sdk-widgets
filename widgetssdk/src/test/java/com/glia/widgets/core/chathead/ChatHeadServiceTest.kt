package com.glia.widgets.core.chathead

import android.mock
import android.os.Build
import android.unMock
import android.util.TypedValue
import com.glia.widgets.R
import com.glia.widgets.di.ControllerFactory
import com.glia.widgets.di.Dependencies
import com.glia.widgets.di.UseCaseFactory
import com.glia.widgets.engagement.domain.IsCurrentEngagementCallVisualizerUseCase
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.ResourceProvider
import com.glia.widgets.helper.wrapWithGliaTheme
import com.glia.widgets.locale.LocaleProvider
import com.glia.widgets.locale.StringKeyPair
import com.glia.widgets.view.head.ChatHeadContract
import com.glia.widgets.view.head.ChatHeadPosition
import io.reactivex.rxjava3.core.Observable
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowApplication

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

    private lateinit var chatHeadController: ChatHeadContract.Controller

    @Before
    fun setUp() {
        Logger.mock()
        val application = RuntimeEnvironment.getApplication()

        Dependencies.resourceProvider = ResourceProvider(application)

        val localeProvider = mock<LocaleProvider>()
        whenever(localeProvider.getLocaleObservable()) doReturn Observable.never()
        whenever(localeProvider.getString(any<Int>(), any<List<StringKeyPair>>())) doReturn "Back to the Engagement. Floating Button."
        Dependencies.localeProvider = localeProvider

        chatHeadController = mock {
            on { chatHeadPosition } doReturn ChatHeadPosition()
        }
        val controllerFactory = mock<ControllerFactory>()
        whenever(controllerFactory.chatHeadController) doReturn chatHeadController
        Dependencies.controllerFactory = controllerFactory

        val useCaseFactory = mock<UseCaseFactory>()
        val isCallVisualizerUseCase = mock<IsCurrentEngagementCallVisualizerUseCase>()
        whenever(useCaseFactory.isCurrentEngagementCallVisualizer) doReturn isCallVisualizerUseCase
        Dependencies.useCaseFactory = useCaseFactory

        ShadowApplication().grantPermissions(android.Manifest.permission.SYSTEM_ALERT_WINDOW)
    }

    @After
    fun tearDown() {
        Logger.unMock()
    }

    @Test
    fun `onCreate registers the chat head view with the controller`() {
        Robolectric.buildService(ChatHeadService::class.java).create()

        verify(chatHeadController).onSetChatHeadView(any())
    }

    @Test
    fun `onDestroy releases the registered view so the singleton controller does not leak it`() {
        val serviceController = Robolectric.buildService(ChatHeadService::class.java).create()
        val viewCaptor = argumentCaptor<ChatHeadContract.View>()
        verify(chatHeadController).onSetChatHeadView(viewCaptor.capture())

        serviceController.destroy()

        verify(chatHeadController).onClearChatHeadView(viewCaptor.firstValue)
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
