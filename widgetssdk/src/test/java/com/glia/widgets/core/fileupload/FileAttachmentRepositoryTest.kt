package com.glia.widgets.core.fileupload

import android.net.Uri
import com.glia.androidsdk.Engagement
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.engagement.EngagementFile
import com.glia.androidsdk.secureconversations.SecureConversations
import com.glia.widgets.core.engagement.exception.EngagementMissingException
import com.glia.widgets.core.fileupload.domain.AddFileToAttachmentAndUploadUseCase
import com.glia.widgets.core.fileupload.model.LocalAttachment
import com.glia.widgets.di.GliaCore
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Observer
import java.util.Optional
import java.util.function.Consumer

class LocalAttachmentRepositoryTest {
    private lateinit var subjectUnderTest: FileAttachmentRepository

    private lateinit var gliaCore: GliaCore
    private lateinit var secureConversations: SecureConversations

    private lateinit var engagement: Engagement
    private lateinit var engagementFile: EngagementFile
    private lateinit var uri1: Uri
    private lateinit var uri2: Uri
    private lateinit var observer1: Observer
    private lateinit var observer2: Observer

    private val gliaException = GliaException("", GliaException.Cause.INTERNAL_ERROR)

    private val fileAttachment1: LocalAttachment
        get() = LocalAttachment(uri1, "image/jpeg", "attachment_1", 123)

    private val fileAttachment2: LocalAttachment
        get() = LocalAttachment(uri2, "application/pdf", "attachment_2", 321)

    @Before
    fun setUp() {
        gliaCore = mockk(relaxUnitFun = true)
        secureConversations = mockk(relaxUnitFun = true)

        engagement = mockk(relaxUnitFun = true)
        engagementFile = mockk(relaxUnitFun = true)
        uri1 = mockk(relaxUnitFun = true)
        uri2 = mockk(relaxUnitFun = true)
        observer1 = mockk(relaxUnitFun = true)
        observer2 = mockk(relaxUnitFun = true)

        every { gliaCore.secureConversations } returns secureConversations
        subjectUnderTest = FileAttachmentRepositoryImpl(gliaCore)
    }

    @Test
    fun `getAttachedFilesCount returns zero when no file attachments attached`() {
        subjectUnderTest.detachAllFiles()
        val result = subjectUnderTest.getAttachedFilesCount()
        assertEquals(0, result)
    }

    @Test
    fun `getAttachedFilesCount returns one when one file attachment attached`() {
        subjectUnderTest.detachAllFiles()
        subjectUnderTest.attachFile(fileAttachment1)
        val result = subjectUnderTest.getAttachedFilesCount()
        assertEquals(1, result)
    }

    @Test
    fun `isFileAttached returns true when file attachment attached`() {
        subjectUnderTest.detachAllFiles()
        subjectUnderTest.attachFile(fileAttachment1)
        val result = subjectUnderTest.isFileAttached(uri1)
        assertTrue(result)
    }

    @Test
    fun `isFileAttached returns false when file attachment not attached`() {
        subjectUnderTest.detachAllFiles()
        val result = subjectUnderTest.isFileAttached(uri1)
        assertFalse(result)
    }

    @Test
    fun `attachFile attaches file attachment when valid argument`() {
        subjectUnderTest.detachAllFiles()
        subjectUnderTest.attachFile(fileAttachment1)
        val result = subjectUnderTest.getFileAttachments()
        assertTrue(result.contains(fileAttachment1))
    }

    @Test
    fun `uploadFile successful when security check not needed`() {
        val listener = mockk<AddFileToAttachmentAndUploadUseCase.Listener>(relaxed = true)
        every { gliaCore.currentEngagement } returns Optional.of(engagement)
        every { engagementFile.isSecurityScanRequired } returns false
        every { engagement.uploadFile(any<Uri>(), any()) } answers {
            val callback = secondArg<RequestCallback<EngagementFile>>()
            callback.onResult(engagementFile, null)
        }
        subjectUnderTest.detachAllFiles()
        subjectUnderTest.attachFile(fileAttachment1)
        subjectUnderTest.attachFile(fileAttachment2)

        subjectUnderTest.uploadFile(false, fileAttachment1, listener)

        verify { listener.onFinished() }
        assertEquals(LocalAttachment.Status.READY_TO_SEND, subjectUnderTest.getFileAttachments().first().attachmentStatus)
    }

