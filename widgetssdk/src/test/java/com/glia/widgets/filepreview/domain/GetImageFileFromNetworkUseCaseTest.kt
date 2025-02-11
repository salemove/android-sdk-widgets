package com.glia.widgets.filepreview.domain

import android.graphics.Bitmap
import com.glia.androidsdk.chat.AttachmentFile
import com.glia.widgets.chat.domain.DecodeSampledBitmapFromInputStreamUseCase
import com.glia.widgets.filepreview.data.GliaFileRepository
import com.glia.widgets.filepreview.domain.exception.FileNameMissingException
import com.glia.widgets.filepreview.domain.exception.RemoteFileIsDeletedException
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromNetworkUseCase
import io.reactivex.rxjava3.core.Maybe
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.IOException
import java.io.InputStream

class GetImageFileFromNetworkUseCaseTest {
    private lateinit var fileRepository: GliaFileRepository
    private lateinit var attachmentFile: AttachmentFile
    private lateinit var useCase: GetImageFileFromNetworkUseCase
    private lateinit var decodeSampledBitmapFromInputStreamUseCase: DecodeSampledBitmapFromInputStreamUseCase

    @Before
    fun setUp() {
        fileRepository = mock()
        attachmentFile = mock()
        decodeSampledBitmapFromInputStreamUseCase = mock()
        useCase = GetImageFileFromNetworkUseCase(
            fileRepository,
            decodeSampledBitmapFromInputStreamUseCase
        )
    }

    @Test
    fun execute_emitsFileNameMissingException_whenFileIsNull() {
        useCase(null)
            .test()
            .assertError(FileNameMissingException::class.java)
    }

    @Test
    fun execute_emitsFileNameMissingException_whenFileNameIsEmpty() {
        whenever(attachmentFile.name) doReturn NAME_EMPTY
        useCase(attachmentFile)
            .test()
            .assertError(FileNameMissingException::class.java)
    }

    @Test
    fun execute_emitsError_whenDecodingFails() {
        whenever(fileRepository.loadImageFileFromNetwork(any())) doReturn Maybe.just(INPUT_STREAM)
        whenever(attachmentFile.name) doReturn NAME
        whenever(attachmentFile.isDeleted) doReturn false
        whenever(decodeSampledBitmapFromInputStreamUseCase(INPUT_STREAM)) doReturn Maybe.error(
            IOException()
        )
        useCase(attachmentFile)
            .test()
            .assertError(IOException::class.java)
    }

    @Test
    fun execute_emitsRemoteFileIsDeletedException_whenFileIsDeleted() {
        whenever(attachmentFile.name) doReturn NAME
        whenever(attachmentFile.isDeleted) doReturn true
        useCase(attachmentFile)
            .test()
            .assertError(RemoteFileIsDeletedException::class.java)
    }

    @Test
    fun execute_successfullyCompletes_whenValidArgument() {
        whenever(fileRepository.loadImageFileFromNetwork(any())) doReturn Maybe.just(INPUT_STREAM)
        whenever(decodeSampledBitmapFromInputStreamUseCase(INPUT_STREAM)) doReturn Maybe.just(BITMAP)
        whenever(attachmentFile.name) doReturn NAME
        whenever(attachmentFile.isDeleted) doReturn false
        useCase(attachmentFile).test().assertResult(BITMAP)
    }

    companion object {
        private val INPUT_STREAM: InputStream = mock()
        private const val NAME = "NAME"
        private const val NAME_EMPTY = ""
        private val BITMAP: Bitmap = mock()
    }
}
