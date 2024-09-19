package com.glia.widgets.view.unifiedui.config.webbrowser

import com.glia.widgets.view.unifiedui.config.base.HeaderRemoteConfig
import com.glia.widgets.view.unifiedui.theme.webbrowser.WebBrowserTheme
import com.google.gson.annotations.SerializedName

internal data class WebBrowserRemoteConfig(
    @SerializedName("header")
    val headerRemoteConfig: HeaderRemoteConfig?,
) {
    fun toWebBrowserTheme(): WebBrowserTheme = WebBrowserTheme(
        header = headerRemoteConfig?.toHeaderTheme()
    )
}
