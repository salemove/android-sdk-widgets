package com.glia.widgets.survey;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.glia.androidsdk.engagement.Survey;
import com.glia.widgets.base.BaseController;
import com.glia.widgets.base.BaseView;
import com.glia.widgets.core.dialog.DialogController;

public interface SurveyContract {
    interface Controller extends BaseController {
        void init(@Nullable Survey survey);
        void setView(View view);
        void setDialogCallback(DialogController.Callback callback);
        void removeDialogCallback(DialogController.Callback callback);
        void onAnswer(@NonNull Survey.Answer answer);
        void onCancelClicked();
        void onSubmitClicked();
        void submitSurveyAnswersErrorDialogDismissed();
    }

    interface View extends BaseView<Controller> {
        void onStateUpdated(SurveyState state);
        void scrollTo(int index);
        void finish();
    }
}
