package com.glia.widgets.filepreview.domain.usecase

import com.glia.androidsdk.chat.AttachmentFile
import com.glia.widgets.core.secureconversations.domain.ManageSecureMessagingStatusUseCase
import com.glia.widgets.filepreview.data.GliaFileRepository
import com.glia.widgets.filepreview.domain.exception.FileNameMissingException
import com.glia.widgets.filepreview.domain.exception.RemoteFileIsDeletedException
import io.reactivex.rxjava3.core.Completable

internal class DownloadFileUseCase(
    private val fileRepository: GliaFileRepository,
    private val manageSecureMessagingStatusUseCase: ManageSecureMessagingStatusUseCase
) {
    operator fun invoke(file: AttachmentFile): Completable = when {
        file.name.isEmpty() -> Completable.error(FileNameMissingException())
        file.isDeleted -> Completable.error(RemoteFileIsDeletedException())
        else -> fileRepository.downloadFileFromNetwork(manageSecureMessagingStatusUseCase.shouldUseSecureMessagingEndpoints, file)
    }
}
