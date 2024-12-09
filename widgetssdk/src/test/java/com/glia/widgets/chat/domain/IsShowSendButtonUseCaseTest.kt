package com.glia.widgets.chat.domain

import com.glia.widgets.core.fileupload.EngagementFileAttachmentRepository
import com.glia.widgets.core.secureconversations.domain.ManageSecureMessagingStatusUseCase
import com.glia.widgets.engagement.domain.IsQueueingOrLiveEngagementUseCase
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class IsShowSendButtonUseCaseTest {
    private lateinit var isQueueingOrLiveEngagementUseCase: IsQueueingOrLiveEngagementUseCase
    private lateinit var fileAttachmentRepository: EngagementFileAttachmentRepository
    private lateinit var manageSecureMessagingStatusUseCase: ManageSecureMessagingStatusUseCase

    private lateinit var useCase: IsShowSendButtonUseCase

    @Before
    fun setUp() {
        isQueueingOrLiveEngagementUseCase = mock()
        fileAttachmentRepository = mock()
        manageSecureMessagingStatusUseCase = mock()

        useCase = IsShowSendButtonUseCase(
            isQueueingOrLiveEngagementUseCase,
            fileAttachmentRepository,
            manageSecureMessagingStatusUseCase
        )
    }

    @Test
    fun `invoke returns false if ongoing engagement and the message empty and files are not ready to send`() {
        whenever(isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement) doReturn true
        whenever(fileAttachmentRepository.getReadyToSendFileAttachments) doReturn emptyList()

        val result = useCase("")

        assertFalse(result)
    }

    @Test
    fun `invoke returns true if ongoing engagement and the message is not empty and files are not ready to send`() {
        whenever(isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement) doReturn true
        whenever(fileAttachmentRepository.getReadyToSendFileAttachments) doReturn emptyList()

        val result = useCase("test")

        assertTrue(result)
    }

    @Test
    fun `invoke returns true if ongoing engagement and the message is empty and files are ready to send`() {
        whenever(isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement) doReturn true
        whenever(fileAttachmentRepository.getReadyToSendFileAttachments) doReturn listOf(mock())

        val result = useCase("")

        assertTrue(result)
    }

    @Test
    fun `invoke returns true if ongoing engagement and the message is not empty and files are ready to send`() {
        whenever(isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement) doReturn true
        whenever(fileAttachmentRepository.getReadyToSendFileAttachments) doReturn listOf(mock())

        val result = useCase("test")

        assertTrue(result)
    }

    @Test
    fun `invoke returns false if no ongoing engagement and files are ready to send`() {
        whenever(isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement) doReturn false
        whenever(fileAttachmentRepository.getReadyToSendFileAttachments) doReturn listOf(mock())

        val result = useCase("")

        assertFalse(result)
    }

    @Test
    fun `invoke returns false if secure engagement and the message empty and files are not ready to send`() {
        whenever(manageSecureMessagingStatusUseCase.shouldBehaveAsSecureMessaging) doReturn true
        whenever(fileAttachmentRepository.getReadyToSendFileAttachments) doReturn emptyList()

        val result = useCase("")

        assertFalse(result)
    }

    @Test
    fun `invoke returns true if secure engagement and the message is not empty and files are not ready to send`() {
        whenever(manageSecureMessagingStatusUseCase.shouldBehaveAsSecureMessaging) doReturn true
        whenever(fileAttachmentRepository.getReadyToSendFileAttachments) doReturn emptyList()

        val result = useCase("test")

        assertTrue(result)
    }

    @Test
    fun `invoke returns true if secure engagement and the message is empty and files are ready to send`() {
        whenever(manageSecureMessagingStatusUseCase.shouldBehaveAsSecureMessaging) doReturn true
        whenever(fileAttachmentRepository.getReadyToSendFileAttachments) doReturn listOf(mock())

        val result = useCase("")

        assertTrue(result)
    }

    @Test
    fun `invoke returns true if secure engagement and the message is not empty and files are ready to send`() {
        whenever(manageSecureMessagingStatusUseCase.shouldBehaveAsSecureMessaging) doReturn true
        whenever(fileAttachmentRepository.getReadyToSendFileAttachments) doReturn listOf(mock())

        val result = useCase("test")

        assertTrue(result)
    }
}
