package com.glia.widgets.core.fileupload

import com.glia.widgets.core.engagement.exception.EngagementMissingException
import com.glia.widgets.core.fileupload.domain.AddFileToAttachmentAndUploadUseCase
import com.glia.widgets.core.fileupload.model.LocalAttachment
import com.glia.widgets.di.GliaCore
import java.util.Observer
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

    @Deprecated("REMOVE")
    fun addObserver(observer: Observer?) {}

    @Deprecated("REMOVE")
    fun removeObserver(observer: Observer?) {}

    @Deprecated("REMOVE")
    fun clearObservers() {}

//    private fun setFileAttachmentStatus(uri: Uri, status: LocalAttachment.Status) {
//        observable.notifyUpdate(
//            observable.localAttachments
//                .map { localAttachment: LocalAttachment ->
//                    if (localAttachment.uri === uri) {
//                        localAttachment.copy(attachmentStatus = status)
//                    } else {
//                        localAttachment
//                    }
//                }
//        )
//    }

//    private fun onEngagementFileReceived(uri: Uri, engagementFile: EngagementFile) {
//        observable.notifyUpdate(
//            observable.localAttachments
//                .map { attachment: LocalAttachment ->
//                    if (attachment.uri == uri) {
//                        attachment.copy(attachmentStatus = LocalAttachment.Status.READY_TO_SEND, engagementFile = engagementFile)
//                    } else {
//                        attachment
//                    }
//                }
//        )
//    }
}
