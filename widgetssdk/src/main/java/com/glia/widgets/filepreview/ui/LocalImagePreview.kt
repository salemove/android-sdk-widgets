package com.glia.widgets.filepreview.ui

import android.net.Uri
import android.os.Parcelable
import com.glia.widgets.internal.fileupload.model.LocalAttachment
import kotlinx.parcelize.Parcelize

/**
 * The local attachment fields the image preview needs to show and share a visitor's own image.
 */
@Parcelize
internal data class LocalImagePreview(
    val uri: Uri,
    val mimeType: String?,
    val fileId: String,
    val displayName: String,
    val size: Long
) : Parcelable

internal fun LocalAttachment.toLocalImagePreview(): LocalImagePreview = LocalImagePreview(uri, mimeType, id, displayName, size)
