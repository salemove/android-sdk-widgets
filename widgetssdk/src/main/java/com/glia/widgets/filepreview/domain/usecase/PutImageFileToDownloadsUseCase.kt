package com.glia.widgets.filepreview.domain.usecase

import android.graphics.Bitmap
import com.glia.widgets.filepreview.data.GliaFileRepository
import com.glia.widgets.filepreview.domain.exception.FileNameMissingException
import io.reactivex.Completable

internal class PutImageFileToDownloadsUseCase(private val gliaFileRepository: GliaFileRepository) {
    operator fun invoke(fileName: String?, bitmap: Bitmap): Completable {
        return if (fileName.isNullOrEmpty()) {
            Completable.error(FileNameMissingException())
        } else gliaFileRepository
            .putImageToDownloads(fileName, bitmap)
    }
}
