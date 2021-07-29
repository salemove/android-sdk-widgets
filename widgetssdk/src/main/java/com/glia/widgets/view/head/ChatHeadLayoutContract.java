package com.glia.widgets.view.head;

import com.glia.widgets.base.BaseController;
import com.glia.widgets.base.BaseView;

public interface ChatHeadLayoutContract {
    interface Controller extends BaseController {
        void onChatHeadClicked();

        void setView(View view);
    }

    interface View extends BaseView<ChatHeadLayoutContract.Controller> {
        void showOperatorImage(String operatorImgUrl);

        void showUnreadMessageCount(int count);

        void showPlaceholder();

        void showQueueing();

        void navigateToChat();

        void navigateToCall();

        boolean isInChatView();

        void show();

        void hide();
    }
}
