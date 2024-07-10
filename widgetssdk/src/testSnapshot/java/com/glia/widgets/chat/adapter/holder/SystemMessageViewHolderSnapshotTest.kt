package com.glia.widgets.chat.adapter.holder

import com.glia.widgets.SnapshotTest
import com.glia.widgets.UiTheme
import com.glia.widgets.databinding.ChatReceiveMessageContentBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.snapshotutils.SnapshotChatScreen
import com.glia.widgets.snapshotutils.SnapshotProviders
import com.glia.widgets.snapshotutils.SnapshotStrings
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import org.junit.Test

class SystemMessageViewHolderSnapshotTest : SnapshotTest(), SnapshotChatScreen, SnapshotProviders, SnapshotStrings {

    // MARK: Tests

    @Test
    fun defaultView() {
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
    fun withUnifiedThemeWithoutOperatorMessage() {
        snapshot(
            setupView(
                unifiedTheme = unifiedThemeWithoutOperatorMessage()
            ).itemView
        )
    }

    // MARK: utils for tests

    private fun setupView(
        message: String = mediumLengthTexts().joinToString(separator = " "),
        unifiedTheme: UnifiedTheme? = null,
        uiTheme: UiTheme = UiTheme()
    ): SystemMessageViewHolder {
        localeProviderMock()
        unifiedTheme?.let { Dependencies.getGliaThemeManager().theme = it }

        setOnEndListener {
            Dependencies.getGliaThemeManager().theme = null
        }

        return SystemMessageViewHolder(
            ChatReceiveMessageContentBinding.inflate(layoutInflater),
            uiTheme
        ).also { viewHolder ->
            viewHolder.bind(message)
        }
    }
}
