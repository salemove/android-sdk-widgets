package com.glia.widgets.view.unifiedui.config.entrywidget

import com.glia.widgets.view.unifiedui.config.base.ColorRemoteConfig
import com.google.gson.annotations.SerializedName
import com.glia.widgets.view.unifiedui.theme.entrywidget.MediaTypeItemsTheme

internal data class MediaTypeItemsRemoteConfig(
    @SerializedName("mediaTypeItem")
    val mediaTypeItem: MediaTypeItemRemoteConfig?,
    @SerializedName("dividerColor")
    val dividerColor: ColorRemoteConfig?
) {
    fun toMediaTypeItemsTheme(): MediaTypeItemsTheme = MediaTypeItemsTheme(
        mediaTypeItem = mediaTypeItem?.toMediaTypeItemTheme(),
        dividerColor = dividerColor?.toColorTheme()
    )
}
