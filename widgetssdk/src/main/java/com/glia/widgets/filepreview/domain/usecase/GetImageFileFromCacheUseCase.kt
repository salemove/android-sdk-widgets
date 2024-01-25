package com.glia.widgets.filepreview.domain.usecase

import android.graphics.Bitmap
import com.glia.widgets.filepreview.data.GliaFileRepository
import com.glia.widgets.filepreview.domain.exception.FileNameMissingException
import io.reactivex.Maybe

internal class GetImageFileFromCacheUseCase(private val gliaFileRepository: GliaFileRepository) {
    operator fun invoke(fileName: String?): Maybe<Bitmap> {
        return if (fileName.isNullOrEmpty()) {
            Maybe.error(FileNameMissingException())
        } else {
            gliaFileRepository.loadImageFromCache(fileName)
        }
    }
}
