package com.glia.widgets.snapshotutils

import com.glia.widgets.GliaWidgetsConfig
import com.glia.widgets.UiTheme
import com.glia.widgets.di.Dependencies
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import io.mockk.mockk

internal interface SnapshotThemeConfiguration : SnapshotTestLifecycle {

    fun setGlobalThemes(uiTheme: UiTheme?, unifiedTheme: UnifiedTheme?) {
        Dependencies.repositoryFactory = mockk(relaxed = true)

        Dependencies.configurationManager.applyConfiguration(GliaWidgetsConfig.Builder().setUiTheme(uiTheme).build())

        Dependencies.gliaThemeManager.theme = unifiedTheme

        setOnEndListener {
            Dependencies.configurationManager.applyConfiguration(GliaWidgetsConfig.Builder().build())
            Dependencies.gliaThemeManager.theme = null
        }
    }

}
