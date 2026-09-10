package com.glia.widgets.chat.adapter.holder

import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import com.glia.widgets.R
import com.glia.widgets.chat.model.MediaUpgradeStartedTimerItem
import com.glia.widgets.databinding.ChatMediaUpgradeLayoutBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.applyGliaThemeFont
import com.glia.widgets.helper.gliaAttrDrawableRes
import com.glia.widgets.helper.setLocaleText
import com.glia.widgets.view.unifiedui.applyCardLayerTheme
import com.glia.widgets.view.unifiedui.applyImageColorTheme
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.chat.ChatTheme
import com.glia.widgets.view.unifiedui.theme.chat.MediaUpgradeTheme

internal class MediaUpgradeStartedViewHolder(
    private val binding: ChatMediaUpgradeLayoutBinding
) : RecyclerView.ViewHolder(binding.root) {

    /**
     * Resolved here rather than in the layout because which of the two applies is only known at bind
     * time. Every other value on the card - the surface, stroke, icon tint and both text colours -
     * comes from the layout and its text appearances.
     */
    @DrawableRes
    private val upgradeAudioIcon: Int =
        itemView.context.gliaAttrDrawableRes(R.attr.gliaIconChatAudioUpgrade, R.drawable.ic_baseline_mic)

    @DrawableRes
    private val upgradeVideoIcon: Int =
        itemView.context.gliaAttrDrawableRes(R.attr.gliaIconChatVideoUpgrade, R.drawable.ic_baseline_videocam)

    private val chatTheme: ChatTheme? by lazy {
        Dependencies.gliaThemeManager.theme?.chatTheme
    }

    init {
        itemView.context.applyGliaThemeFont(binding.titleView, binding.timerView)
    }

    private fun setMediaUpgradeTheme(mediaUpgradeTheme: MediaUpgradeTheme?) {
        binding.cardView.applyCardLayerTheme(mediaUpgradeTheme?.background)
        binding.iconView.applyImageColorTheme(mediaUpgradeTheme?.iconColor)
        binding.titleView.applyTextTheme(mediaUpgradeTheme?.text)
        binding.timerView.applyTextTheme(mediaUpgradeTheme?.description)
    }

    fun updateTime(time: String) {
        binding.timerView.text = time
    }

    fun bind(chatItem: MediaUpgradeStartedTimerItem) {
        when (chatItem) {
            is MediaUpgradeStartedTimerItem.Audio -> {
                binding.iconView.setImageResource(upgradeAudioIcon)
                binding.titleView.setLocaleText(R.string.chat_media_upgrade_audio_system_message)
                setMediaUpgradeTheme(chatTheme?.audioUpgrade)
            }

            is MediaUpgradeStartedTimerItem.Video -> {
                binding.iconView.setImageResource(upgradeVideoIcon)
                binding.titleView.setLocaleText(R.string.chat_media_upgrade_video_system_message)
                setMediaUpgradeTheme(chatTheme?.videoUpgrade)
            }
        }

        binding.timerView.text = chatItem.time
    }
}
