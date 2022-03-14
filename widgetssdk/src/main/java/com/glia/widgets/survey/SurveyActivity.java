package com.glia.widgets.survey;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.glia.androidsdk.engagement.Survey;
import com.glia.widgets.GliaWidgets;
import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.di.Dependencies;

public class SurveyActivity extends AppCompatActivity {
    private static final String TAG = SurveyActivity.class.getSimpleName();

    private SurveyView surveyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.survey_activity);
        prepareSurveyView();
    }

    private void prepareSurveyView() {
        surveyView = findViewById(R.id.survey_view);

        SurveyContract.Controller surveyController =
                Dependencies.getControllerFactory().getSurveyController();
        surveyView.setController(surveyController);
        Bundle extras = getIntent().getExtras();
        UiTheme uiTheme = extras.getParcelable(GliaWidgets.UI_THEME);
        surveyView.setTheme(uiTheme);

        Survey survey = extras.getParcelable(GliaWidgets.SURVEY);
        surveyController.init(survey);
    }

    @Override
    protected void onDestroy() {
        if (surveyView != null) {
            surveyView.onDestroyView();
        }
        super.onDestroy();
    }
}
