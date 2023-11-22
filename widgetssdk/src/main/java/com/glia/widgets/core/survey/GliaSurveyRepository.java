package com.glia.widgets.core.survey;

import androidx.annotation.NonNull;

import com.glia.androidsdk.GliaException;
import com.glia.androidsdk.engagement.Survey;
import com.glia.widgets.di.GliaCore;
import com.glia.widgets.helper.Logger;

import java.util.List;
import java.util.function.Consumer;

public class GliaSurveyRepository {
    private static final String TAG = GliaSurveyRepository.class.getSimpleName();

    private final GliaCore gliaCore;

    public GliaSurveyRepository(GliaCore gliaCore) {
        this.gliaCore = gliaCore;
    }

    public void submitSurveyAnswers(@NonNull List<Survey.Answer> answers,
                                    @NonNull String surveyId,
                                    @NonNull String engagementId,
                                    @NonNull Consumer<GliaException> callback) {
        Logger.i(TAG, "Submit survey answers");
        gliaCore.submitSurveyAnswers(answers, surveyId, engagementId, callback);
    }
}
