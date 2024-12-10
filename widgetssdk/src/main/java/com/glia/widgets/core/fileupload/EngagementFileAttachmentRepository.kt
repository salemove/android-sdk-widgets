package com.glia.widgets.core.fileupload

import com.glia.widgets.core.engagement.exception.EngagementMissingException
import com.glia.widgets.core.fileupload.domain.AddFileToAttachmentAndUploadUseCase
import com.glia.widgets.core.fileupload.model.LocalAttachment
import com.glia.widgets.di.GliaCore
import kotlin.jvm.optionals.getOrNull

internal class EngagementFileAttachmentRepository(
    private val gliaCore: GliaCore
) : BaseFileAttachmentRepository() {

    override fun uploadFile(file: LocalAttachment, listener: AddFileToAttachmentAndUploadUseCase.Listener) {
        val engagement = gliaCore.currentEngagement.getOrNull()
        when {

            engagement != null -> {
                engagement.uploadFile(file.uri, handleFileUpload(file, listener))
            }

            else -> {
                setFileAttachmentEngagementMissing(file.uri)
                listener.onError(EngagementMissingException())
            }
        }
    }
}
