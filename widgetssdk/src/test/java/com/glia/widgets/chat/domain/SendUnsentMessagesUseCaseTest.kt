package com.glia.widgets.chat.domain

import com.glia.androidsdk.GliaException
import com.glia.androidsdk.chat.SingleChoiceAttachment
import com.glia.widgets.chat.data.GliaChatRepository
import com.glia.widgets.chat.model.OutgoingMessage
import com.glia.widgets.internal.secureconversations.SecureConversationsRepository
import com.glia.widgets.internal.secureconversations.domain.ManageSecureMessagingStatusUseCase
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class SendUnsentMessagesUseCaseTest {

    private val chatRepository: GliaChatRepository = mockk(relaxUnitFun = true)
    private val secureConversationsRepository: SecureConversationsRepository = mockk(relaxUnitFun = true)
    private val manageSecureMessagingStatusUseCase: ManageSecureMessagingStatusUseCase = mockk()

    private val onSuccess: () -> Unit = mockk(relaxed = true)
    private val onFailure: (GliaException) -> Unit = mockk(relaxed = true)

    private lateinit var useCase: SendUnsentMessagesUseCase

    @Before
    fun setUp() {
        useCase = SendUnsentMessagesUseCase(chatRepository, secureConversationsRepository, manageSecureMessagingStatusUseCase)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `invoke sends text through chat repository when secure messaging is not active`() {
        givenSecureMessaging(false)
        val payload = OutgoingMessage.Text("content")

        useCase(payload, onSuccess, onFailure)

        verify { chatRepository.sendMessage("content", payload.messageId, onSuccess, onFailure) }
        verify(inverse = true) { secureConversationsRepository.sendMessage(any(), any(), any(), any()) }
    }

    @Test
    fun `invoke sends text through secure conversations when secure messaging is active`() {
        givenSecureMessaging(true)
        val payload = OutgoingMessage.Text("content")

        useCase(payload, onSuccess, onFailure)

        verify { secureConversationsRepository.sendMessage("content", payload.messageId, onSuccess, onFailure) }
        verify(inverse = true) { chatRepository.sendMessage(any(), any(), any(), any()) }
    }

    @Test
    fun `invoke sends file through chat repository when secure messaging is not active`() {
        givenSecureMessaging(false)
        val payload = OutgoingMessage.File("file-id")

        useCase(payload, onSuccess, onFailure)

        verify { chatRepository.sendFileAttachment("file-id", onSuccess, onFailure) }
        verify(inverse = true) { secureConversationsRepository.sendFileAttachment(any(), any(), any()) }
    }

    @Test
    fun `invoke sends file through secure conversations when secure messaging is active`() {
        givenSecureMessaging(true)
        val payload = OutgoingMessage.File("file-id")

        useCase(payload, onSuccess, onFailure)

        verify { secureConversationsRepository.sendFileAttachment("file-id", onSuccess, onFailure) }
        verify(inverse = true) { chatRepository.sendFileAttachment(any(), any(), any()) }
    }

    @Test
    fun `invoke sends single choice through chat repository when secure messaging is not active`() {
        givenSecureMessaging(false)
        val attachment: SingleChoiceAttachment = mockk()
        val payload = OutgoingMessage.SingleChoice(attachment)

        useCase(payload, onSuccess, onFailure)

        verify { chatRepository.sendSingleChoiceAttachment(attachment, payload.messageId, onSuccess, onFailure) }
        verify(inverse = true) { secureConversationsRepository.sendSingleChoiceAttachment(any(), any(), any(), any()) }
    }

    @Test
    fun `invoke sends single choice through secure conversations when secure messaging is active`() {
        givenSecureMessaging(true)
        val attachment: SingleChoiceAttachment = mockk()
        val payload = OutgoingMessage.SingleChoice(attachment)

        useCase(payload, onSuccess, onFailure)

        verify { secureConversationsRepository.sendSingleChoiceAttachment(attachment, payload.messageId, onSuccess, onFailure) }
        verify(inverse = true) { chatRepository.sendSingleChoiceAttachment(any(), any(), any(), any()) }
    }

    private fun givenSecureMessaging(isActive: Boolean) {
        every { manageSecureMessagingStatusUseCase.shouldUseSecureMessagingEndpoints } returns isActive
    }
}