    @Test
    fun `uploadFile successful when security needed`() {
        val listener = mockk<AddFileToAttachmentAndUploadUseCase.Listener>(relaxed = true)
        every { gliaCore.currentEngagement } returns Optional.of(engagement)
        every { engagementFile.isSecurityScanRequired } returns true
        every { engagement.uploadFile(any<Uri>(), any()) } answers {
            val callback = secondArg<RequestCallback<EngagementFile>>()
            callback.onResult(engagementFile, null)
        }

        val fileScanCallbackSlot = slot<Consumer<EngagementFile.ScanResult>>()

        subjectUnderTest.detachAllFiles()
        subjectUnderTest.attachFile(fileAttachment1)

        subjectUnderTest.uploadFile(false, fileAttachment1, listener)
        assertEquals(LocalAttachment.Status.SECURITY_SCAN, subjectUnderTest.getFileAttachments().first().attachmentStatus)
        verify { engagementFile.on(eq(EngagementFile.Events.SCAN_RESULT), capture(fileScanCallbackSlot)) }
        verify { listener.onSecurityCheckStarted() }

        fileScanCallbackSlot.captured.accept(EngagementFile.ScanResult.CLEAN)
        verify { listener.onSecurityCheckFinished(EngagementFile.ScanResult.CLEAN) }
        verify { listener.onFinished() }
        assertEquals(LocalAttachment.Status.READY_TO_SEND, subjectUnderTest.getFileAttachments().first().attachmentStatus)
    }

    @Test
    fun `uploadFile fails when missing engagement`() {
        val listener = mockk<AddFileToAttachmentAndUploadUseCase.Listener>(relaxed = true)
        every { gliaCore.currentEngagement } returns Optional.empty()
        subjectUnderTest.detachAllFiles()
        subjectUnderTest.attachFile(fileAttachment1)

        subjectUnderTest.uploadFile(false, fileAttachment1, listener)

        verify { listener.onError(any<EngagementMissingException>()) }
        assertEquals(LocalAttachment.Status.ERROR_ENGAGEMENT_MISSING, subjectUnderTest.getFileAttachments().first().attachmentStatus)
    }

    @Test
    fun `uploadFile fails when upload fails`() {
        val listener = mockk<AddFileToAttachmentAndUploadUseCase.Listener>(relaxed = true)
        every { gliaCore.currentEngagement } returns Optional.of(engagement)
        every { engagement.uploadFile(any<Uri>(), any()) } answers {
            val callback = secondArg<RequestCallback<EngagementFile>>()
            callback.onResult(null, gliaException)
        }
        subjectUnderTest.detachAllFiles()
        subjectUnderTest.attachFile(fileAttachment1)

        subjectUnderTest.uploadFile(false, fileAttachment1, listener)

        verify { listener.onError(gliaException) }
        assertEquals(LocalAttachment.Status.ERROR_INTERNAL, subjectUnderTest.getFileAttachments().first().attachmentStatus)
    }

    @Test
    fun `uploadFile fails when security check fails`() {
        val listener = mockk<AddFileToAttachmentAndUploadUseCase.Listener>(relaxed = true)
        every { gliaCore.currentEngagement } returns Optional.of(engagement)
        every { engagementFile.isSecurityScanRequired } returns true
        every { engagement.uploadFile(any<Uri>(), any()) } answers {
            val callback = secondArg<RequestCallback<EngagementFile>>()
            callback.onResult(engagementFile, null)
        }

        val fileScanCallbackSlot = slot<Consumer<EngagementFile.ScanResult>>()

        subjectUnderTest.detachAllFiles()
        subjectUnderTest.attachFile(fileAttachment1)

        subjectUnderTest.uploadFile(false, fileAttachment1, listener)

        verify { engagementFile.on(eq(EngagementFile.Events.SCAN_RESULT), capture(fileScanCallbackSlot)) }
        verify { listener.onSecurityCheckStarted() }

        fileScanCallbackSlot.captured.accept(EngagementFile.ScanResult.INFECTED)

        verify { listener.onSecurityCheckFinished(EngagementFile.ScanResult.INFECTED) }
        verify { listener.onFinished() }
        assertEquals(LocalAttachment.Status.ERROR_SECURITY_SCAN_FAILED, subjectUnderTest.getFileAttachments().first().attachmentStatus)
    }

    @Test
    fun `uploadFile checks if secure engagement when engagement is null`() {
        val listener = mockk<AddFileToAttachmentAndUploadUseCase.Listener>(relaxed = true)
        every { gliaCore.currentEngagement } returns Optional.empty()

        subjectUnderTest.attachFile(fileAttachment1)
        subjectUnderTest.uploadFile(false, fileAttachment1, listener)

        assertEquals(LocalAttachment.Status.ERROR_ENGAGEMENT_MISSING, subjectUnderTest.getFileAttachments().first().attachmentStatus)
        verify { listener.onError(any<EngagementMissingException>()) }
    }

    @Test
    fun `uploadFile uses secure upload file when secure engagement`() {
        val listener = mockk<AddFileToAttachmentAndUploadUseCase.Listener>(relaxed = true)
        every { gliaCore.currentEngagement } returns Optional.empty()

        subjectUnderTest.uploadFile(true, fileAttachment1, listener)

        verify { secureConversations.uploadFile(eq(uri1), any()) }
    }

