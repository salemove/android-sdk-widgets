package com.glia.widgets.view.unifiedui.config.chat

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal enum class AttachmentSourceTypeRemoteConfig(val value: String) : Parcelable {
    PHOTO_LIBRARY("photoLibrary"),
    TAKE_PHOTO("takePhoto"),
    BROWSE("browse")
}