package com.glia.widgets.core.survey;

import androidx.annotation.Nullable;

import com.glia.androidsdk.engagement.Survey;

public interface OnSurveyListener {
    void onSurveyLoaded(@Nullable Survey survey);
}
