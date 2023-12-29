package com.glia.widgets.view.head;

import androidx.core.util.Pair;

import com.glia.widgets.UiTheme;
import com.glia.widgets.base.BaseController;
import com.glia.widgets.base.BaseView;
import com.glia.widgets.core.configuration.GliaSdkConfiguration;

import org.jetbrains.annotations.Nullable;

public interface ChatHeadContract {
    interface Controller extends BaseController {
        void onChatHeadClicked();

        void onResume(android.view.View view);

        void onSetChatHeadView(View view);

        void onApplicationStop();

        void onChatHeadPositionChanged(int x, int y);

        Pair<Integer, Integer> getChatHeadPosition();

        void setBuildTimeTheme(@Nullable UiTheme uiTheme);

        void setSdkConfiguration(@Nullable GliaSdkConfiguration configuration);

        void onPause(@Nullable android.view.View gliaOrRootView);

        void updateChatHeadView();
    }

    interface View extends BaseView<Controller> {
        void showOperatorImage(String operatorImgUrl);

        void showUnreadMessageCount(int count);

        void showPlaceholder();

        void showQueueing();

        void showScreenSharing();

        void showOnHold();

        void hideOnHold();

        void navigateToChat();

        void navigateToCall();

        void navigateToEndScreenSharing();

        void updateConfiguration(UiTheme buildTimeTheme, GliaSdkConfiguration sdkConfiguration);
    }
}
