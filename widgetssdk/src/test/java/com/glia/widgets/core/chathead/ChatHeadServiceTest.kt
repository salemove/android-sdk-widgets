package com.glia.widgets.core.chathead

import android.mock
import android.os.Build
import android.unMock
import com.glia.widgets.di.ControllerFactory
import com.glia.widgets.di.Dependencies
import com.glia.widgets.di.UseCaseFactory
import com.glia.widgets.engagement.domain.IsCurrentEngagementCallVisualizerUseCase
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.ResourceProvider
import com.glia.widgets.locale.LocaleProvider
import com.glia.widgets.locale.StringKeyPair
import com.glia.widgets.view.head.ChatHeadContract
import com.glia.widgets.view.head.ChatHeadPosition
import io.reactivex.rxjava3.core.Observable
import org.junit.After
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
}
