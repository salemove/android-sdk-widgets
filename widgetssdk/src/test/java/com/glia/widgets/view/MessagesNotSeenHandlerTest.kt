package com.glia.widgets.view

import com.glia.widgets.chat.domain.GliaOnMessageUseCase
import com.glia.widgets.internal.engagement.domain.model.ChatMessageInternal
import com.glia.widgets.helper.Logger
import io.mockk.*
import io.reactivex.rxjava3.core.Observable
import org.junit.Before
import org.junit.Test

class MessagesNotSeenHandlerTest {

    private lateinit var gliaOnMessageUseCase: GliaOnMessageUseCase
    private lateinit var messagesNotSeenHandlerListener: MessagesNotSeenHandler.MessagesNotSeenHandlerListener
    private lateinit var messagesNotSeenHandler: MessagesNotSeenHandler

    @Before
    fun setUp() {
        Logger.setIsDebug(false)
        gliaOnMessageUseCase = mockk()
        messagesNotSeenHandlerListener = mockk(relaxed = true)
        messagesNotSeenHandler = MessagesNotSeenHandler(gliaOnMessageUseCase)
        messagesNotSeenHandler.addListener(messagesNotSeenHandlerListener)
    }

    @Test
    fun `init invokes gliaOnMessageUseCase`() {
        every { gliaOnMessageUseCase.invoke() } returns Observable.empty()
        messagesNotSeenHandler.init()
        verify { gliaOnMessageUseCase.invoke() }
    }

    @Test
    fun `chatOnBackClicked invokes onNewCount with zero`() {
        messagesNotSeenHandler.chatOnBackClicked()
        verify { messagesNotSeenHandlerListener.onNewCount(0) }
    }

    @Test
    fun `callChatButtonClicked triggers onNewCount with zero`() {
        messagesNotSeenHandler.callChatButtonClicked()
        verify { messagesNotSeenHandlerListener.onNewCount(0) }
    }

    @Test
    fun `chatUpgradeOfferAccepted triggers onNewCount with zero`() {
        messagesNotSeenHandler.chatUpgradeOfferAccepted()
        verify { messagesNotSeenHandlerListener.onNewCount(0) }
    }

    @Test
    fun `addListener triggers onNewCount with zero`() {
        messagesNotSeenHandler.addListener(messagesNotSeenHandlerListener)
        verify { messagesNotSeenHandlerListener.onNewCount(0) }
    }

    @Test
    fun `onMessage triggers onNewCount after init`() {
        val message = mockk<ChatMessageInternal>()
        every { message.isNotVisitor } returns true
        every { message.chatMessage.id } returns "1"
        every { gliaOnMessageUseCase.invoke() } returns Observable.empty()

        messagesNotSeenHandler.init()
        messagesNotSeenHandler.onMessage(message)

        verify { messagesNotSeenHandlerListener.onNewCount(1) }
    }

    @Test
    fun `onDestroy triggers onNewCount when called`() {
        messagesNotSeenHandler.onDestroy()
        verify { messagesNotSeenHandlerListener.onNewCount(0) }
    }
}
