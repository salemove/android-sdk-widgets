package com.glia.widgets.survey;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.glia.androidsdk.engagement.Survey;
import com.glia.widgets.base.BaseController;
import com.glia.widgets.base.BaseView;

/**
 * @hide
 */
public interface SurveyContract {

    /**
     * @hide
     */
    interface Controller extends BaseController {
        void init(@Nullable Survey survey);

        void setView(View view);

        void onAnswer(@NonNull Survey.Answer answer);

        void onCancelClicked();

        void onSubmitClicked();
    }

    /**
     * @hide
     */
    interface View extends BaseView<Controller> {
        void onStateUpdated(SurveyState state);

        void scrollTo(int index);

        void hideSoftKeyboard();

        void onNetworkTimeout();

        void finish();
    }
}
