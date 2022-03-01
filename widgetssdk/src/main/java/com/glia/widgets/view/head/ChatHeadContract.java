package com.glia.widgets.view.head;

import com.glia.widgets.UiTheme;
import com.glia.widgets.base.BaseController;
import com.glia.widgets.base.BaseView;
import com.glia.widgets.core.configuration.GliaSdkConfiguration;

public interface ChatHeadContract {
    interface Controller extends BaseController {
        void onChatHeadClicked();

        void onResume(android.view.View view);

        void onSetChatHeadView(View view);

        void onApplicationStop();
    }

    interface View extends BaseView<Controller> {
        void showOperatorImage(String operatorImgUrl);

        void showUnreadMessageCount(int count);

        void showPlaceholder();

        void showQueueing();

        void navigateToChat();

        void navigateToCall();

        void updateConfiguration(UiTheme buildTimeTheme, GliaSdkConfiguration sdkConfiguration);
    }
}
