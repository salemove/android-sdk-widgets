package com.glia.widgets.view.unifiedui.config.gva

import com.glia.widgets.view.unifiedui.config.base.ButtonRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.LayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.TextRemoteConfig
import com.glia.widgets.view.unifiedui.theme.gva.GvaPersistentButtonTheme
import com.google.gson.annotations.SerializedName

internal data class GvaPersistentButtonRemoteConfig(
    @SerializedName("title")
    val titleRemoteConfig: TextRemoteConfig?,

    @SerializedName("background")
    val backgroundRemoteConfig: LayerRemoteConfig?,

    @SerializedName("button")
    val buttonRemoteConfig: ButtonRemoteConfig?
) {
    fun toGvaPersistentButtonTheme(): GvaPersistentButtonTheme = GvaPersistentButtonTheme(
        title = titleRemoteConfig?.toTextTheme(),
        background = backgroundRemoteConfig?.toLayerTheme(),
        button = buttonRemoteConfig?.toButtonTheme()
    )
}
