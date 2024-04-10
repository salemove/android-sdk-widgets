package com.glia.widgets.filepreview.domain

import android.graphics.Bitmap
import com.glia.widgets.filepreview.data.GliaFileRepository
import com.glia.widgets.filepreview.domain.exception.FileNameMissingException
import com.glia.widgets.filepreview.domain.usecase.PutImageFileToDownloadsUseCase
import io.reactivex.rxjava3.core.Completable
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class PutImageFileToDownloadsUseCaseTest {
    private lateinit var fileRepository: GliaFileRepository
    private lateinit var useCase: PutImageFileToDownloadsUseCase

    @Before
    fun setUp() {
        fileRepository = mock()
        useCase = PutImageFileToDownloadsUseCase(fileRepository)
    }

    @Test
    fun execute_emitsFileNameMissingException_whenFileNameIsNull() {
        useCase(null, BITMAP)
            .test()
            .assertError(FileNameMissingException::class.java)
    }

    @Test
    fun execute_emitsFileNameMissingException_whenFileNameIsEmpty() {
        useCase(NAME_EMPTY, BITMAP)
            .test()
            .assertError(FileNameMissingException::class.java)
    }

    @Test
    fun execute_successfullyCompletes_whenValidArgument() {
        whenever(fileRepository.putImageToDownloads(any(), any())) doReturn Completable.complete()
        useCase(NAME, BITMAP)
            .test()
            .assertComplete()
    }

    companion object {
        private const val NAME = "NAME"
        private const val NAME_EMPTY = ""
        private val BITMAP: Bitmap = mock()
    }
}
