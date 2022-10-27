package com.glia.widgets.view.unifiedui.theme.chat

import android.os.Parcelable
import com.glia.widgets.view.unifiedui.theme.base.ThemeColor
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class ThemeFileUploadBar(
    val filePreview: ThemeFilePreview?,
    val uploading: ThemeFileUpload?,
    val uploaded: ThemeFileUpload?,
    val error: ThemeFileUpload?,
    val progress: ThemeColor?,
    val errorProgress: ThemeColor?,
    val progressBackground: ThemeColor?,
    val removeButton: ThemeColor?
) : Parcelable
