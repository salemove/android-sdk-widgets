package com.glia.widgets.view.unifiedui.theme.chat

import android.os.Parcelable
import com.glia.widgets.view.unifiedui.theme.base.ThemeColor
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class ThemeAttachmentsPopup(
    val dividerColor: ThemeColor?,
    val background: ThemeColor?,
    val photoLibrary: ThemeAttachmentsItem?,
    val takePhoto: ThemeAttachmentsItem?,
    val browse: ThemeAttachmentsItem?
): Parcelable
