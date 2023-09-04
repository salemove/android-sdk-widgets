package com.glia.widgets.view.unifiedui.theme

import androidx.annotation.VisibleForTesting
import com.glia.widgets.view.unifiedui.parse.RemoteConfigurationParser

internal class UnifiedThemeManager {
    private val parser: RemoteConfigurationParser by lazy { RemoteConfigurationParser() }

    @VisibleForTesting
    internal var theme: UnifiedTheme? = null

    fun applyJsonConfig(jsonConfig: String?) {
        theme = jsonConfig?.let(parser::parseRemoteConfiguration)?.toUnifiedTheme()
    }
}
