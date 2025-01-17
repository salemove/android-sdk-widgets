package com.glia.widgets.view.unifiedui.config.entrywidget

import com.glia.widgets.view.unifiedui.config.base.ColorLayerRemoteConfig
import com.glia.widgets.view.unifiedui.theme.entrywidget.MediaTypeItemsTheme
import com.google.gson.annotations.SerializedName

internal data class MediaTypeItemsRemoteConfig(
    @SerializedName("mediaTypeItem")
    val mediaTypeItem: MediaTypeItemRemoteConfig?,
    @SerializedName("dividerColor")
    val dividerColor: ColorLayerRemoteConfig?
) {
    fun toMediaTypeItemsTheme(): MediaTypeItemsTheme = MediaTypeItemsTheme(
        mediaTypeItem = mediaTypeItem?.toMediaTypeItemTheme(),
        dividerColor = dividerColor?.toColorTheme()
    )
}