    @Test
    fun `setFileAttachmentTooLarge updates correct file attachment status when multiple file attachments attached`() {
        subjectUnderTest.detachAllFiles()
        subjectUnderTest.attachFile(fileAttachment1)
        subjectUnderTest.attachFile(fileAttachment2)

        subjectUnderTest.setFileAttachmentTooLarge(uri1)

        subjectUnderTest.getFileAttachments().forEach {
            if (it.uri == uri1) {
                assertEquals(LocalAttachment.Status.ERROR_FILE_TOO_LARGE, it.attachmentStatus)
            } else {
                assertEquals(LocalAttachment.Status.UPLOADING, it.attachmentStatus)
            }
        }
    }

    @Test
    fun `setSupportedFileAttachmentCountExceeded updates file attachment status when file attachment attached`() {
        subjectUnderTest.detachAllFiles()
        subjectUnderTest.attachFile(fileAttachment1)

        subjectUnderTest.setSupportedFileAttachmentCountExceeded(uri1)

        assertEquals(
            LocalAttachment.Status.ERROR_SUPPORTED_FILE_ATTACHMENT_COUNT_EXCEEDED,
            subjectUnderTest.getFileAttachments().first().attachmentStatus
        )
    }

    @Test
    fun `setFileAttachmentEngagementMissing updates file attachment status when file attachment attached`() {
        subjectUnderTest.detachAllFiles()
        subjectUnderTest.attachFile(fileAttachment1)

        subjectUnderTest.setFileAttachmentEngagementMissing(uri1)

        assertEquals(LocalAttachment.Status.ERROR_ENGAGEMENT_MISSING, subjectUnderTest.getFileAttachments().first().attachmentStatus)
    }

    @Test
    fun `attachFile attaches multiple files when called multiple times`() {
        subjectUnderTest.detachAllFiles()
        subjectUnderTest.attachFile(fileAttachment1)
        subjectUnderTest.attachFile(fileAttachment2)
        val result = subjectUnderTest.getFileAttachments()
        assertTrue(result.containsAll(listOf(fileAttachment1, fileAttachment2)))
    }

    @Test
    fun `attachFile attaches same file multiple times when called multiple times`() {
        subjectUnderTest.detachAllFiles()
        subjectUnderTest.attachFile(fileAttachment1)
        subjectUnderTest.attachFile(fileAttachment1)
        val result = subjectUnderTest.getFileAttachments()
        assertTrue(result.indexOf(fileAttachment1) != result.lastIndexOf(fileAttachment1))
    }

    @Test
    fun `detachFile detaches file when file attachment attached`() {
        subjectUnderTest.detachAllFiles()
        subjectUnderTest.attachFile(fileAttachment1)
        subjectUnderTest.attachFile(fileAttachment2)

        subjectUnderTest.detachFile(fileAttachment1)

        val result = subjectUnderTest.getFileAttachments()

        assertFalse(result.contains(fileAttachment1))
        assertTrue(result.contains(fileAttachment2))
    }

    @Test
    fun `detachFile detaches all files when same file attached multiple times`() {
        subjectUnderTest.detachAllFiles()
        subjectUnderTest.attachFile(fileAttachment1)
        subjectUnderTest.attachFile(fileAttachment1)

        subjectUnderTest.detachFile(fileAttachment1)

        assertTrue(subjectUnderTest.getFileAttachments().isEmpty())
    }

    @Test
    fun `detachFile does nothing when file attachment not attached`() {
        subjectUnderTest.detachAllFiles()

        subjectUnderTest.detachFile(fileAttachment1)

        assertFalse(subjectUnderTest.getFileAttachments().contains(fileAttachment1))
    }

    @Test
    fun `detachAllFiles detaches all files when files attached`() {
        subjectUnderTest.detachAllFiles()
        subjectUnderTest.attachFile(fileAttachment1)
        subjectUnderTest.attachFile(fileAttachment1)
        subjectUnderTest.attachFile(fileAttachment2)

        subjectUnderTest.detachAllFiles()

        assertTrue(subjectUnderTest.getFileAttachments().isEmpty())
    }

    @Test
    fun `detachAllFiles does nothing when no files attached`() {
        subjectUnderTest.detachAllFiles()
        subjectUnderTest.detachAllFiles()

        assertTrue(subjectUnderTest.getFileAttachments().isEmpty())
    }

    @Test
    fun `getReadyToSendFileAttachments returns ready to send file attachments when no files attached`() {
        subjectUnderTest.detachAllFiles()
        val attachment1 = fileAttachment1.copy(attachmentStatus = LocalAttachment.Status.READY_TO_SEND)
        val attachment2 = fileAttachment2
        subjectUnderTest.attachFile(attachment1)
        subjectUnderTest.attachFile(attachment2)

        val result = subjectUnderTest.getReadyToSendFileAttachments()

        assertTrue(result.contains(attachment1))
        assertFalse(result.contains(attachment2))
    }
}
