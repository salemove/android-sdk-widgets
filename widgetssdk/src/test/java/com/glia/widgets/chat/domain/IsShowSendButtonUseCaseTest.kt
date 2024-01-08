package com.glia.widgets.chat.domain

import com.glia.widgets.core.fileupload.FileAttachmentRepository
import com.glia.widgets.core.secureconversations.domain.IsSecureEngagementUseCase
import com.glia.widgets.engagement.domain.IsQueueingOrEngagementUseCase
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class IsShowSendButtonUseCaseTest {
    private lateinit var isQueueingOrEngagementUseCase: IsQueueingOrEngagementUseCase
    private lateinit var fileAttachmentRepository: FileAttachmentRepository
    private lateinit var isSecureEngagementUseCase: IsSecureEngagementUseCase

    private lateinit var useCase: IsShowSendButtonUseCase

    @Before
    fun setUp() {
        isQueueingOrEngagementUseCase = mock()
        fileAttachmentRepository = mock()
        isSecureEngagementUseCase = mock()

        useCase = IsShowSendButtonUseCase(
            isQueueingOrEngagementUseCase,
            fileAttachmentRepository,
            isSecureEngagementUseCase
        )
    }

    @Test
    fun `invoke returns false if ongoing engagement and the message empty and files are not ready to send`() {
        whenever(isQueueingOrEngagementUseCase.hasOngoingEngagement) doReturn true
        whenever(fileAttachmentRepository.readyToSendFileAttachments) doReturn emptyList()

        val result = useCase("")

        assertFalse(result)
    }

    @Test
    fun `invoke returns true if ongoing engagement and the message is not empty and files are not ready to send`() {
        whenever(isQueueingOrEngagementUseCase.hasOngoingEngagement) doReturn true
        whenever(fileAttachmentRepository.readyToSendFileAttachments) doReturn emptyList()

        val result = useCase("test")

        assertTrue(result)
    }

    @Test
    fun `invoke returns true if ongoing engagement and the message is empty and files are ready to send`() {
        whenever(isQueueingOrEngagementUseCase.hasOngoingEngagement) doReturn true
        whenever(fileAttachmentRepository.readyToSendFileAttachments) doReturn listOf(mock())

        val result = useCase("")

        assertTrue(result)
    }

    @Test
    fun `invoke returns true if ongoing engagement and the message is not empty and files are ready to send`() {
        whenever(isQueueingOrEngagementUseCase.hasOngoingEngagement) doReturn true
        whenever(fileAttachmentRepository.readyToSendFileAttachments) doReturn listOf(mock())

        val result = useCase("test")

        assertTrue(result)
    }

    @Test
    fun `invoke returns false if no ongoing engagement and files are ready to send`() {
        whenever(isQueueingOrEngagementUseCase.hasOngoingEngagement) doReturn false
        whenever(fileAttachmentRepository.readyToSendFileAttachments) doReturn listOf(mock())

        val result = useCase("")

        assertFalse(result)
    }

    @Test
    fun `invoke returns false if secure engagement and the message empty and files are not ready to send`() {
        whenever(isSecureEngagementUseCase.invoke()) doReturn true
        whenever(fileAttachmentRepository.readyToSendFileAttachments) doReturn emptyList()

        val result = useCase("")

        assertFalse(result)
    }

    @Test
    fun `invoke returns true if secure engagement and the message is not empty and files are not ready to send`() {
        whenever(isSecureEngagementUseCase.invoke()) doReturn true
        whenever(fileAttachmentRepository.readyToSendFileAttachments) doReturn emptyList()

        val result = useCase("test")

        assertTrue(result)
    }

    @Test
    fun `invoke returns true if secure engagement and the message is empty and files are ready to send`() {
        whenever(isSecureEngagementUseCase.invoke()) doReturn true
        whenever(fileAttachmentRepository.readyToSendFileAttachments) doReturn listOf(mock())

        val result = useCase("")

        assertTrue(result)
    }

    @Test
    fun `invoke returns true if secure engagement and the message is not empty and files are ready to send`() {
        whenever(isSecureEngagementUseCase.invoke()) doReturn true
        whenever(fileAttachmentRepository.readyToSendFileAttachments) doReturn listOf(mock())

        val result = useCase("test")

        assertTrue(result)
    }
}
