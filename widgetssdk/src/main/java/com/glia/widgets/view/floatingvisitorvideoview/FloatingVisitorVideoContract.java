package com.glia.widgets.view.floatingvisitorvideoview;

import android.app.Activity;

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
        void setActivity(Activity activity);

        void show(VisitorMediaState state);

        void hide();

        void onResume();

        void onPause();

        void onDestroy();

        void showOnHold();

        void hideOnHold();
    }
}
