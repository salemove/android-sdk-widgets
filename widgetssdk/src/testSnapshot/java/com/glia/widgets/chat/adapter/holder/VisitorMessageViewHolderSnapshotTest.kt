package com.glia.widgets.chat.adapter.holder

import com.glia.widgets.SnapshotTest
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.model.VisitorMessageItem
import com.glia.widgets.databinding.ChatVisitorMessageLayoutBinding
import com.glia.widgets.snapshotutils.SnapshotChatScreen
import com.glia.widgets.snapshotutils.SnapshotProviders
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import org.junit.Test

class VisitorMessageViewHolderSnapshotTest : SnapshotTest(), SnapshotChatScreen, SnapshotProviders {

    // MARK: without labels

    @Test
    fun withoutLabels() {
        snapshot(
            setupView(
                VisitorMessageItem("Visitor message text")
            ).viewHolder.itemView
        )
    }

    @Test
    fun withoutLabelsWithUiTheme() {
        snapshot(
            setupView(
                VisitorMessageItem("Visitor message text"),
                uiTheme = uiTheme()
            ).viewHolder.itemView
        )
    }

    @Test
    fun withoutLabelsWithGlobalColors() {
        snapshot(
            setupView(
                VisitorMessageItem("Visitor message text"),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).viewHolder.itemView
        )
    }

    @Test
    fun withoutLabelsWithUnifiedTheme() {
        snapshot(
            setupView(
                VisitorMessageItem("Visitor message text"),
                unifiedTheme = unifiedTheme()
            ).viewHolder.itemView
        )
    }

    @Test
    fun withoutLabelsWithUnifiedThemeWithoutVisitorMessage() {
        snapshot(
            setupView(
                VisitorMessageItem("Visitor message text"),
                unifiedTheme = unifiedThemeWithoutVisitorMessage()
            ).viewHolder.itemView
        )
    }

    // MARK: with delivered label

    @Test
    fun deliveredLabel() {
        snapshot(
            setupView(
                VisitorMessageItem("Visitor message text", showDelivered = true)
            ).viewHolder.itemView
        )
    }

    @Test
    fun deliveredLabelWithUiTheme() {
        snapshot(
            setupView(
                VisitorMessageItem("Visitor message text", showDelivered = true),
                uiTheme = uiTheme()
            ).viewHolder.itemView
        )
    }

    @Test
    fun deliveredLabelWithGlobalColors() {
        snapshot(
            setupView(
                VisitorMessageItem("Visitor message text", showDelivered = true),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).viewHolder.itemView
        )
    }

    @Test
    fun deliveredLabelWithUnifiedTheme() {
        snapshot(
            setupView(
                VisitorMessageItem("Visitor message text", showDelivered = true),
                unifiedTheme = unifiedTheme()
            ).viewHolder.itemView
        )
    }

    @Test
    fun deliveredLabelWithUnifiedThemeWithoutVisitorMessage() {
        snapshot(
            setupView(
                VisitorMessageItem("Visitor message text", showDelivered = true),
                unifiedTheme = unifiedThemeWithoutVisitorMessage()
            ).viewHolder.itemView
        )
    }

    // MARK: with error label

    @Test
    fun errorLabel() {
        snapshot(
            setupView(
                VisitorMessageItem("Visitor message text", showError = true)
            ).viewHolder.itemView
        )
    }

    @Test
    fun errorLabelWithUiTheme() {
        snapshot(
            setupView(
                VisitorMessageItem("Visitor message text", showError = true),
                uiTheme = uiTheme()
            ).viewHolder.itemView
        )
    }

    @Test
    fun errorLabelWithGlobalColors() {
        snapshot(
            setupView(
                VisitorMessageItem("Visitor message text", showError = true),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).viewHolder.itemView
        )
    }

    @Test
    fun errorLabelWithUnifiedTheme() {
        snapshot(
            setupView(
                VisitorMessageItem("Visitor message text", showError = true),
                unifiedTheme = unifiedTheme()
            ).viewHolder.itemView
        )
    }

    @Test
    fun errorLabelWithUnifiedThemeWithoutVisitorMessage() {
        snapshot(
            setupView(
                VisitorMessageItem("Visitor message text", showError = true),
                unifiedTheme = unifiedThemeWithoutVisitorMessage()
            ).viewHolder.itemView
        )
    }

    // MARK: utils for tests

    private data class ViewData(val binding: ChatVisitorMessageLayoutBinding, val viewHolder: VisitorMessageViewHolder)

    private fun setupView(
        item: VisitorMessageItem,
        unifiedTheme: UnifiedTheme? = null,
        uiTheme: UiTheme = UiTheme()
    ): ViewData {
        val binding = ChatVisitorMessageLayoutBinding.inflate(layoutInflater)
        val viewHolder = VisitorMessageViewHolder(binding, {}, uiTheme, unifiedTheme, stringProviderMock())

        viewHolder.bind(item)

        return ViewData(binding, viewHolder)
    }
}
