package com.glia.widgets.survey;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.glia.androidsdk.engagement.Survey;
import com.glia.widgets.GliaWidgets;
import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.base.GliaActivity;
import com.glia.widgets.di.Dependencies;
import com.glia.widgets.helper.Utils;

public class SurveyActivity extends AppCompatActivity implements SurveyView.OnFinishListener,
        GliaActivity {
    private SurveyView surveyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.survey_activity);
        prepareSurveyView();
    }

    @Override
    protected void onDestroy() {
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

    private void hideSoftKeyboard() {
        Utils.hideSoftKeyboard(this, getWindow().getDecorView().getWindowToken());
    }

    private void prepareSurveyView() {
        surveyView = findViewById(R.id.survey_view);
        surveyView.setOnTitleUpdatedListener(this::setTitle);
        surveyView.setOnFinishListener(this);
        SurveyContract.Controller surveyController = Dependencies.getControllerFactory().getSurveyController();
        surveyView.setController(surveyController);
        Bundle extras = getIntent().getExtras();
        UiTheme uiTheme = extras.getParcelable(GliaWidgets.UI_THEME);
        surveyView.setTheme(uiTheme);

        Survey survey = extras.getParcelable(GliaWidgets.SURVEY);
        surveyController.init(survey);
    }
}
