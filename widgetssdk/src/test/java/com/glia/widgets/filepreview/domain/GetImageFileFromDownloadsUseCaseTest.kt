package com.glia.widgets.filepreview.domain

import android.graphics.Bitmap
import com.glia.widgets.filepreview.data.GliaFileRepository
import com.glia.widgets.filepreview.domain.exception.FileNameMissingException
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromDownloadsUseCase
import io.reactivex.Maybe
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GetImageFileFromDownloadsUseCaseTest {
    private lateinit var fileRepository: GliaFileRepository
    private lateinit var useCase: GetImageFileFromDownloadsUseCase

    @Before
    fun setUp() {
        fileRepository = mock()
        useCase = GetImageFileFromDownloadsUseCase(fileRepository)
    }

    @Test
    fun execute_emitFileNameMissingException_whenFileNameIsNull() {
        useCase(null)
            .test()
            .assertError(FileNameMissingException::class.java)
    }

    @Test
    fun execute_emitFileNameMissingException_whenFileNameIsEmpty() {
        useCase(NAME_EMPTY)
            .test()
            .assertError(FileNameMissingException::class.java)
    }

    @Test
    fun execute_successfullyCompletes_whenValidArgument() {
        whenever(fileRepository.loadImageFromDownloads(any())) doReturn Maybe.just(BITMAP)
        useCase(NAME)
            .test()
            .assertResult(BITMAP)
    }

    companion object {
        private const val NAME = "NAME"
        private const val NAME_EMPTY = ""
        private val BITMAP: Bitmap = mock()
    }
}
