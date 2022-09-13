package com.glia.widgets.chat.adapter.holder

import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.model.history.MediaUpgradeStartedTimerItem
import com.glia.widgets.databinding.ChatMediaUpgradeLayoutBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.view.unifiedui.exstensions.*
import com.glia.widgets.view.unifiedui.theme.chat.ChatTheme
import com.glia.widgets.view.unifiedui.theme.chat.UpgradeTheme

class MediaUpgradeStartedViewHolder(
    private val binding: ChatMediaUpgradeLayoutBinding, uiTheme: UiTheme
) : RecyclerView.ViewHolder(binding.root) {

    @DrawableRes
    private val upgradeAudioIcon: Int? = uiTheme.iconChatAudioUpgrade

    @DrawableRes
    private val upgradeVideoIcon: Int? = uiTheme.iconChatVideoUpgrade

    private val chatTheme: ChatTheme? by lazy {
        Dependencies.getGliaThemeManager().theme?.chatTheme
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

    private fun setUpgradeTheme(upgradeTheme: UpgradeTheme?) {
        binding.cardView.applyCardLayerTheme(upgradeTheme?.background)
        binding.iconView.applyImageColorTheme(upgradeTheme?.iconColor)
        binding.titleView.applyTextTheme(upgradeTheme?.text)
        binding.timerView.applyTextTheme(upgradeTheme?.description)
    }

    fun updateTime(time: String) {
        binding.timerView.text = time
    }

    fun bind(chatItem: MediaUpgradeStartedTimerItem) {
        if (chatItem.type == MediaUpgradeStartedTimerItem.Type.AUDIO) {
            upgradeAudioIcon?.also(binding.iconView::setImageResource)
            binding.iconView.contentDescription =
                itemView.resources.getString(R.string.glia_chat_audio_icon_content_description)
            binding.titleView.text =
                itemView.resources.getString(R.string.glia_chat_upgraded_to_audio_call)
            setUpgradeTheme(chatTheme?.audioUpgrade)
        } else {
            upgradeVideoIcon?.also(binding.iconView::setImageResource)
            binding.iconView.contentDescription =
                itemView.resources.getString(R.string.glia_chat_video_icon_content_description)
            binding.titleView.text =
                itemView.resources.getString(R.string.glia_chat_upgraded_to_video_call)
            setUpgradeTheme(chatTheme?.videoUpgrade)
        }
        binding.timerView.text = chatItem.time
    }
}