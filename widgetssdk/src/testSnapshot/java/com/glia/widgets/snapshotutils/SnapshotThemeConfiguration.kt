package com.glia.widgets.snapshotutils

import com.glia.widgets.di.Dependencies
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import io.mockk.mockk

internal interface SnapshotThemeConfiguration : SnapshotTestLifecycle {

    fun setUnifiedTheme(unifiedTheme: UnifiedTheme?) {
        Dependencies.repositoryFactory = mockk(relaxed = true)

        Dependencies.gliaThemeManager.theme = unifiedTheme

        setOnEndListener {
            Dependencies.gliaThemeManager.theme = null
        }
    }

}
