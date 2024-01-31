package com.glia.widgets.chat.adapter.holder

import com.glia.widgets.R
import com.glia.widgets.SnapshotTest
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.model.OperatorMessageItem
import com.glia.widgets.databinding.ChatOperatorMessageLayoutBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.snapshotutils.SnapshotChatScreen
import com.glia.widgets.snapshotutils.SnapshotOperatorMessage
import com.glia.widgets.snapshotutils.SnapshotPicasso
import com.glia.widgets.snapshotutils.SnapshotProviders
import com.glia.widgets.snapshotutils.SnapshotStrings
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import com.google.gson.JsonObject
import org.junit.Test

class OperatorMessageViewHolderSnapshotTest : SnapshotTest(), SnapshotChatScreen, SnapshotProviders, SnapshotPicasso, SnapshotOperatorMessage, SnapshotStrings {

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
    fun plainTextWithUiTheme() {
        snapshot(
            setupView(
                operatorMessagePlainText(content = mediumLengthTexts()[0]),
                uiTheme = uiTheme()
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
    fun plainTextWithHeaderWithUiTheme() {
        snapshot(
            setupView(
                plainTextWithHeaderItem(),
                uiTheme = uiTheme()
            ).itemView
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
    fun responseCardWithUiTheme() {
        snapshot(
            setupView(
                responseCardItem(),
                uiTheme = uiTheme()
            ).itemView
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

    private fun setupView(
        item: OperatorMessageItem,
        unifiedTheme: UnifiedTheme? = null,
        uiTheme: UiTheme = UiTheme()
    ): OperatorMessageViewHolder {
        stringProviderMock()
        picassoMock(listOf(R.drawable.test_banner, R.drawable.test_launcher2))
        unifiedTheme?.let { Dependencies.getGliaThemeManager().theme = it }

        setOnEndListener {
            Dependencies.getGliaThemeManager().theme = null
        }

        return OperatorMessageViewHolder(
            ChatOperatorMessageLayoutBinding.inflate(layoutInflater),
            uiTheme
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
