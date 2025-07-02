package com.glia.widgets.view.unifiedui.config.entrywidget

import com.glia.widgets.view.unifiedui.config.base.ButtonRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.LayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.TextRemoteConfig
import com.glia.widgets.view.unifiedui.theme.entrywidget.EntryWidgetTheme
import com.google.gson.annotations.SerializedName

internal data class EntryWidgetRemoteConfig(
    @SerializedName("background")
    val background: LayerRemoteConfig?,
    @SerializedName("mediaTypeItems")
    val mediaTypeItems: MediaTypeItemsRemoteConfig?,
    @SerializedName("errorTitle")
    val errorTitle: TextRemoteConfig?,
    @SerializedName("errorMessage")
    val errorMessage: TextRemoteConfig?,
    @SerializedName("errorButton")
    val errorButton: ButtonRemoteConfig?
) {
    fun toEntryWidgetTheme(): EntryWidgetTheme = EntryWidgetTheme(
        background = background?.toLayerTheme(),
        mediaTypeItems = mediaTypeItems?.toMediaTypeItemsTheme(),
        errorTitle = errorTitle?.toTextTheme(),
        errorMessage = errorMessage?.toTextTheme(),
        errorButton = errorButton?.toButtonTheme()
    )
}
