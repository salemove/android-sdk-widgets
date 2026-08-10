package com.glia.widgets.view.head.controller

import com.glia.widgets.engagement.domain.CurrentOperatorUseCase
import com.glia.widgets.engagement.domain.EngagementStateUseCase
import com.glia.widgets.engagement.domain.VisitorMediaUseCase
import com.glia.widgets.helper.Logger
import com.glia.widgets.internal.chathead.domain.DisplayBubbleOutsideAppUseCase
import com.glia.widgets.internal.chathead.domain.ResolveChatHeadNavigationUseCase
import com.glia.widgets.view.MessagesNotSeenHandler
import com.glia.widgets.view.head.ChatHeadContract
import com.glia.widgets.view.head.ChatHeadPosition
import io.reactivex.rxjava3.core.Flowable
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class ServiceChatHeadControllerTest {
    private lateinit var displayBubbleOutsideAppUseCase: DisplayBubbleOutsideAppUseCase
    private lateinit var resolveChatHeadNavigationUseCase: ResolveChatHeadNavigationUseCase
    private lateinit var messagesNotSeenHandler: MessagesNotSeenHandler
    private lateinit var engagementStateUseCase: EngagementStateUseCase
    private lateinit var currentOperatorUseCase: CurrentOperatorUseCase
    private lateinit var visitorMediaUseCase: VisitorMediaUseCase

    private lateinit var chatHeadView: ChatHeadContract.View

    private lateinit var controller: ServiceChatHeadController

    @Before
    fun setUp() {
        Logger.setIsDebug(false)
        displayBubbleOutsideAppUseCase = mock()
        resolveChatHeadNavigationUseCase = mock()
        messagesNotSeenHandler = mock()
        engagementStateUseCase = mock {
            on { invoke() } doReturn Flowable.empty()
        }
        currentOperatorUseCase = mock {
            on { invoke() } doReturn Flowable.empty()
        }
        visitorMediaUseCase = mock {
            on { onHoldState } doReturn Flowable.empty()
        }

        chatHeadView = mock()

        controller = ServiceChatHeadController(
            displayBubbleOutsideAppUseCase = displayBubbleOutsideAppUseCase,
            resolveChatHeadNavigationUseCase = resolveChatHeadNavigationUseCase,
            messagesNotSeenHandler = messagesNotSeenHandler,
            _chatHeadPosition = ChatHeadPosition(),
            engagementStateUseCase = engagementStateUseCase,
            currentOperatorUseCase = currentOperatorUseCase,
            visitorMediaUseCase = visitorMediaUseCase
        )
    }

    @Test
    fun `onSetChatHeadView registers the view so it receives updates`() {
        controller.onSetChatHeadView(chatHeadView)

        controller.updateChatHeadView()

        verify(chatHeadView).showUnreadMessageCount(0)
    }

    @Test
    fun `onClearChatHeadView releases the view so the service is not leaked`() {
        controller.onSetChatHeadView(chatHeadView)

        controller.onClearChatHeadView(chatHeadView)

        controller.updateChatHeadView()
        verify(chatHeadView, never()).showUnreadMessageCount(0)
    }

    @Test
    fun `onClearChatHeadView keeps the view registered by a newer service instance`() {
        val newerView = mock<ChatHeadContract.View>()
        controller.onSetChatHeadView(chatHeadView)
        controller.onSetChatHeadView(newerView)

        controller.onClearChatHeadView(chatHeadView)

        controller.updateChatHeadView()
        verify(newerView).showUnreadMessageCount(0)
    }
}
