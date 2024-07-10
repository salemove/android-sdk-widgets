package com.glia.widgets.chat.adapter.holder

import com.glia.widgets.SnapshotTest
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.model.GvaButton
import com.glia.widgets.chat.model.GvaPersistentButtons
import com.glia.widgets.databinding.ChatGvaPersistentButtonsContentBinding
import com.glia.widgets.databinding.ChatOperatorMessageLayoutBinding
import com.glia.widgets.snapshotutils.SnapshotGva
import com.glia.widgets.snapshotutils.SnapshotProviders
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import org.junit.Test

class GvaPersistentButtonsViewHolderSnapshotTest : SnapshotTest(), SnapshotGva, SnapshotProviders {

    private fun gvaPersistentButtons(showChatHead: Boolean = false) = GvaPersistentButtons(
        content = gvaLongSubtitle(),
        options = mediumLengthTexts().map { GvaButton(it) },
        showChatHead = showChatHead
    )

    // MARK: tests with all views

    @Test
    fun withoutChatHead() {
        snapshot(
            setupView(
                gvaPersistentButtons()
            ).viewHolder.itemView
        )
    }

    @Test
    fun withoutChatHeadWithUiTheme() {
        snapshot(
            setupView(
                gvaPersistentButtons(),
                uiTheme = uiTheme()
            ).viewHolder.itemView
        )
    }

    @Test
    fun withoutChatHeadWithGlobalColors() {
        snapshot(
            setupView(
                gvaPersistentButtons(),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).viewHolder.itemView
        )
    }

    @Test
    fun withoutChatHeadWithUnifiedTheme() {
        snapshot(
            setupView(
                gvaPersistentButtons(),
                unifiedTheme = unifiedTheme()
            ).viewHolder.itemView
        )
    }

    @Test
    fun withoutChatHeadUnifiedThemeWithoutGva() {
        snapshot(
            setupView(
                gvaPersistentButtons(),
                unifiedTheme = unifiedThemeWithoutGva()
            ).viewHolder.itemView
        )
    }

    // MARK: tests with long content

    @Test
    fun withChatHead() {
        snapshot(
            setupView(
                gvaPersistentButtons(
                    showChatHead = true
                )
            ).viewHolder.itemView
        )
    }

    @Test
    fun withChatHeadWithUiTheme() {
        snapshot(
            setupView(
                gvaPersistentButtons(
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
                gvaPersistentButtons(
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
                gvaPersistentButtons(
                    showChatHead = true
                ),
                unifiedTheme = unifiedTheme()
            ).viewHolder.itemView
        )
    }

    @Test
    fun withChatHeadUnifiedThemeWithoutGva() {
        snapshot(
            setupView(
                gvaPersistentButtons(
                    showChatHead = true
                ),
                unifiedTheme = unifiedThemeWithoutGva()
            ).viewHolder.itemView
        )
    }

    // MARK: utils for tests

    private data class ViewData(
        val chatOperatorMessageLayoutBinding: ChatOperatorMessageLayoutBinding,
        val gvaPersistentButtonsContentBinding: ChatGvaPersistentButtonsContentBinding,
        val viewHolder: GvaPersistentButtonsViewHolder
    )

    private fun setupView(
        card: GvaPersistentButtons,
        unifiedTheme: UnifiedTheme? = null,
        uiTheme: UiTheme = UiTheme()
    ): ViewData {
        localeProviderMock()

        val chatOperatorMessageLayoutBinding = ChatOperatorMessageLayoutBinding.inflate(layoutInflater)
        val gvaPersistentButtonsContentBinding = ChatGvaPersistentButtonsContentBinding.inflate(
            layoutInflater,
            chatOperatorMessageLayoutBinding.contentLayout,
            true
        )
        val viewHolder = GvaPersistentButtonsViewHolder(
            chatOperatorMessageLayoutBinding,
            gvaPersistentButtonsContentBinding,
            {},
            uiTheme,
            unifiedTheme
        )

        viewHolder.bind(card)

        return ViewData(chatOperatorMessageLayoutBinding, gvaPersistentButtonsContentBinding, viewHolder)
    }
}
