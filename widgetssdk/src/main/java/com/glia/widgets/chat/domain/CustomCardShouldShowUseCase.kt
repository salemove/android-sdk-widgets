package com.glia.widgets.chat.domain

import com.glia.androidsdk.chat.ChatMessage
import com.glia.androidsdk.chat.SingleChoiceAttachment
import com.glia.widgets.chat.adapter.CustomCardAdapter

internal class CustomCardShouldShowUseCase(private val adapter: CustomCardAdapter?) {
    fun execute(message: ChatMessage, viewType: Int, shouldApplySelectedOption: Boolean): Boolean {
        if (shouldApplySelectedOption && !hasSelectedOption(message)) {
            return true
        }
        if (adapter == null || message.metadata == null) {
            return false
        }
        return adapter.shouldShowCard(message, viewType)
    }

    private fun hasSelectedOption(message: ChatMessage): Boolean {
        if (message.attachment == null || message.attachment !is SingleChoiceAttachment) return false
        val singleChoiceAttachment = message.attachment as SingleChoiceAttachment
        val selectedOption = singleChoiceAttachment.selectedOption
        return selectedOption != null && selectedOption.isNotEmpty()
    }
}
