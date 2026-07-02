package com.glia.widgets.chat.domain

import android.net.Uri
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.chat.SingleChoiceAttachment
import com.glia.androidsdk.engagement.EngagementFile
import com.glia.widgets.chat.data.GliaChatRepository
import com.glia.widgets.chat.model.OutgoingMessage
import com.glia.widgets.chat.model.VisitorAttachmentItem
import com.glia.widgets.chat.model.VisitorMessageItem
import com.glia.widgets.engagement.domain.IsOperatorPresentUseCase
import com.glia.widgets.internal.fileupload.FileAttachmentRepository
import com.glia.widgets.internal.fileupload.model.LocalAttachment
import com.glia.widgets.internal.secureconversations.SecureConversationsRepository
import com.glia.widgets.internal.secureconversations.domain.ManageSecureMessagingStatusUseCase
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GliaSendMessageUseCaseTest {

    private val chatRepository: GliaChatRepository = mockk(relaxUnitFun = true)
    private val fileAttachmentRepository: FileAttachmentRepository = mockk(relaxUnitFun = true)
    private val isOperatorPresentUseCase: IsOperatorPresentUseCase = mockk()
    private val secureConversationsRepository: SecureConversationsRepository = mockk(relaxUnitFun = true)
    private val manageSecureMessagingStatusUseCase: ManageSecureMessagingStatusUseCase = mockk()
    private val listener: GliaSendMessageUseCase.Listener = mockk(relaxUnitFun = true)

    private lateinit var useCase: GliaSendMessageUseCase

    @Before
    fun setUp() {
        useCase = GliaSendMessageUseCase(
            chatRepository,
            fileAttachmentRepository,
            isOperatorPresentUseCase,
            secureConversationsRepository,
            manageSecureMessagingStatusUseCase
        )
        every { fileAttachmentRepository.getReadyToSendFileAttachments() } returns emptyList()
        givenOperatorOnline(false)
        givenSecureMessaging(false)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `execute validates message and sends text through chat repository when operator is online`() {
        givenOperatorOnline(true)
        val itemSlot = slot<VisitorMessageItem>()
        val payloadSlot = slot<OutgoingMessage>()
        every { listener.onMessagePrepared(capture(itemSlot), capture(payloadSlot)) } returns Unit

        useCase.execute(MESSAGE, listener)

        verify { listener.onMessageValidated() }
        val payload = payloadSlot.captured as OutgoingMessage.Text
        assertEquals(MESSAGE, payload.content)
        assertEquals(payload.messageId, itemSlot.captured.id)
        verify { chatRepository.sendMessage(MESSAGE, payload.messageId, any(), any()) }
        verify(inverse = true) { secureConversationsRepository.sendMessage(any(), any(), any(), any()) }
    }

    @Test
    fun `execute sends text through secure conversations when secure messaging is active`() {
        givenSecureMessaging(true)

        useCase.execute(MESSAGE, listener)

        verify { secureConversationsRepository.sendMessage(eq(MESSAGE), any(), any(), any()) }
        verify(inverse = true) { chatRepository.sendMessage(any(), any(), any(), any()) }
    }

    @Test
    fun `execute prefers live chat over secure messaging when operator is online`() {
        givenOperatorOnline(true)
        givenSecureMessaging(true)

        useCase.execute(MESSAGE, listener)

        verify { chatRepository.sendMessage(eq(MESSAGE), any(), any(), any()) }
        verify(inverse = true) { secureConversationsRepository.sendMessage(any(), any(), any(), any()) }
    }

    @Test
    fun `execute notifies errorOperatorOffline when neither operator nor secure messaging is available`() {
        useCase.execute(MESSAGE, listener)

        verify { listener.errorOperatorOffline(any()) }
        verify(inverse = true) { chatRepository.sendMessage(any(), any(), any(), any()) }
        verify(inverse = true) { secureConversationsRepository.sendMessage(any(), any(), any(), any()) }
    }

    @Test
    fun `execute does nothing when message is blank and there are no attachments`() {
        useCase.execute("", listener)

        verify(inverse = true) { listener.onMessageValidated() }
        verify(inverse = true) { listener.onMessagePrepared(any(), any()) }
        verify(inverse = true) { listener.errorOperatorOffline(any()) }
    }

    @Test
    fun `execute notifies messageSent when Core reports text send success`() {
        givenOperatorOnline(true)
        val payloadSlot = slot<OutgoingMessage>()
        every { listener.onMessagePrepared(any(), capture(payloadSlot)) } returns Unit
        val onSuccessSlot = slot<() -> Unit>()
        every { chatRepository.sendMessage(any(), any(), capture(onSuccessSlot), any()) } returns Unit

        useCase.execute(MESSAGE, listener)
        onSuccessSlot.captured.invoke()

        verify { listener.messageSent(payloadSlot.captured.messageId) }
    }

    @Test
    fun `execute notifies error when Core reports text send failure`() {
        givenOperatorOnline(true)
        val exception = GliaException("error", GliaException.Cause.INTERNAL_ERROR)
        val payloadSlot = slot<OutgoingMessage>()
        every { listener.onMessagePrepared(any(), capture(payloadSlot)) } returns Unit
        val onFailureSlot = slot<(GliaException) -> Unit>()
        every { chatRepository.sendMessage(any(), any(), any(), capture(onFailureSlot)) } returns Unit

        useCase.execute(MESSAGE, listener)
        onFailureSlot.captured.invoke(exception)

        verify { listener.error(exception, payloadSlot.captured.messageId) }
    }

    @Test
    fun `execute prepares and sends each attachment as a separate file message`() {
        givenOperatorOnline(true)
        val attachments = listOf(localAttachment(FILE_ID), localAttachment(FILE_ID_1))
        every { fileAttachmentRepository.getReadyToSendFileAttachments() } returns attachments
        val itemSlots = mutableListOf<VisitorAttachmentItem>()
        val payloadSlots = mutableListOf<OutgoingMessage>()
        every { listener.onAttachmentPrepared(capture(itemSlots), capture(payloadSlots)) } returns Unit

        useCase.execute("", listener)

        assertEquals(listOf(FILE_ID, FILE_ID_1), itemSlots.map { it.id })
        assertEquals(listOf(FILE_ID, FILE_ID_1), payloadSlots.map { it.messageId })
        verify { chatRepository.sendFileAttachment(FILE_ID, any(), any()) }
        verify { chatRepository.sendFileAttachment(FILE_ID_1, any(), any()) }
        verify { fileAttachmentRepository.detachFiles(attachments) }
    }

    @Test
    fun `execute sends attachments through secure conversations when secure messaging is active`() {
        givenSecureMessaging(true)
        every { fileAttachmentRepository.getReadyToSendFileAttachments() } returns listOf(localAttachment(FILE_ID))

        useCase.execute("", listener)

        verify { secureConversationsRepository.sendFileAttachment(FILE_ID, any(), any()) }
        verify(inverse = true) { chatRepository.sendFileAttachment(any(), any(), any()) }
    }

    @Test
    fun `execute notifies errorOperatorOffline for attachments when sending is not possible`() {
        every { fileAttachmentRepository.getReadyToSendFileAttachments() } returns listOf(localAttachment(FILE_ID))

        useCase.execute("", listener)

        verify { listener.errorOperatorOffline(FILE_ID) }
        verify(inverse = true) { chatRepository.sendFileAttachment(any(), any(), any()) }
        verify(inverse = true) { secureConversationsRepository.sendFileAttachment(any(), any(), any()) }
    }

    @Test
    fun `execute skips attachments that have no engagement file`() {
        givenOperatorOnline(true)
        every { fileAttachmentRepository.getReadyToSendFileAttachments() } returns listOf(localAttachment(fileId = null))

        useCase.execute("", listener)

        verify(inverse = true) { listener.onAttachmentPrepared(any(), any()) }
        verify(inverse = true) { chatRepository.sendFileAttachment(any(), any(), any()) }
    }

    @Test
    fun `execute sends single choice attachment through chat repository when operator is online`() {
        givenOperatorOnline(true)
        val singleChoiceAttachment: SingleChoiceAttachment = mockk {
            every { selectedOptionText } returns "selected"
        }
        val itemSlot = slot<VisitorMessageItem>()
        val payloadSlot = slot<OutgoingMessage>()
        every { listener.onMessagePrepared(capture(itemSlot), capture(payloadSlot)) } returns Unit

        useCase.execute(singleChoiceAttachment, listener)

        assertEquals("selected", itemSlot.captured.message)
        verify { chatRepository.sendSingleChoiceAttachment(singleChoiceAttachment, payloadSlot.captured.messageId, any(), any()) }
    }

    @Test
    fun `execute prefers secure conversations for single choice attachment when secure messaging is active`() {
        givenOperatorOnline(true)
        givenSecureMessaging(true)
        val singleChoiceAttachment: SingleChoiceAttachment = mockk {
            every { selectedOptionText } returns "selected"
        }

        useCase.execute(singleChoiceAttachment, listener)

        verify { secureConversationsRepository.sendSingleChoiceAttachment(eq(singleChoiceAttachment), any(), any(), any()) }
        verify(inverse = true) { chatRepository.sendSingleChoiceAttachment(any(), any(), any(), any()) }
    }

    @Test
    fun `execute notifies errorOperatorOffline for single choice attachment when sending is not possible`() {
        val singleChoiceAttachment: SingleChoiceAttachment = mockk {
            every { selectedOptionText } returns "selected"
        }

        useCase.execute(singleChoiceAttachment, listener)

        verify { listener.errorOperatorOffline(any()) }
    }

    private fun givenOperatorOnline(isOnline: Boolean) {
        every { isOperatorPresentUseCase() } returns isOnline
    }

    private fun givenSecureMessaging(isActive: Boolean) {
        every { manageSecureMessagingStatusUseCase.shouldUseSecureMessagingEndpoints } returns isActive
    }

    private fun localAttachment(fileId: String?): LocalAttachment {
        val engagementFile: EngagementFile? = fileId?.let { mockk { every { id } returns it } }
        return LocalAttachment(
            uri = mockk<Uri>(),
            mimeType = "image/png",
            displayName = "display-name",
            size = 10L,
            attachmentStatus = LocalAttachment.Status.READY_TO_SEND,
            engagementFile = engagementFile
        )
    }

    private companion object {
        const val MESSAGE = "message"
        const val FILE_ID = "file-id"
        const val FILE_ID_1 = "file-id-1"
    }
}
