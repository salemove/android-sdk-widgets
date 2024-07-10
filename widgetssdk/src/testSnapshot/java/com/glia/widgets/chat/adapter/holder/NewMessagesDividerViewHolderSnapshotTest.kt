package com.glia.widgets.chat.adapter.holder

import com.glia.widgets.SnapshotTest
import com.glia.widgets.UiTheme
import com.glia.widgets.databinding.ChatNewMessagesDividerLayoutBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.snapshotutils.SnapshotChatScreen
import com.glia.widgets.snapshotutils.SnapshotChatView
import com.glia.widgets.snapshotutils.SnapshotProviders
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import org.junit.Test

class NewMessagesDividerViewHolderSnapshotTest : SnapshotTest(
    renderingMode = fullWidthRenderMode
), SnapshotChatScreen, SnapshotChatView, SnapshotProviders {

    // MARK: Tests

    @Test
    fun defaultTheme() {
        snapshot(
            setupView().itemView
        )
    }

    @Test
    fun withUiTheme() {
        snapshot(
            setupView(
                uiTheme = uiTheme()
            ).itemView
        )
    }

    @Test
    fun withGlobalColors() {
        snapshot(
            setupView(
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).itemView
        )
    }

    @Test
    fun withUnifiedTheme() {
        snapshot(
            setupView(
                unifiedTheme = unifiedTheme()
            ).itemView
        )
    }

    @Test
    fun withUnifiedThemeWithoutChat() {
        snapshot(
            setupView(
                unifiedTheme = unifiedThemeWithoutChat()
            ).itemView
        )
    }

    // MARK: utils for tests

    private fun setupView(
        unifiedTheme: UnifiedTheme? = null,
        uiTheme: UiTheme = UiTheme()
    ): NewMessagesDividerViewHolder {
        localeProviderMock()
        unifiedTheme?.let { Dependencies.getGliaThemeManager().theme = it }

        setOnEndListener {
            Dependencies.getGliaThemeManager().theme = null
        }

        return NewMessagesDividerViewHolder(
            ChatNewMessagesDividerLayoutBinding.inflate(layoutInflater),
            uiTheme
        )
    }
}
