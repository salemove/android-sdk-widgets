package com.glia.widgets.webbrowser

import com.android.ide.common.rendering.api.SessionParams
import com.glia.widgets.R
import com.glia.widgets.SnapshotTest
import com.glia.widgets.di.Dependencies
import com.glia.widgets.locale.LocaleString
import com.glia.widgets.snapshotutils.SnapshotProviders
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import org.junit.Test

class WebBrowserViewSnapshotTest : SnapshotTest(
    renderingMode = SessionParams.RenderingMode.FULL_EXPAND
), SnapshotProviders {

    // MARK: tests

    @Test
    fun defaultTheme() {
        snapshot(
            setupView()
        )
    }

    @Test
    fun defaultThemeWithGlobalColors() {
        snapshot(
            setupView(unifiedTheme = unifiedThemeWithGlobalColors())
        )
    }

    @Test
    fun defaultThemeWithUnifiedTheme() {
        snapshot(
            setupView(unifiedTheme = unifiedTheme())
        )
    }

    @Test
    fun defaultThemeWithUnifiedThemeWithoutWebBrowser() {
        snapshot(
            setupView(unifiedTheme = unifiedThemeWithoutWebBrowser())
        )
    }

    // MARK: utils for tests

    private fun setupView(
        title: LocaleString = LocaleString(R.string.dialog_link2_text),
        unifiedTheme: UnifiedTheme? = null
    ): WebBrowserView {
        localeProviderMock()
        resourceProviderMock()

        unifiedTheme?.let { Dependencies.getGliaThemeManager().theme = it }

        setOnEndListener {
            Dependencies.getGliaThemeManager().theme = null
        }

        return WebBrowserView(context).also {
            it.setTitle(title)
        }
    }

    private fun unifiedThemeWithoutWebBrowser(): UnifiedTheme = unifiedTheme(R.raw.test_unified_config) { unifiedTheme ->
        unifiedTheme.remove("webBrowserScreen")
    }
}
