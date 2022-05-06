package com.glia.widgets.view.floatingvisitorvideoview;

import com.glia.androidsdk.comms.VisitorMediaState;
import com.glia.widgets.base.BaseController;
import com.glia.widgets.base.BaseView;

public interface FloatingVisitorVideoContract {
    interface Controller extends BaseController {
        void onResume();

        void onPause();

        void setView(View view);
    }

    interface View extends BaseView<Controller> {
        void show(VisitorMediaState state);

        void hide();

        void onResume();

        void onPause();

        void showOnHold();

        void hideOnHold();
    }
}
