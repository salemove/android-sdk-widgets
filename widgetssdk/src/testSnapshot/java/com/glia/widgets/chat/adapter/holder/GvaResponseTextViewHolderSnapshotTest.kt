package com.glia.widgets.chat.adapter.holder

import com.glia.widgets.SnapshotTest
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.model.GvaResponseText
import com.glia.widgets.databinding.ChatOperatorMessageLayoutBinding
import com.glia.widgets.databinding.ChatReceiveMessageContentBinding
import com.glia.widgets.snapshotutils.SnapshotGva
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import org.junit.Test

class GvaResponseTextViewHolderSnapshotTest : SnapshotTest(), SnapshotGva {

    private fun gvaResponseText(showChatHead: Boolean = false) = GvaResponseText(
        content = gvaLongSubtitle(),
        showChatHead = showChatHead
    )

    // MARK: tests with all views

    @Test
    fun withoutChatHead() {
        snapshot(
            setupView(
                gvaResponseText()
            ).viewHolder.itemView
        )
    }

    @Test
    fun withoutChatHeadWithUiTheme() {
        snapshot(
            setupView(
                gvaResponseText(),
                uiTheme = uiTheme()
            ).viewHolder.itemView
        )
    }

    @Test
    fun withoutChatHeadWithGlobalColors() {
        snapshot(
            setupView(
                gvaResponseText(),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).viewHolder.itemView
        )
    }

    @Test
    fun withoutChatHeadWithUnifiedTheme() {
        snapshot(
            setupView(
                gvaResponseText(),
                unifiedTheme = unifiedTheme()
            ).viewHolder.itemView
        )
    }

    // MARK: tests with long content

    @Test
    fun withChatHead() {
        snapshot(
            setupView(
                gvaResponseText(
                    showChatHead = true
                ),
            ).viewHolder.itemView
        )
    }

    @Test
    fun withChatHeadWithUiTheme() {
        snapshot(
            setupView(
                gvaResponseText(
                    showChatHead = true
                ),
                uiTheme = uiTheme()
            ).viewHolder.itemView
        )
    }

    @Test
    fun withChatHeadWithGlobalColors() {
        snapshot(
            setupView(
                gvaResponseText(
                    showChatHead = true
                ),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).viewHolder.itemView
        )
    }

    @Test
    fun withChatHeadWithUnifiedTheme() {
        snapshot(
            setupView(
                gvaResponseText(
                    showChatHead = true
                ),
                unifiedTheme = unifiedTheme()
            ).viewHolder.itemView
        )
    }

    // MARK: utils for tests

    private data class ViewData(
        val chatOperatorMessageLayoutBinding: ChatOperatorMessageLayoutBinding,
        val gvaPersistentButtonsContentBinding: ChatReceiveMessageContentBinding,
        val viewHolder: GvaResponseTextViewHolder
    )

    private fun setupView(
        card: GvaResponseText,
        unifiedTheme: UnifiedTheme? = null,
        uiTheme: UiTheme = UiTheme()
    ): ViewData {
        val chatOperatorMessageLayoutBinding = ChatOperatorMessageLayoutBinding.inflate(layoutInflater)
        val gvaPersistentButtonsContentBinding = ChatReceiveMessageContentBinding.inflate(
            layoutInflater,
            chatOperatorMessageLayoutBinding.contentLayout,
            true
        )
        val viewHolder = GvaResponseTextViewHolder(
            chatOperatorMessageLayoutBinding,
            gvaPersistentButtonsContentBinding,
            uiTheme,
            unifiedTheme
        )

        viewHolder.bind(card)

        return ViewData(chatOperatorMessageLayoutBinding, gvaPersistentButtonsContentBinding, viewHolder)
    }
}
