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
                VisitorMessageItem("Visitor message text", "ID")
            ).viewHolder.itemView
        )
    }

    @Test
    fun withoutLabelsWithUiTheme() {
        snapshot(
            setupView(
                VisitorMessageItem("Visitor message text", "ID"),
                uiTheme = uiTheme()
            ).viewHolder.itemView
        )
    }

    @Test
    fun withoutLabelsWithGlobalColors() {
        snapshot(
            setupView(
                VisitorMessageItem("Visitor message text", "ID"),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).viewHolder.itemView
        )
    }

    @Test
    fun withoutLabelsWithUnifiedTheme() {
        snapshot(
            setupView(
                VisitorMessageItem("Visitor message text", "ID"),
                unifiedTheme = unifiedTheme()
            ).viewHolder.itemView
        )
    }

    @Test
    fun withoutLabelsWithUnifiedThemeWithoutVisitorMessage() {
        snapshot(
            setupView(
                VisitorMessageItem("Visitor message text", "ID"),
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
        val viewHolder = VisitorMessageViewHolder(binding, {}, uiTheme, unifiedTheme, localeProviderMock())

        viewHolder.bind(item)

        return ViewData(binding, viewHolder)
    }
}
