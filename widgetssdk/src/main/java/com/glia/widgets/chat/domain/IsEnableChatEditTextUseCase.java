package com.glia.widgets.chat.domain;

import com.glia.widgets.chat.adapter.ChatAdapter;
import com.glia.widgets.chat.model.ChatInputMode;
import com.glia.widgets.chat.model.history.ChatItem;
import com.glia.widgets.chat.model.history.OperatorMessageItem;
import com.glia.widgets.helper.ListUtil;

import java.util.List;

public class IsEnableChatEditTextUseCase {
    // should enable only if there is no unselected choicecard last
    public boolean execute(List<ChatItem> items, ChatInputMode inputMode) {
        ChatItem lastItem = ListUtil.getLast(items);
        return !exists(lastItem) || !isOperatorMessageType(lastItem) || !isCardRequiresInteraction(lastItem);
    }

    private boolean exists(ChatItem item) {
        return item != null;
    }

    private boolean isOperatorMessageType(ChatItem item) {
        return item.getViewType() == ChatAdapter.OPERATOR_MESSAGE_VIEW_TYPE;
    }

    private boolean isCardRequiresInteraction(ChatItem item) {
        return ((OperatorMessageItem) item).isSingleChoiceCard();
    }
}

