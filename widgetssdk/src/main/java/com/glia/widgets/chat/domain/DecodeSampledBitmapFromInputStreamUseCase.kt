package com.glia.widgets.chat.domain

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import io.reactivex.Maybe
import java.io.IOException
import java.io.InputStream
import kotlin.math.roundToInt

private const val DESIRED_IMAGE_SIZE = 640

internal class DecodeSampledBitmapFromInputStreamUseCase {
    operator fun invoke(inputStream: InputStream): Maybe<Bitmap> =
        Maybe.create {
            val rawBitmap = BitmapFactory.decodeStream(inputStream)
            if (rawBitmap == null) {
                it.onError(IOException("InputStream could not be decoded"))
                return@create
            }
            val rawHeight = rawBitmap.height.toDouble()
            val rawWidth = rawBitmap.width.toDouble()
            val ratio = rawWidth / rawHeight
            val scaledBitmap = Bitmap.createScaledBitmap(
                rawBitmap,
                (DESIRED_IMAGE_SIZE * ratio).roundToInt(),
                DESIRED_IMAGE_SIZE,
                false
            )

            if (rawBitmap != scaledBitmap) {
                rawBitmap.recycle()
            }

            if (scaledBitmap != null) {
                it.onSuccess(scaledBitmap)
            } else {
                it.onError(IOException("Bitmap could not be scaled"))
            }
        }
}
