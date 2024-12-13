package com.glia.widgets.entrywidget.adapter

import android.view.View
import androidx.core.view.isVisible
import com.glia.widgets.R
import com.glia.widgets.databinding.EntryWidgetLiveItemBinding
import com.glia.widgets.entrywidget.EntryWidgetContract
import com.glia.widgets.helper.setLocaleContentDescription
import com.glia.widgets.helper.setLocaleHint
import com.glia.widgets.helper.setLocaleText
import com.glia.widgets.view.unifiedui.applyImageColorTheme
import com.glia.widgets.view.unifiedui.applyLayerTheme
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.entrywidget.MediaTypeItemTheme

internal class EntryWidgetLiveItemViewHolder(
    private val binding: EntryWidgetLiveItemBinding,
    itemTheme: MediaTypeItemTheme?,
    isInsideSecureConversation: Boolean
) : EntryWidgetAdapter.ViewHolder(binding.root) {

    init {
        itemTheme?.let {
            binding.root.applyLayerTheme(it.background)
            binding.title.applyTextTheme(it.title)
            binding.description.applyTextTheme(it.message)
            binding.icon.applyImageColorTheme(it.iconColor)
            it.loadingTintColor?.primaryColorStateList?.let { tintList ->
                binding.iconLoading.backgroundTintList = tintList
                binding.titleLoading.backgroundTintList = tintList
                binding.descriptionLoading.backgroundTintList = tintList
            }
        }


        if (isInsideSecureConversation) {
            val mediumPadding = binding.root.resources.getDimension(R.dimen.glia_medium).toInt()
            binding.root.setPadding(0, mediumPadding, 0, mediumPadding)
        }
    }

    override fun bind(itemType: EntryWidgetContract.ItemType, onClickListener: View.OnClickListener) { //
        binding.root.setOnClickListener(onClickListener)
        binding.root.contentDescription = null
        when (itemType) {
            EntryWidgetContract.ItemType.VideoCall -> {
                binding.icon.setImageResource(R.drawable.ic_video)
                binding.title.setLocaleText(R.string.entry_widget_video_button_label)
                binding.description.setLocaleText(R.string.entry_widget_video_button_description)
                binding.description.setLocaleHint(R.string.entry_widget_video_button_accessibility_hint)
            }

            EntryWidgetContract.ItemType.VideoCallOngoing -> {
                binding.icon.setImageResource(R.drawable.ic_video)
                binding.title.setLocaleText(R.string.entry_widget_video_button_label)
                binding.description.setLocaleText(R.string.entry_widget_ongoing_engagement_description)
                binding.description.setLocaleHint(R.string.entry_widget_ongoing_engagement_button_accessibility_hint)
            }

            EntryWidgetContract.ItemType.AudioCall -> {
                binding.icon.setImageResource(R.drawable.ic_audio)
                binding.title.setLocaleText(R.string.entry_widget_audio_button_label)
                binding.description.setLocaleText(R.string.entry_widget_audio_button_description)
                binding.description.setLocaleHint(R.string.entry_widget_audio_button_accessibility_hint)
            }

            EntryWidgetContract.ItemType.AudioCallOngoing -> {
                binding.icon.setImageResource(R.drawable.ic_audio)
                binding.title.setLocaleText(R.string.entry_widget_audio_button_label)
                binding.description.setLocaleText(R.string.entry_widget_ongoing_engagement_description)
                binding.description.setLocaleHint(R.string.entry_widget_ongoing_engagement_button_accessibility_hint)
            }

            EntryWidgetContract.ItemType.Chat -> {
                binding.icon.setImageResource(R.drawable.ic_chat)
                binding.title.setLocaleText(R.string.entry_widget_live_chat_button_label)
                binding.description.setLocaleText(R.string.entry_widget_live_chat_button_description)
                binding.description.setLocaleHint(R.string.entry_widget_live_chat_button_accessibility_hint)
            }

            EntryWidgetContract.ItemType.ChatOngoing -> {
                binding.icon.setImageResource(R.drawable.ic_chat)
                binding.title.setLocaleText(R.string.entry_widget_live_chat_button_label)
                binding.description.setLocaleText(R.string.entry_widget_ongoing_engagement_description)
                binding.description.setLocaleHint(R.string.entry_widget_ongoing_engagement_button_accessibility_hint)
            }

            else -> {
                binding.icon.setImageResource(0)
                binding.title.text = null
                binding.description.text = null
                binding.description.hint = null
                binding.root.setLocaleContentDescription(R.string.entry_widget_loading_accessibility_label)
                binding.root.setOnClickListener(null)
            }
        }

        binding.loadingGroup.isVisible = itemType == EntryWidgetContract.ItemType.LoadingState
    }
}
