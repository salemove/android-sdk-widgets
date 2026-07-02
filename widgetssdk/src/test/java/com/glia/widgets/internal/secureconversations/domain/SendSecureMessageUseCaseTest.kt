package com.glia.widgets.internal.secureconversations.domain

import android.net.Uri
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.engagement.EngagementFile
import com.glia.widgets.internal.fileupload.FileAttachmentRepository
import com.glia.widgets.internal.fileupload.model.LocalAttachment
import com.glia.widgets.internal.secureconversations.SecureConversationsRepository
import com.glia.widgets.internal.secureconversations.SendMessageRepository
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class SendSecureMessageUseCaseTest {

    private val sendMessageRepository: SendMessageRepository = mockk(relaxUnitFun = true)
    private val secureConversationsRepository: SecureConversationsRepository = mockk(relaxUnitFun = true)
    private val fileAttachmentRepository: FileAttachmentRepository = mockk(relaxUnitFun = true)

    private val onSuccess: () -> Unit = mockk(relaxed = true)
    private val onFailure: (GliaException) -> Unit = mockk(relaxed = true)

    private lateinit var useCase: SendSecureMessageUseCase

    @Before
    fun setUp() {
        useCase = SendSecureMessageUseCase(sendMessageRepository, secureConversationsRepository, fileAttachmentRepository)
        every { sendMessageRepository.value } returns MESSAGE
        every { fileAttachmentRepository.getReadyToSendFileAttachments() } returns emptyList()
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `invoke sends plain message when there are no attachments`() {
        useCase(onSuccess, onFailure)

        verify { secureConversationsRepository.sendMessage(eq(MESSAGE), any(), eq(onSuccess), eq(onFailure)) }
        verify(inverse = true) { secureConversationsRepository.sendMessageWithAttachments(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `invoke sends message with attachment file ids when attachments are ready to send`() {
        val attachments = listOf(localAttachment(FILE_ID), localAttachment(FILE_ID_1))
        every { fileAttachmentRepository.getReadyToSendFileAttachments() } returns attachments

        useCase(onSuccess, onFailure)

        verify {
            secureConversationsRepository.sendMessageWithAttachments(eq(MESSAGE), eq(listOf(FILE_ID, FILE_ID_1)), any(), any(), any())
        }
        verify(inverse = true) { secureConversationsRepository.sendMessage(any(), any(), any(), any()) }
    }

    @Test
    fun `invoke resets composed message and detaches files before reporting success for attachment sends`() {
        val attachments = listOf(localAttachment(FILE_ID))
        every { fileAttachmentRepository.getReadyToSendFileAttachments() } returns attachments
        val sdkOnSuccessSlot = slot<() -> Unit>()
        every {
            secureConversationsRepository.sendMessageWithAttachments(any(), any(), any(), capture(sdkOnSuccessSlot), any())
        } returns Unit

        useCase(onSuccess, onFailure)
        sdkOnSuccessSlot.captured.invoke()

        verify { sendMessageRepository.reset() }
        verify { fileAttachmentRepository.detachFiles(attachments) }
        verify { onSuccess.invoke() }
    }

    @Test
    fun `invoke propagates failure without resetting state for attachment sends`() {
        val attachments = listOf(localAttachment(FILE_ID))
        val exception = GliaException("error", GliaException.Cause.INTERNAL_ERROR)
        every { fileAttachmentRepository.getReadyToSendFileAttachments() } returns attachments
        val sdkOnFailureSlot = slot<(GliaException) -> Unit>()
        every {
            secureConversationsRepository.sendMessageWithAttachments(any(), any(), any(), any(), capture(sdkOnFailureSlot))
        } returns Unit

        useCase(onSuccess, onFailure)
        sdkOnFailureSlot.captured.invoke(exception)

        verify { onFailure.invoke(exception) }
        verify(inverse = true) { sendMessageRepository.reset() }
        verify(inverse = true) { fileAttachmentRepository.detachFiles(any()) }
        verify(inverse = true) { onSuccess.invoke() }
    }

    private fun localAttachment(fileId: String): LocalAttachment {
        val engagementFile: EngagementFile = mockk { every { id } returns fileId }
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
