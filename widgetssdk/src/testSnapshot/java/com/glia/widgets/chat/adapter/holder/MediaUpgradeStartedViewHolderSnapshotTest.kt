package com.glia.widgets.chat.adapter.holder

import com.glia.widgets.R
import com.glia.widgets.SnapshotTest
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.model.MediaUpgradeStartedTimerItem
import com.glia.widgets.databinding.ChatMediaUpgradeLayoutBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.snapshotutils.SnapshotChatScreen
import com.glia.widgets.snapshotutils.SnapshotProviders
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import com.google.gson.JsonObject
import org.junit.Test

class MediaUpgradeStartedViewHolderSnapshotTest : SnapshotTest(), SnapshotChatScreen, SnapshotProviders {

    // MARK: Audio

    @Test
    fun audio() {
        snapshot(
            setupView(MediaUpgradeStartedTimerItem.Audio()).itemView
        )
    }

    @Test
    fun audioUiTheme() {
        snapshot(
            setupView(
                MediaUpgradeStartedTimerItem.Audio(),
                uiTheme = uiTheme()
            ).itemView
        )
    }

    @Test
    fun audioWithGlobalColors() {
        snapshot(
            setupView(
                MediaUpgradeStartedTimerItem.Audio(),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).itemView
        )
    }

    @Test
    fun audioWithUnifiedTheme() {
        snapshot(
            setupView(
                MediaUpgradeStartedTimerItem.Audio(),
                unifiedTheme = unifiedTheme()
            ).itemView
        )
    }

    @Test
    fun audioWithUnifiedThemeWithoutAudioUpgrade() {
        snapshot(
            setupView(
                MediaUpgradeStartedTimerItem.Audio(),
                unifiedTheme = unifiedThemeWithoutAudioUpgrade()
            ).itemView
        )
    }

    // MARK: Video

    @Test
    fun video() {
        snapshot(
            setupView(MediaUpgradeStartedTimerItem.Video()).itemView
        )
    }

    @Test
    fun videoUiTheme() {
        snapshot(
            setupView(
                MediaUpgradeStartedTimerItem.Video(),
                uiTheme = uiTheme()
            ).itemView
        )
    }

    @Test
    fun videoWithGlobalColors() {
        snapshot(
            setupView(
                MediaUpgradeStartedTimerItem.Video(),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).itemView
        )
    }

    @Test
    fun videoWithUnifiedTheme() {
        snapshot(
            setupView(
                MediaUpgradeStartedTimerItem.Video(),
                unifiedTheme = unifiedTheme()
            ).itemView
        )
    }

    @Test
    fun videoWithUnifiedThemeWithoutVideoUpgrade() {
        snapshot(
            setupView(
                MediaUpgradeStartedTimerItem.Video(),
                unifiedTheme = unifiedThemeWithoutVideoUpgrade()
            ).itemView
        )
    }

    // MARK: utils for tests

    private fun setupView(
        item: MediaUpgradeStartedTimerItem,
        unifiedTheme: UnifiedTheme? = null,
        uiTheme: UiTheme = UiTheme(iconChatVideoUpgrade = R.drawable.ic_baseline_videocam)
    ): MediaUpgradeStartedViewHolder {
        localeProviderMock()
        unifiedTheme?.let { Dependencies.gliaThemeManager.theme = it }

        setOnEndListener {
            Dependencies.gliaThemeManager.theme = null
        }

        return MediaUpgradeStartedViewHolder(
            ChatMediaUpgradeLayoutBinding.inflate(layoutInflater),
            uiTheme
        ).also { viewHolder ->
            viewHolder.bind(item)
        }
    }

    private fun unifiedThemeWithoutAudioUpgrade(): UnifiedTheme = unifiedTheme(R.raw.test_unified_config) { unifiedTheme ->
        unifiedTheme.add(
            "chatScreen",
            (unifiedTheme.remove("chatScreen") as JsonObject).also {
                it.remove("audioUpgrade")
            }
        )
    }

    private fun unifiedThemeWithoutVideoUpgrade(): UnifiedTheme = unifiedTheme(R.raw.test_unified_config) { unifiedTheme ->
        unifiedTheme.add(
            "chatScreen",
            (unifiedTheme.remove("chatScreen") as JsonObject).also {
                it.remove("videoUpgrade")
            }
        )
    }
}
