package com.glia.widgets.view.unifiedui.webbrowser

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge
import com.glia.widgets.view.unifiedui.theme.base.HeaderTheme

internal data class WebBrowserTheme(
    val header: HeaderTheme? = null
) : Mergeable<WebBrowserTheme> {
    override fun merge(other: WebBrowserTheme): WebBrowserTheme = WebBrowserTheme(header = header merge other.header)
}
