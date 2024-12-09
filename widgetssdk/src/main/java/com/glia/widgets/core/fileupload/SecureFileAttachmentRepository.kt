package com.glia.widgets.core.fileupload

import com.glia.androidsdk.secureconversations.SecureConversations
import com.glia.widgets.core.fileupload.domain.AddFileToAttachmentAndUploadUseCase
import com.glia.widgets.core.fileupload.model.LocalAttachment
import com.glia.widgets.di.GliaCore

internal class SecureFileAttachmentRepository(
    private val gliaCore: GliaCore
) : BaseFileAttachmentRepository() {
    private val secureConversations: SecureConversations by lazy {
        gliaCore.secureConversations
    }

    override fun uploadFile(file: LocalAttachment, listener: AddFileToAttachmentAndUploadUseCase.Listener) {
        secureConversations.uploadFile(file.uri, handleFileUpload(file, listener))
    }
}
