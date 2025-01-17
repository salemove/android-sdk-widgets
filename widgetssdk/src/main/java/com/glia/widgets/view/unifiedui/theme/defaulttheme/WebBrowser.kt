package com.glia.widgets.view.unifiedui.theme.defaulttheme

import com.glia.widgets.view.unifiedui.theme.ColorPallet
import com.glia.widgets.view.unifiedui.theme.webbrowser.WebBrowserTheme

/**
 * Default theme for Chat screen
 */
internal fun WebBrowserTheme(pallet: ColorPallet) = WebBrowserTheme(
    header = PrimaryColorHeaderTheme(pallet)
)
