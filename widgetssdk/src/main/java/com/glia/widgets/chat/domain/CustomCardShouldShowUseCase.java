package com.glia.widgets.chat.domain;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.glia.androidsdk.chat.ChatMessage;
import com.glia.androidsdk.chat.SingleChoiceAttachment;
import com.glia.widgets.chat.adapter.CustomCardAdapter;

/**
 * @hide
 */
public class CustomCardShouldShowUseCase {
    @Nullable
    private final CustomCardAdapter adapter;

    public CustomCardShouldShowUseCase(@Nullable CustomCardAdapter adapter) {
        this.adapter = adapter;
    }

    public boolean execute(@NonNull ChatMessage message, int viewType, boolean shouldApplySelectedOption) {
        if (shouldApplySelectedOption && !hasSelectedOption(message)) {
            return true;
        }
        if (adapter == null || message.getMetadata() == null) {
            return false;
        }
        return adapter.shouldShowCard(message, viewType);
    }

    private boolean hasSelectedOption(@NonNull ChatMessage message) {
        if (message.getAttachment() == null
                || !(message.getAttachment() instanceof SingleChoiceAttachment)) {
            return false;
        }
        SingleChoiceAttachment singleChoiceAttachment = (SingleChoiceAttachment) message.getAttachment();
        String selectedOption = singleChoiceAttachment.getSelectedOption();
        return selectedOption != null && !selectedOption.isEmpty();
    }
}
