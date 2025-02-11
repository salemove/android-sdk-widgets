package com.glia.widgets.filepreview.domain.usecase

import android.graphics.Bitmap
import com.glia.androidsdk.chat.AttachmentFile
import com.glia.widgets.chat.domain.DecodeSampledBitmapFromInputStreamUseCase
import com.glia.widgets.filepreview.data.GliaFileRepository
import com.glia.widgets.filepreview.domain.exception.FileNameMissingException
import com.glia.widgets.filepreview.domain.exception.RemoteFileIsDeletedException
import com.glia.widgets.helper.fileName
import io.reactivex.rxjava3.core.Maybe

internal class GetImageFileFromNetworkUseCase(
    private val gliaFileRepository: GliaFileRepository,
    private val decodeSampledBitmapFromInputStreamUseCase: DecodeSampledBitmapFromInputStreamUseCase,
) {
    operator fun invoke(file: AttachmentFile?): Maybe<Bitmap> = when {
        file?.name.isNullOrBlank() -> Maybe.error(FileNameMissingException())
        file!!.isDeleted -> Maybe.error(RemoteFileIsDeletedException())
        else -> gliaFileRepository.loadImageFileFromNetwork(file)
            .flatMap { decodeSampledBitmapFromInputStreamUseCase(it) }
            .doOnSuccess { gliaFileRepository.putImageToCache(file.fileName, it) }
    }
}
