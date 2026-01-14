package com.glia.widgets.internal.fileupload.domain

import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.site.SiteInfo
import com.glia.widgets.chat.domain.SiteInfoUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SupportedUploadFileTypesUseCaseTest {

    private lateinit var siteInfoUseCase: SiteInfoUseCase
    private lateinit var subjectUnderTest: SupportedUploadFileTypesUseCase

    @Before
    fun setUp() {
        siteInfoUseCase = mockk(relaxUnitFun = true)
        subjectUnderTest = SupportedUploadFileTypesUseCase(siteInfoUseCase)
    }

    @Test
    fun `invoke returns result with isSendFilesAllowed true when visitor is allowed`() {
        val siteInfo = mockSiteInfo(
            isVisitorAllowed = true,
            allowedFileContentTypes = listOf("image/jpeg", "application/pdf")
        )
        val callbackSlot = slot<RequestCallback<SiteInfo?>>()
        every { siteInfoUseCase.invoke(capture(callbackSlot)) } answers {
            callbackSlot.captured.onResult(siteInfo, null)
        }
        var result: SupportedUploadFileTypesUseCase.SupportedUploadFilesResult? = null

        subjectUnderTest.invoke { result = it }

        assertTrue(result!!.isSendFilesAllowed)
    }

    @Test
    fun `invoke returns result with isSendFilesAllowed false when visitor is not allowed`() {
        val siteInfo = mockSiteInfo(
            isVisitorAllowed = false,
            allowedFileContentTypes = listOf("image/jpeg")
        )
        val callbackSlot = slot<RequestCallback<SiteInfo?>>()
        every { siteInfoUseCase.invoke(capture(callbackSlot)) } answers {
            callbackSlot.captured.onResult(siteInfo, null)
        }
        var result: SupportedUploadFileTypesUseCase.SupportedUploadFilesResult? = null

        subjectUnderTest.invoke { result = it }

        assertFalse(result!!.isSendFilesAllowed)
    }

    @Test
    fun `invoke returns result with isSendFilesAllowed false when allowedFileContentTypes is empty`() {
        val siteInfo = mockSiteInfo(
            isVisitorAllowed = true,
            allowedFileContentTypes = emptyList()
        )
        val callbackSlot = slot<RequestCallback<SiteInfo?>>()
        every { siteInfoUseCase.invoke(capture(callbackSlot)) } answers {
            callbackSlot.captured.onResult(siteInfo, null)
        }
        var result: SupportedUploadFileTypesUseCase.SupportedUploadFilesResult? = null

        subjectUnderTest.invoke { result = it }

        assertFalse(result!!.isSendFilesAllowed)
    }

    @Test
    fun `invoke returns result with isLibraryAttachmentAllowed true when image types are present`() {
        val siteInfo = mockSiteInfo(
            isVisitorAllowed = true,
            allowedFileContentTypes = listOf("image/jpeg", "image/png", "application/pdf")
        )
        val callbackSlot = slot<RequestCallback<SiteInfo?>>()
        every { siteInfoUseCase.invoke(capture(callbackSlot)) } answers {
            callbackSlot.captured.onResult(siteInfo, null)
        }
        var result: SupportedUploadFileTypesUseCase.SupportedUploadFilesResult? = null

        subjectUnderTest.invoke { result = it }

        assertTrue(result!!.isLibraryAttachmentAllowed)
    }

    @Test
    fun `invoke returns result with isLibraryAttachmentAllowed false when no image types present`() {
        val siteInfo = mockSiteInfo(
            isVisitorAllowed = true,
            allowedFileContentTypes = listOf("application/pdf", "text/plain")
        )
        val callbackSlot = slot<RequestCallback<SiteInfo?>>()
        every { siteInfoUseCase.invoke(capture(callbackSlot)) } answers {
            callbackSlot.captured.onResult(siteInfo, null)
        }
        var result: SupportedUploadFileTypesUseCase.SupportedUploadFilesResult? = null

        subjectUnderTest.invoke { result = it }

        assertFalse(result!!.isLibraryAttachmentAllowed)
    }

    @Test
    fun `invoke returns result with isTakePhotoAttachmentAllowed true when image jpeg is supported`() {
        val siteInfo = mockSiteInfo(
            isVisitorAllowed = true,
            allowedFileContentTypes = listOf("image/jpeg", "application/pdf")
        )
        val callbackSlot = slot<RequestCallback<SiteInfo?>>()
        every { siteInfoUseCase.invoke(capture(callbackSlot)) } answers {
            callbackSlot.captured.onResult(siteInfo, null)
        }
        var result: SupportedUploadFileTypesUseCase.SupportedUploadFilesResult? = null

        subjectUnderTest.invoke { result = it }

        assertTrue(result!!.isTakePhotoAttachmentAllowed)
    }

    @Test
    fun `invoke returns result with isTakePhotoAttachmentAllowed false when jpeg is not supported`() {
        val siteInfo = mockSiteInfo(
            isVisitorAllowed = true,
            allowedFileContentTypes = listOf("image/png", "application/pdf")
        )
        val callbackSlot = slot<RequestCallback<SiteInfo?>>()
        every { siteInfoUseCase.invoke(capture(callbackSlot)) } answers {
            callbackSlot.captured.onResult(siteInfo, null)
        }
        var result: SupportedUploadFileTypesUseCase.SupportedUploadFilesResult? = null

        subjectUnderTest.invoke { result = it }

        assertFalse(result!!.isTakePhotoAttachmentAllowed)
    }

    @Test
    fun `invoke returns result with isBrowseAttachmentAllowed true when file types are present`() {
        val siteInfo = mockSiteInfo(
            isVisitorAllowed = true,
            allowedFileContentTypes = listOf("application/pdf")
        )
        val callbackSlot = slot<RequestCallback<SiteInfo?>>()
        every { siteInfoUseCase.invoke(capture(callbackSlot)) } answers {
            callbackSlot.captured.onResult(siteInfo, null)
        }
        var result: SupportedUploadFileTypesUseCase.SupportedUploadFilesResult? = null

        subjectUnderTest.invoke { result = it }

        assertTrue(result!!.isBrowseAttachmentAllowed)
    }

    @Test
    fun `invoke returns result with isBrowseAttachmentAllowed false when no file types present`() {
        val siteInfo = mockSiteInfo(
            isVisitorAllowed = true,
            allowedFileContentTypes = emptyList()
        )
        val callbackSlot = slot<RequestCallback<SiteInfo?>>()
        every { siteInfoUseCase.invoke(capture(callbackSlot)) } answers {
            callbackSlot.captured.onResult(siteInfo, null)
        }
        var result: SupportedUploadFileTypesUseCase.SupportedUploadFilesResult? = null

        subjectUnderTest.invoke { result = it }

        assertFalse(result!!.isBrowseAttachmentAllowed)
    }

    @Test
    fun `invoke returns all allowed file content types`() {
        val fileTypes = listOf("image/jpeg", "image/png", "application/pdf", "text/plain")
        val siteInfo = mockSiteInfo(
            isVisitorAllowed = true,
            allowedFileContentTypes = fileTypes
        )
        val callbackSlot = slot<RequestCallback<SiteInfo?>>()
        every { siteInfoUseCase.invoke(capture(callbackSlot)) } answers {
            callbackSlot.captured.onResult(siteInfo, null)
        }
        var result: SupportedUploadFileTypesUseCase.SupportedUploadFilesResult? = null

        subjectUnderTest.invoke { result = it }

        assertEquals(fileTypes, result!!.allowedFileTypes)
    }

    @Test
    fun `invoke returns only image types in imageAllowedTypes`() {
        val fileTypes = listOf("image/jpeg", "image/png", "application/pdf", "text/plain", "image/gif")
        val expectedImageTypes = listOf("image/jpeg", "image/png", "image/gif")
        val siteInfo = mockSiteInfo(
            isVisitorAllowed = true,
            allowedFileContentTypes = fileTypes
        )
        val callbackSlot = slot<RequestCallback<SiteInfo?>>()
        every { siteInfoUseCase.invoke(capture(callbackSlot)) } answers {
            callbackSlot.captured.onResult(siteInfo, null)
        }
        var result: SupportedUploadFileTypesUseCase.SupportedUploadFilesResult? = null

        subjectUnderTest.invoke { result = it }

        assertEquals(expectedImageTypes, result!!.allowedMediaTypes)
    }

    @Test
    fun `invoke returns empty imageAllowedTypes when no image types present`() {
        val fileTypes = listOf("application/pdf", "text/plain")
        val siteInfo = mockSiteInfo(
            isVisitorAllowed = true,
            allowedFileContentTypes = fileTypes
        )
        val callbackSlot = slot<RequestCallback<SiteInfo?>>()
        every { siteInfoUseCase.invoke(capture(callbackSlot)) } answers {
            callbackSlot.captured.onResult(siteInfo, null)
        }
        var result: SupportedUploadFileTypesUseCase.SupportedUploadFilesResult? = null

        subjectUnderTest.invoke { result = it }

        assertTrue(result!!.allowedMediaTypes.isEmpty())
    }

    @Test
    fun `invoke returns default result when siteInfo is null`() {
        val callbackSlot = slot<RequestCallback<SiteInfo?>>()
        every { siteInfoUseCase.invoke(capture(callbackSlot)) } answers {
            callbackSlot.captured.onResult(null, null)
        }
        var result: SupportedUploadFileTypesUseCase.SupportedUploadFilesResult? = null

        subjectUnderTest.invoke { result = it }

        assertFalse(result!!.isSendFilesAllowed)
        assertFalse(result!!.isLibraryAttachmentAllowed)
        assertFalse(result!!.isTakePhotoAttachmentAllowed)
        assertFalse(result!!.isBrowseAttachmentAllowed)
        assertTrue(result!!.allowedFileTypes.isEmpty())
        assertTrue(result!!.allowedMediaTypes.isEmpty())
    }

    @Test
    fun `invoke calls siteInfoUseCase exactly once`() {
        val siteInfo = mockSiteInfo(
            isVisitorAllowed = true,
            allowedFileContentTypes = listOf("image/jpeg")
        )
        val callbackSlot = slot<RequestCallback<SiteInfo?>>()
        every { siteInfoUseCase.invoke(capture(callbackSlot)) } answers {
            callbackSlot.captured.onResult(siteInfo, null)
        }

        subjectUnderTest.invoke { }

        verify(exactly = 1) { siteInfoUseCase.invoke(any()) }
    }

    @Test
    fun `invoke handles complex scenario with mixed file types`() {
        val fileTypes = listOf(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "application/pdf",
            "application/msword",
            "text/plain",
            "video/mp4",
            "video/quicktime"
        )
        val expectedMediaTypes = listOf(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "video/mp4",
            "video/quicktime"
        )
        val siteInfo = mockSiteInfo(
            isVisitorAllowed = true,
            allowedFileContentTypes = fileTypes
        )
        val callbackSlot = slot<RequestCallback<SiteInfo?>>()
        every { siteInfoUseCase.invoke(capture(callbackSlot)) } answers {
            callbackSlot.captured.onResult(siteInfo, null)
        }
        var result: SupportedUploadFileTypesUseCase.SupportedUploadFilesResult? = null

        subjectUnderTest.invoke { result = it }

        assertTrue(result!!.isSendFilesAllowed)
        assertTrue(result!!.isLibraryAttachmentAllowed)
        assertTrue(result!!.isTakePhotoAttachmentAllowed)
        assertTrue(result!!.isBrowseAttachmentAllowed)
        assertEquals(fileTypes, result!!.allowedFileTypes)
        assertEquals(expectedMediaTypes, result!!.allowedMediaTypes)
    }

    @Test
    fun `invoke adds video mp4 when only video quicktime is present`() {
        val fileTypes = listOf(
            "video/quicktime"
        )
        val expectedFileTypes = listOf(
            "video/quicktime",
            "video/mp4"
        )
        val siteInfo = mockSiteInfo(
            isVisitorAllowed = true,
            allowedFileContentTypes = fileTypes
        )
        val callbackSlot = slot<RequestCallback<SiteInfo?>>()
        every { siteInfoUseCase.invoke(capture(callbackSlot)) } answers {
            callbackSlot.captured.onResult(siteInfo, null)
        }
        var result: SupportedUploadFileTypesUseCase.SupportedUploadFilesResult? = null

        subjectUnderTest.invoke { result = it }

        assertEquals(expectedFileTypes, result!!.allowedFileTypes)
    }

    private fun mockSiteInfo(
        isVisitorAllowed: Boolean,
        allowedFileContentTypes: List<String>
    ): SiteInfo {
        val allowedFileSender = mockk<SiteInfo.AllowedFileSenders>()
        every { allowedFileSender.isVisitorAllowed } returns isVisitorAllowed

        val siteInfo = mockk<SiteInfo>()
        every { siteInfo.allowedFileSenders } returns allowedFileSender
        every { siteInfo.allowedFileContentTypes } returns allowedFileContentTypes

        return siteInfo
    }
}
