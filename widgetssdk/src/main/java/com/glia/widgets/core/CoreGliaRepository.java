package com.glia.widgets.core;

import com.glia.androidsdk.Glia;
import com.glia.androidsdk.chat.ChatMessage;

public class CoreGliaRepository {

    public void loadHistory(HistoryCallback historyCallback) {
        if(historyCallback!=null) {
            Glia.getChatHistory(historyCallback::loaded);
        }
    }

    public interface HistoryCallback{
        void loaded(ChatMessage[] messages, Throwable error);
    }
}
