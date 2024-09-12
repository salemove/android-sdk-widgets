package com.glia.widgets.entrywidget.adapter

import android.view.View
import androidx.core.view.isVisible
import com.glia.widgets.R
import com.glia.widgets.databinding.EntryWidgetMediaTypeItemBinding
import com.glia.widgets.entrywidget.EntryWidgetContract
import com.glia.widgets.helper.setLocaleContentDescription
import com.glia.widgets.helper.setLocaleText

internal class EntryWidgetMediaTypeItemViewHolder(
    private val binding: EntryWidgetMediaTypeItemBinding
) : EntryWidgetAdapter.ViewHolder(binding.root) {

    override fun bind(itemType: EntryWidgetContract.ItemType, onClickListener: View.OnClickListener) {
        binding.root.setOnClickListener(onClickListener)
        when (itemType) {
            EntryWidgetContract.ItemType.VIDEO_CALL -> {
                binding.icon.setImageResource(R.drawable.ic_video)
                binding.title.setLocaleText(R.string.entry_widget_video_button_label)
                binding.description.setLocaleText(R.string.entry_widget_video_button_description)
                binding.root.setLocaleContentDescription(R.string.entry_widget_video_button_accessibility)
            }
            EntryWidgetContract.ItemType.AUDIO_CALL -> {
                binding.icon.setImageResource(R.drawable.ic_audio)
                binding.title.setLocaleText(R.string.entry_widget_audio_button_label)
                binding.description.setLocaleText(R.string.entry_widget_audio_button_description)
                binding.root.setLocaleContentDescription(R.string.entry_widget_audio_button_accessibility)
            }
            EntryWidgetContract.ItemType.CHAT -> {
                binding.icon.setImageResource(R.drawable.ic_chat)
                binding.title.setLocaleText(R.string.entry_widget_live_chat_button_label)
                binding.description.setLocaleText(R.string.entry_widget_live_chat_button_description)
                binding.root.setLocaleContentDescription(R.string.entry_widget_live_chat_button_accessibility)
            }
            EntryWidgetContract.ItemType.SECURE_MESSAGE -> {
                binding.icon.setImageResource(R.drawable.ic_secure_message)
                binding.title.setLocaleText(R.string.entry_widget_secure_messaging_button_label)
                binding.description.setLocaleText(R.string.entry_widget_secure_messaging_button_description)
                binding.root.setLocaleContentDescription(R.string.entry_widget_secure_messaging_button_accessibility)
            }
            else -> {
                binding.icon.setImageResource(0)
                binding.title.text = null
                binding.description.text = null
                binding.root.contentDescription = null
            }
        }

        binding.loadingGroup.isVisible = itemType == EntryWidgetContract.ItemType.LOADING_STATE
    }
}
