package com.glia.widgets.chat.adapter.holder

import com.glia.widgets.R
import com.glia.widgets.SnapshotTest
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.model.OperatorStatusItem
import com.glia.widgets.databinding.ChatOperatorStatusLayoutBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.snapshotutils.SnapshotChatScreen
import com.glia.widgets.snapshotutils.SnapshotChatView
import com.glia.widgets.snapshotutils.SnapshotLottie
import com.glia.widgets.snapshotutils.SnapshotCoil
import com.glia.widgets.snapshotutils.SnapshotProviders
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import org.junit.Test

internal class OperatorStatusViewHolderSnapshotTest : SnapshotTest(
    renderingMode = fullWidthRenderMode
), SnapshotChatScreen, SnapshotChatView, SnapshotProviders, SnapshotCoil, SnapshotLottie {

    // MARK: Connected

    private fun connectedItem() = OperatorStatusItem.Connected(
        "Screen Shot",
        "https://test.url"
    )

    @Test
    fun connected() {
        snapshot(
            setupView(
                connectedItem()
            ).viewHolder.itemView
        )
    }

    @Test
    fun connectedWithGlobalColors() {
        snapshot(
            setupView(
                connectedItem(),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).viewHolder.itemView
        )
    }

    @Test
    fun connectedWithUnifiedTheme() {
        snapshot(
            setupView(
                connectedItem(),
                unifiedTheme = unifiedTheme()
            ).viewHolder.itemView
        )
    }

    @Test
    fun connectedWithUnifiedThemeWithoutChatConnect() {
        snapshot(
            setupView(
                connectedItem(),
                unifiedTheme = unifiedThemeWithoutChatConnect()
            ).viewHolder.itemView
        )
    }

    // MARK: Connected with image load error

    @Test
    fun connectedWithImageError() {
        snapshot(
            setupView(
                item = connectedItem(),
                imageLoadError = true
            ).viewHolder.itemView
        )
    }

    // MARK: In Queue

    private fun inQueueItem() = OperatorStatusItem.InQueue

    @Test
    fun inQueue() {
        snapshot(
            setupView(
                inQueueItem()
            ).viewHolder.itemView
        )
    }

    @Test
    fun inQueueWithGlobalColors() {
        snapshot(
            setupView(
                inQueueItem(),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).viewHolder.itemView
        )
    }

    @Test
    fun inQueueWithUnifiedTheme() {
        snapshot(
            setupView(
                inQueueItem(),
                unifiedTheme = unifiedTheme()
            ).viewHolder.itemView
        )
    }

    @Test
    fun inQueueWithUnifiedThemeWithoutChatConnect() {
        snapshot(
            setupView(
                inQueueItem(),
                unifiedTheme = unifiedThemeWithoutChatConnect()
            ).viewHolder.itemView
        )
    }

    // MARK: In Queue

    private fun joinedItem() = OperatorStatusItem.Joined(
        "Screen Shot",
        null
    )

    @Test
    fun joined() {
        snapshot(
            setupView(
                joinedItem()
            ).viewHolder.itemView
        )
    }

    @Test
    fun joinedWithGlobalColors() {
        snapshot(
            setupView(
                joinedItem(),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).viewHolder.itemView
        )
    }

    @Test
    fun joinedWithUnifiedTheme() {
        snapshot(
            setupView(
                joinedItem(),
                unifiedTheme = unifiedTheme()
            ).viewHolder.itemView
        )
    }

    @Test
    fun joinedWithUnifiedThemeWithoutChatConnect() {
        snapshot(
            setupView(
                joinedItem(),
                unifiedTheme = unifiedThemeWithoutChatConnect()
            ).viewHolder.itemView
        )
    }

    // MARK: Transferring

    @Test
    fun transferring() {
        snapshot(
            setupView(
                OperatorStatusItem.Transferring
            ).viewHolder.itemView
        )
    }

    @Test
    fun transferringWithGlobalColors() {
        snapshot(
            setupView(
                OperatorStatusItem.Transferring,
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).viewHolder.itemView
        )
    }

    @Test
    fun transferringWithUnifiedTheme() {
        snapshot(
            setupView(
                OperatorStatusItem.Transferring,
                unifiedTheme = unifiedTheme()
            ).viewHolder.itemView
        )
    }

    @Test
    fun transferringWithUnifiedThemeWithoutChatConnect() {
        snapshot(
            setupView(
                OperatorStatusItem.Transferring,
                unifiedTheme = unifiedThemeWithoutChatConnect()
            ).viewHolder.itemView
        )
    }

    // MARK: utils for tests

    private data class ViewData(val binding: ChatOperatorStatusLayoutBinding, val viewHolder: OperatorStatusViewHolder)

    private fun setupView(
        item: OperatorStatusItem,
        imageLoadError: Boolean = false,
        unifiedTheme: UnifiedTheme? = null
    ): ViewData {
        lottieMock()
        mockCoil(listOf(R.drawable.test_banner), imageLoadError = imageLoadError)

        unifiedTheme?.let { Dependencies.gliaThemeManager.theme = it }

        val binding = ChatOperatorStatusLayoutBinding.inflate(layoutInflater)
        val viewHolder = OperatorStatusViewHolder(binding, UiTheme())

        viewHolder.bind(item)

        setOnEndListener {
            Dependencies.gliaThemeManager.theme = null
        }

        return ViewData(binding, viewHolder)
    }
}
