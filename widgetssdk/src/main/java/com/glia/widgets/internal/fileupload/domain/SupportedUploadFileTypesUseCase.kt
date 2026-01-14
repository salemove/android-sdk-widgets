package com.glia.widgets.internal.fileupload.domain

import com.glia.androidsdk.site.SiteInfo
import com.glia.widgets.chat.domain.SiteInfoUseCase

/**
 * @hide
 */
internal class SupportedUploadFileTypesUseCase(
    private val siteInfoUseCase: SiteInfoUseCase
) {
    operator fun invoke(onResult: (SupportedUploadFilesResult) -> Unit) {
        siteInfoUseCase.invoke { response, _ ->
            response?.let {
                onResult(
                    SupportedUploadFilesResult(
                        isSendFilesAllowed = it.allowedFileSenders.isVisitorAllowed && it.allowedFileContentTypes.isNotEmpty(),
                        isLibraryAttachmentAllowed = isLibraryAttachmentAllowed(it),
                        isTakePhotoAttachmentAllowed = isTakePhotoAttachmentAllowed(it),
                        isBrowseAttachmentAllowed = isBrowseAttachmentAllowed(it),
                        allowedFileTypes = allowedFileContentTypes(it),
                        allowedMediaTypes = allowedMediaContentTypes(it)
                    )
                )
            } ?: run {
                onResult(
                    SupportedUploadFilesResult(
                        isSendFilesAllowed = false,
                        isLibraryAttachmentAllowed = false,
                        isTakePhotoAttachmentAllowed = false,
                        isBrowseAttachmentAllowed = false,
                        allowedFileTypes = emptyList(),
                        allowedMediaTypes = emptyList()
                    )
                )
                return@invoke
            }
        }
    }

    private fun allowedFileContentTypes(siteInfo: SiteInfo): List<String> {
        // Android's ActivityResultContracts.OpenDocument does not check the MOV file extension, only the container type.
        // Since MOV files often use the MP4 container, we must add "video/mp4" to allowed types if "video/quicktime" is supported.
        // This ensures users can select MOV files even when only "video/quicktime" is listed by the site.
        val containsVideoQuickTime = siteInfo.allowedFileContentTypes.any { it.equals("video/quicktime") }
        val containsVideoMp4 = siteInfo.allowedFileContentTypes.any { it.equals("video/mp4") }
        if (containsVideoQuickTime && !containsVideoMp4) {
            return siteInfo.allowedFileContentTypes + "video/mp4"
        }
        return siteInfo.allowedFileContentTypes
    }

    private fun allowedMediaContentTypes(siteInfo: SiteInfo): List<String> {
        return siteInfo.allowedFileContentTypes.filter { it.startsWith("image/") || it.startsWith("video/") }
    }

    private fun isLibraryAttachmentAllowed(siteInfo: SiteInfo): Boolean {
        return allowedMediaContentTypes(siteInfo).isNotEmpty()
    }

    private fun isTakePhotoAttachmentAllowed(siteInfo: SiteInfo): Boolean {
        return siteInfo.allowedFileContentTypes.contains("image/jpeg")
    }

    private fun isBrowseAttachmentAllowed(siteInfo: SiteInfo): Boolean {
        return allowedFileContentTypes(siteInfo).isNotEmpty()
    }

    data class SupportedUploadFilesResult(
        val isSendFilesAllowed: Boolean,
        val isLibraryAttachmentAllowed: Boolean,
        val isTakePhotoAttachmentAllowed: Boolean,
        val isBrowseAttachmentAllowed: Boolean,
        val allowedFileTypes: List<String>,
        val allowedMediaTypes: List<String>
    )
}
