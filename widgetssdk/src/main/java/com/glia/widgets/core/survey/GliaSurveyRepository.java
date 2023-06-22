package com.glia.widgets.core.survey;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.glia.androidsdk.Engagement;
import com.glia.androidsdk.GliaException;
import com.glia.androidsdk.RequestCallback;
import com.glia.androidsdk.engagement.Survey;
import com.glia.widgets.di.GliaCore;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GliaSurveyRepository implements RequestCallback<Survey> {

    private final GliaCore gliaCore;
    private final List<OnSurveyListener> listeners = new ArrayList<>();

    @Nullable
    private String engagementId = null;

    @Nullable
    private ResultStorage result;

    public GliaSurveyRepository(GliaCore gliaCore) {
        this.gliaCore = gliaCore;
    }

    public synchronized void onEngagementEnded(Engagement engagement) {
        String engagementId = engagement.getState().getId();
        if (engagementId.equals(this.engagementId)) { // Prevent multiple surveys fetching.
            return;
        }
        this.engagementId = engagementId;
        engagement.getSurvey(this);
    }

    @Override
    public void onResult(@Nullable Survey survey, GliaException e) {
        List<OnSurveyListener> listenersCopy = new ArrayList<>(listeners);
        if (listenersCopy.isEmpty()) {
            result = new ResultStorage(survey);
        }
        for (OnSurveyListener listener : listenersCopy) {
            listener.onSurveyLoaded(survey);
        }
    }

    public void registerListener(OnSurveyListener listener) {
        listeners.add(listener);
        if (result != null) {
            listener.onSurveyLoaded(result.getSurvey());
            result = null;
        }
    }

    public boolean hasResult() {
        return result != null;
    }

    public void reset() {
        result = null;
    }

    public void unregisterListener(OnSurveyListener listener) {
        listeners.remove(listener);
    }

    public void submitSurveyAnswers(@NonNull List<Survey.Answer> answers,
                                    @NonNull String surveyId,
                                    @NonNull String engagementId,
                                    @NonNull Consumer<GliaException> callback) {
        gliaCore.submitSurveyAnswers(answers, surveyId, engagementId, callback);
    }

    private static class ResultStorage {
        @Nullable
        private final Survey survey;

        private ResultStorage(@Nullable Survey survey) {
            this.survey = survey;
        }

        @Nullable
        public Survey getSurvey() {
            return survey;
        }
    }
}
