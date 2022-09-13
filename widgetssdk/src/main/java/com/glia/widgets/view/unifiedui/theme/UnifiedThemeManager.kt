package com.glia.widgets.view.unifiedui.theme

import com.glia.widgets.view.unifiedui.parse.RemoteConfigurationParser

internal class UnifiedThemeManager {
    private val parser: RemoteConfigurationParser by lazy { RemoteConfigurationParser() }

    private var _theme: UnifiedTheme? = null
    val theme: UnifiedTheme?
        get() = _theme

    fun applyJsonConfig(jsonConfig: String?) {
        _theme = jsonConfig?.let(parser::parseRemoteConfiguration)?.toUnifiedTheme()
    }

}