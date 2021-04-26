package com.glia.widgets.model;

import com.glia.androidsdk.Glia;
import com.glia.androidsdk.chat.ChatMessage;
import com.glia.widgets.helper.BaseObservable;

public class GliaMessageRepository extends BaseObservable<GliaMessageRepository.Listener> {

    public interface Listener {
        void loaded(ChatMessage[] messages, Throwable error);
    }

    public void loadHistory() {
        for (Listener listener : getListeners()) {
            Glia.getChatHistory(listener::loaded);
        }
    }
}
