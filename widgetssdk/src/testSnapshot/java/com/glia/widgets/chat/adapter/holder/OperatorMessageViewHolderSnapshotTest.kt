package com.glia.widgets.chat.adapter.holder

import com.glia.widgets.R
import com.glia.widgets.SnapshotTest
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.model.OperatorMessageItem
import com.glia.widgets.databinding.ChatOperatorMessageLayoutBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.snapshotutils.SnapshotChatScreen
import com.glia.widgets.snapshotutils.SnapshotOperatorMessage
import com.glia.widgets.snapshotutils.SnapshotCoil
import com.glia.widgets.snapshotutils.SnapshotProviders
import com.glia.widgets.snapshotutils.SnapshotStrings
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import com.google.gson.JsonObject
import org.junit.Test

internal class OperatorMessageViewHolderSnapshotTest : SnapshotTest(), SnapshotChatScreen, SnapshotProviders, SnapshotCoil, SnapshotOperatorMessage,
    SnapshotStrings {

    // MARK: Plain text

    @Test
    fun plainText() {
        snapshot(
            setupView(
                operatorMessagePlainText(content = mediumLengthTexts()[0])
            ).itemView
        )
    }

    @Test
    fun plainTextWithGlobalColors() {
        snapshot(
            setupView(
                operatorMessagePlainText(content = mediumLengthTexts()[0]),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).itemView
        )
    }

    @Test
    fun plainTextWithUnifiedTheme() {
        snapshot(
            setupView(
                operatorMessagePlainText(content = mediumLengthTexts()[0]),
                unifiedTheme = unifiedTheme()
            ).itemView
        )
    }

    @Test
    fun plainTextWithUnifiedThemeWithoutOperatorMessage() {
        snapshot(
            setupView(
                operatorMessagePlainText(content = mediumLengthTexts()[0]),
                unifiedTheme = unifiedThemeWithoutOperatorMessage()
            ).itemView
        )
    }

    // MARK: Plain text with header

    private fun plainTextWithHeaderItem() = operatorMessagePlainText(
        content = mediumLengthTexts()[0],
        showChatHead = true
    )

    @Test
    fun plainTextWithHeader() {
        snapshot(
            setupView(plainTextWithHeaderItem()).itemView
        )
    }

    @Test
    fun plainTextWithHeaderWithGlobalColors() {
        snapshot(
            setupView(
                plainTextWithHeaderItem(),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).itemView
        )
    }

    @Test
    fun plainTextWithHeaderWithUnifiedTheme() {
        snapshot(
            setupView(
                plainTextWithHeaderItem(),
                unifiedTheme = unifiedTheme()
            ).itemView
        )
    }

    @Test
    fun plainTextWithHeaderWithUnifiedThemeWithoutOperatorMessage() {
        snapshot(
            setupView(
                plainTextWithHeaderItem(),
                unifiedTheme = unifiedThemeWithoutOperatorMessage()
            ).itemView
        )
    }

    // MARK: Response card

    private fun responseCardItem() = operatorMessageResponseCard(
        content = mediumLengthTexts()[0],
        choiceCardImageUrl = "https://card.url",
        showChatHead = true,
        operatorProfileImgUrl = "https://test.link"
    )

    @Test
    fun responseCard() {
        snapshot(
            setupView(responseCardItem()).itemView
        )
    }

    @Test
    fun responseCardWithGlobalColors() {
        snapshot(
            setupView(
                responseCardItem(),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).itemView
        )
    }

    @Test
    fun responseCardWithUnifiedTheme() {
        snapshot(
            setupView(
                responseCardItem(),
                unifiedTheme = unifiedTheme()
            ).itemView
        )
    }

    @Test
    fun responseCardWithUnifiedThemeWithoutResponseCard() {
        snapshot(
            setupView(
                responseCardItem(),
                unifiedTheme = unifiedThemeWithoutResponseCard()
            ).itemView
        )
    }

    // MARK: utils for tests

    private fun setupView(item: OperatorMessageItem, unifiedTheme: UnifiedTheme? = null): OperatorMessageViewHolder {
        mockCoil(listOf(R.drawable.test_banner, R.drawable.test_launcher2))
        unifiedTheme?.let { Dependencies.gliaThemeManager.theme = it }

        setOnEndListener {
            Dependencies.gliaThemeManager.theme = null
        }

        return OperatorMessageViewHolder(
            ChatOperatorMessageLayoutBinding.inflate(layoutInflater),
            UiTheme()
        ).also { viewHolder ->
            viewHolder.bind(item) { _, _ -> }
        }
    }

    private fun unifiedThemeWithoutResponseCard(): UnifiedTheme = unifiedTheme(R.raw.test_unified_config) { unifiedTheme ->
        unifiedTheme.add(
            "chatScreen",
            (unifiedTheme.remove("chatScreen") as JsonObject).also {
                it.remove("responseCard")
            }
        )
    }
}
