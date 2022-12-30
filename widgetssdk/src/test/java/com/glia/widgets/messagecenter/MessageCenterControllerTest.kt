package com.glia.widgets.messagecenter

import com.glia.androidsdk.GliaException
import com.glia.widgets.core.secureconversations.domain.IsMessageCenterAvailableUseCase
import com.glia.widgets.core.secureconversations.domain.SendSecureMessageUseCase
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito


internal class MessageCenterControllerTest {
    private lateinit var messageCenterController: MessageCenterController
    private lateinit var sendSecureMessageUseCase: SendSecureMessageUseCase
    private lateinit var isMessageCenterAvailableUseCase: IsMessageCenterAvailableUseCase
    private lateinit var viewContract: MessageCenterContract.View

    @Before
    fun setUp() {
        sendSecureMessageUseCase = Mockito.mock(SendSecureMessageUseCase::class.java)
        isMessageCenterAvailableUseCase = Mockito.mock(
            IsMessageCenterAvailableUseCase::class.java
        )
        messageCenterController =
            MessageCenterController(sendSecureMessageUseCase, isMessageCenterAvailableUseCase)
        viewContract = Mockito.mock(MessageCenterContract.View::class.java)
        messageCenterController.setView(viewContract)
    }

    @Test
    fun handleMessageSendResult_CallsNavigateToMessaging_WhenErrorNull() {
        messageCenterController.handleSendMessageResult(null)
        Mockito.verify(viewContract, Mockito.times(1)).navigateToMessaging()
    }

    @Test
    fun handleMessageSendResult_CallsNavigateToMessaging_WhenAuthError() {
        val gliaException = GliaException("Message", GliaException.Cause.AUTHENTICATION_ERROR)
        messageCenterController.handleSendMessageResult(gliaException)
        Mockito.verify(viewContract, Mockito.times(1)).showMessageCenterUnavailableDialog()
    }

    @Test
    fun handleMessageSendResult_CallsNavigateToMessaging_WhenInternalError() {
        val gliaException = GliaException("Message", GliaException.Cause.INTERNAL_ERROR)
        messageCenterController.handleSendMessageResult(gliaException)
        Mockito.verify(viewContract, Mockito.times(1)).showUnexpectedErrorDialog()
    }

    @Test
    fun handleMessageSendResult_CallsNavigateToMessaging_WhenOtherError() {
        val gliaException = GliaException("Message", GliaException.Cause.INVALID_INPUT)
        messageCenterController.handleSendMessageResult(gliaException)
        Mockito.verify(viewContract, Mockito.times(1)).showUnexpectedErrorDialog()
    }
}
