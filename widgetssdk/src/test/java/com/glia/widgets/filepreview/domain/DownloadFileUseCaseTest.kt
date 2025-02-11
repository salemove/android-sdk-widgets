package com.glia.widgets.filepreview.domain

import com.glia.androidsdk.chat.AttachmentFile
import com.glia.widgets.filepreview.data.GliaFileRepository
import com.glia.widgets.filepreview.domain.exception.FileNameMissingException
import com.glia.widgets.filepreview.domain.exception.RemoteFileIsDeletedException
import com.glia.widgets.filepreview.domain.usecase.DownloadFileUseCase
import io.reactivex.rxjava3.core.Completable
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

private const val NAME = "NAME"
private const val NAME_EMPTY = ""

class DownloadFileUseCaseTest {
    private lateinit var fileRepository: GliaFileRepository
    private lateinit var attachmentFile: AttachmentFile
    private lateinit var useCase: DownloadFileUseCase

    @Before
    fun setUp() {
        fileRepository = mock()
        attachmentFile = mock()
        useCase = DownloadFileUseCase(fileRepository)
    }

    @Test
    fun execute_emitsFileNameMissingException_whenFileNameIsEmpty() {
        whenever(attachmentFile.name) doReturn NAME_EMPTY
        useCase(attachmentFile).test().assertError(FileNameMissingException::class.java)
    }

    @Test
    fun execute_emitsRemoteFileIsDeletedException_whenFileIsDeleted() {
        whenever(attachmentFile.name) doReturn NAME
        whenever(attachmentFile.isDeleted) doReturn true
        useCase(attachmentFile).test().assertError(RemoteFileIsDeletedException::class.java)
    }

    @Test
    fun execute_successfullyCompletes_whenValidArgument() {
        whenever(fileRepository.downloadFileFromNetwork(any())) doReturn Completable.complete()
        whenever(attachmentFile.name) doReturn NAME
        whenever(attachmentFile.isDeleted) doReturn false
        useCase(attachmentFile).test().assertComplete()
    }
}
