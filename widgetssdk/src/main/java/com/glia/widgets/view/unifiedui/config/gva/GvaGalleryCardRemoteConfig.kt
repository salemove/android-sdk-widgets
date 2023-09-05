package com.glia.widgets.view.unifiedui.config.gva

import com.glia.widgets.view.unifiedui.config.base.ButtonRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.LayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.TextRemoteConfig
import com.glia.widgets.view.unifiedui.theme.gva.GvaGalleryCardTheme
import com.google.gson.annotations.SerializedName

internal data class GvaGalleryCardRemoteConfig(
    @SerializedName("title")
    val titleRemoteConfig: TextRemoteConfig?,

    @SerializedName("subtitle")
    val subtitleRemoteConfig: TextRemoteConfig?,

    @SerializedName("image")
    val imageRemoteConfig: LayerRemoteConfig?,

    @SerializedName("button")
    val buttonRemoteConfig: ButtonRemoteConfig?,

    @SerializedName("background")
    val backgroundRemoteConfig: LayerRemoteConfig?
) {
    fun toGvaGalleryCardTheme(): GvaGalleryCardTheme = GvaGalleryCardTheme(
        title = titleRemoteConfig?.toTextTheme(),
        subtitle = subtitleRemoteConfig?.toTextTheme(),
        image = imageRemoteConfig?.toLayerTheme(),
        button = buttonRemoteConfig?.toButtonTheme(),
        background = backgroundRemoteConfig?.toLayerTheme()
    )
}
