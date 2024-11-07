package com.glia.widgets.chat.adapter.holder

import com.glia.widgets.SnapshotTest
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.model.VisitorItemStatus
import com.glia.widgets.chat.model.VisitorMessageItem
import com.glia.widgets.databinding.ChatVisitorMessageLayoutBinding
import com.glia.widgets.snapshotutils.SnapshotChatScreen
import com.glia.widgets.snapshotutils.SnapshotProviders
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import org.junit.Test

internal class VisitorMessageViewHolderSnapshotTest : SnapshotTest(), SnapshotChatScreen, SnapshotProviders {

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

    // MARK: with delivered label

    @Test
    fun deliveredLabel() {
        snapshot(
            setupView(
                VisitorMessageItem("Visitor message text", "ID", status = VisitorItemStatus.DELIVERED)
            ).viewHolder.itemView
        )
    }

    @Test
    fun deliveredLabelWithGlobalColors() {
        snapshot(
            setupView(
                VisitorMessageItem("Visitor message text", "ID", status = VisitorItemStatus.DELIVERED),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).viewHolder.itemView
        )
    }

    @Test
    fun deliveredLabelWithUnifiedTheme() {
        snapshot(
            setupView(
                VisitorMessageItem("Visitor message text", "ID", status = VisitorItemStatus.DELIVERED),
                unifiedTheme = unifiedTheme()
            ).viewHolder.itemView
        )
    }

    @Test
    fun deliveredLabelWithUnifiedThemeWithoutVisitorMessage() {
        snapshot(
            setupView(
                VisitorMessageItem("Visitor message text", "ID", status = VisitorItemStatus.DELIVERED),
                unifiedTheme = unifiedThemeWithoutVisitorMessage()
            ).viewHolder.itemView
        )
    }

    // MARK: with error label

    @Test
    fun errorLabel() {
        snapshot(
            setupView(
                VisitorMessageItem("Visitor message text", "ID", status = VisitorItemStatus.ERROR_INDICATOR)
            ).viewHolder.itemView
        )
    }

    @Test
    fun errorLabelWithGlobalColors() {
        snapshot(
            setupView(
                VisitorMessageItem("Visitor message text", "ID", status = VisitorItemStatus.ERROR_INDICATOR),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).viewHolder.itemView
        )
    }

    @Test
    fun errorLabelWithUnifiedTheme() {
        snapshot(
            setupView(
                VisitorMessageItem("Visitor message text", "ID", status = VisitorItemStatus.ERROR_INDICATOR),
                unifiedTheme = unifiedTheme()
            ).viewHolder.itemView
        )
    }

    @Test
    fun errorLabelWithUnifiedThemeWithoutVisitorMessage() {
        snapshot(
            setupView(
                VisitorMessageItem("Visitor message text", "ID", status = VisitorItemStatus.ERROR_INDICATOR),
                unifiedTheme = unifiedThemeWithoutVisitorMessage()
            ).viewHolder.itemView
        )
    }

    // MARK: utils for tests

    private data class ViewData(val binding: ChatVisitorMessageLayoutBinding, val viewHolder: VisitorMessageViewHolder)

    private fun setupView(item: VisitorMessageItem, unifiedTheme: UnifiedTheme? = null): ViewData {
        val binding = ChatVisitorMessageLayoutBinding.inflate(layoutInflater)
        val viewHolder = VisitorMessageViewHolder(binding, {}, UiTheme(), unifiedTheme, localeProviderMock())

        viewHolder.bind(item)

        return ViewData(binding, viewHolder)
    }
}
