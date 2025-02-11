package com.glia.widgets.core.fileupload.domain

import com.glia.widgets.core.engagement.exception.EngagementMissingException
import com.glia.widgets.core.fileupload.FileAttachmentRepository
import com.glia.widgets.core.fileupload.exception.RemoveBeforeReUploadingException
import com.glia.widgets.core.fileupload.exception.SupportedFileCountExceededException
import com.glia.widgets.core.fileupload.exception.SupportedFileSizeExceededException
import com.glia.widgets.core.fileupload.model.LocalAttachment
import com.glia.widgets.core.secureconversations.domain.ManageSecureMessagingStatusUseCase
import com.glia.widgets.engagement.domain.IsQueueingOrLiveEngagementUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class AddFileToAttachmentAndUploadUseCaseTest {

    private lateinit var fileAttachmentRepository: FileAttachmentRepository
    private lateinit var isQueueingOrLiveEngagementUseCase: IsQueueingOrLiveEngagementUseCase
    private lateinit var manageSecureMessagingStatusUseCase: ManageSecureMessagingStatusUseCase
    private lateinit var subjectUnderTest: AddFileToAttachmentAndUploadUseCase

    @Before
    fun setUp() {
        isQueueingOrLiveEngagementUseCase = mockk(relaxUnitFun = true)
        fileAttachmentRepository = mockk(relaxUnitFun = true)
        manageSecureMessagingStatusUseCase = mockk(relaxUnitFun = true)
        subjectUnderTest = AddFileToAttachmentAndUploadUseCase(
            isQueueingOrLiveEngagementUseCase,
            fileAttachmentRepository,
            manageSecureMessagingStatusUseCase
        )
    }

    @Test
    fun `invoke calls onError with RemoveBeforeReUploadingException when file is already attached`() {
        val localAttachment = mockk<LocalAttachment>()
        val listener = mockk<AddFileToAttachmentAndUploadUseCase.Listener>(relaxed = true)
        every { fileAttachmentRepository.isFileAttached(localAttachment.uri) } returns true

        subjectUnderTest.invoke(localAttachment, listener)

        verify { listener.onError(any<RemoveBeforeReUploadingException>()) }
    }

    @Test
    fun `invoke calls onError with EngagementMissingException when engagement is missing`() {
        val localAttachment = mockk<LocalAttachment>()
        val listener = mockk<AddFileToAttachmentAndUploadUseCase.Listener>(relaxed = true)
        every { fileAttachmentRepository.isFileAttached(localAttachment.uri) } returns false
        every { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement } returns false
        every { manageSecureMessagingStatusUseCase.shouldBehaveAsSecureMessaging } returns false

        subjectUnderTest.invoke(localAttachment, listener)

        verify { listener.onError(any<EngagementMissingException>()) }
    }

    @Test
    fun `invoke calls onError with SupportedFileCountExceededException when too many files are attached`() {
        val localAttachment = mockk<LocalAttachment>()
        every { localAttachment.size } returns AddFileToAttachmentAndUploadUseCase.SUPPORTED_FILE_SIZE - 1
        val listener = mockk<AddFileToAttachmentAndUploadUseCase.Listener>(relaxed = true)
        every { fileAttachmentRepository.isFileAttached(localAttachment.uri) } returns false
        every { fileAttachmentRepository.getAttachedFilesCount() } returns FileUploadLimitNotExceededObservableUseCase.FILE_UPLOAD_LIMIT + 1
        every { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement } returns true
        every { manageSecureMessagingStatusUseCase.shouldUseSecureMessagingEndpoints } returns false

        subjectUnderTest.invoke(localAttachment, listener)

        verify { listener.onError(any<SupportedFileCountExceededException>()) }
    }

    @Test
    fun `invoke calls onError with SupportedFileSizeExceededException when file size is too large`() {
        val localAttachment = mockk<LocalAttachment>()
        val listener = mockk<AddFileToAttachmentAndUploadUseCase.Listener>(relaxed = true)
        every { fileAttachmentRepository.isFileAttached(localAttachment.uri) } returns false
        every { fileAttachmentRepository.getAttachedFilesCount() } returns 1
        every { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement } returns true
        every { localAttachment.size } returns AddFileToAttachmentAndUploadUseCase.SUPPORTED_FILE_SIZE

        subjectUnderTest.invoke(localAttachment, listener)

        verify { listener.onError(any<SupportedFileSizeExceededException>()) }
    }

    @Test
    fun `invoke calls onStarted when valid argument`() {
        val localAttachment = mockk<LocalAttachment>()
        val listener = mockk<AddFileToAttachmentAndUploadUseCase.Listener>(relaxed = true)
        every { fileAttachmentRepository.isFileAttached(localAttachment.uri) } returns false
        every { fileAttachmentRepository.getAttachedFilesCount() } returns 1
        every { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement } returns true
        every { localAttachment.size } returns AddFileToAttachmentAndUploadUseCase.SUPPORTED_FILE_SIZE - 1
        every { manageSecureMessagingStatusUseCase.shouldUseSecureMessagingEndpoints } returns false

        subjectUnderTest.invoke(localAttachment, listener)

        verify { listener.onStarted() }
    }

    @Test
    fun `invoke uploads file when is secure engagement`() {
        val localAttachment = mockk<LocalAttachment>()
        val listener = mockk<AddFileToAttachmentAndUploadUseCase.Listener>(relaxed = true)
        every { fileAttachmentRepository.isFileAttached(localAttachment.uri) } returns false
        every { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement } returns false
        every { fileAttachmentRepository.getAttachedFilesCount() } returns 1
        every { localAttachment.size } returns AddFileToAttachmentAndUploadUseCase.SUPPORTED_FILE_SIZE - 1
        every { manageSecureMessagingStatusUseCase.shouldUseSecureMessagingEndpoints } returns true
        every { manageSecureMessagingStatusUseCase.shouldBehaveAsSecureMessaging } returns true

        subjectUnderTest.invoke(localAttachment, listener)

        verify { fileAttachmentRepository.uploadFile(true, localAttachment, listener) }
    }
}
