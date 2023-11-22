package com.glia.widgets.survey;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.glia.androidsdk.engagement.Survey;
import com.glia.widgets.GliaWidgets;
import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.di.Dependencies;
import com.glia.widgets.helper.InsetsKt;
import com.glia.widgets.helper.Logger;

public class SurveyActivity extends AppCompatActivity implements SurveyView.OnFinishListener {
    private static final String TAG = SurveyActivity.class.getSimpleName();

    private SurveyView surveyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.i(TAG, "Create Survey screen");
        setContentView(R.layout.survey_activity);
        prepareSurveyView();
    }

    @Override
    protected void onDestroy() {
        Logger.i(TAG, "Destroy Survey screen");
        hideSoftKeyboard();
        if (surveyView != null) {
            surveyView.onDestroyView();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        // Back press behaves the same way as Callback.onFinish(). See the comment below.
        finishAndRemoveTask();
    }

    @Override
    public void onFinish() {

        // In case the engagement ends, Activity is removed from the device's Recents menu
        // to avoid app users to accidentally start queueing for another call when they resume
        // the app from the Recents menu and the app's backstack was empty.
        finishAndRemoveTask();
    }

    @Override
    public void finishAndRemoveTask() {
        super.finishAndRemoveTask();

        overridePendingTransition(0, 0);
    }

    private void hideSoftKeyboard() {
        WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(getWindow(), findViewById(R.id.survey_view));
        InsetsKt.hideKeyboard(insetsController);
    }

    private void prepareSurveyView() {
        surveyView = findViewById(R.id.survey_view);
        surveyView.setOnTitleUpdatedListener(this::setTitle);
        surveyView.setOnFinishListener(this);
        SurveyContract.Controller surveyController = Dependencies.getControllerFactory().getSurveyController();
        surveyView.setController(surveyController);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            UiTheme uiTheme = extras.getParcelable(GliaWidgets.UI_THEME);
            surveyView.setTheme(uiTheme);
            Survey survey = extras.getParcelable(GliaWidgets.SURVEY);
            surveyController.init(survey);
        }
    }
}
