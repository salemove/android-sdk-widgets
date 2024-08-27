package com.glia.widgets.chat.adapter.holder

import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.model.MediaUpgradeStartedTimerItem
import com.glia.widgets.databinding.ChatMediaUpgradeLayoutBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.getColorCompat
import com.glia.widgets.helper.getColorStateListCompat
import com.glia.widgets.helper.getFontCompat
import com.glia.widgets.helper.setLocaleText
import com.glia.widgets.view.unifiedui.applyCardLayerTheme
import com.glia.widgets.view.unifiedui.applyImageColorTheme
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.chat.ChatTheme
import com.glia.widgets.view.unifiedui.theme.chat.MediaUpgradeTheme

internal class MediaUpgradeStartedViewHolder(
    private val binding: ChatMediaUpgradeLayoutBinding,
    uiTheme: UiTheme
) : RecyclerView.ViewHolder(binding.root) {

    @DrawableRes
    private val upgradeAudioIcon: Int? = uiTheme.iconChatAudioUpgrade

    @DrawableRes
    private val upgradeVideoIcon: Int? = uiTheme.iconChatVideoUpgrade

    private val chatTheme: ChatTheme? by lazy {
        Dependencies.gliaThemeManager.theme?.chatTheme
    }

    init {
        uiTheme.baseLightColor?.let(itemView::getColorCompat)
            ?.also(binding.cardView::setCardBackgroundColor)

        uiTheme.baseShadeColor?.let(itemView::getColorCompat)
            ?.also(binding.cardView::setStrokeColor)

        uiTheme.brandPrimaryColor?.let(itemView::getColorStateListCompat)
            ?.also(binding.iconView::setImageTintList)

        uiTheme.baseDarkColor?.let(itemView::getColorCompat)?.also(binding.titleView::setTextColor)

        uiTheme.baseNormalColor?.let(itemView::getColorCompat)
            ?.also(binding.timerView::setTextColor)

        if (uiTheme.fontRes != null) {
            val fontFamily = itemView.getFontCompat(uiTheme.fontRes)
            binding.titleView.typeface = fontFamily
            binding.timerView.typeface = fontFamily
        }
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
                upgradeAudioIcon?.also(binding.iconView::setImageResource)
                binding.titleView.setLocaleText(R.string.chat_media_upgrade_audio_system_message)
                setMediaUpgradeTheme(chatTheme?.audioUpgrade)
            }

            is MediaUpgradeStartedTimerItem.Video -> {
                upgradeVideoIcon?.also(binding.iconView::setImageResource)
                binding.titleView.setLocaleText(R.string.chat_media_upgrade_video_system_message)
                setMediaUpgradeTheme(chatTheme?.videoUpgrade)
            }
        }

        binding.timerView.text = chatItem.time
    }
}
