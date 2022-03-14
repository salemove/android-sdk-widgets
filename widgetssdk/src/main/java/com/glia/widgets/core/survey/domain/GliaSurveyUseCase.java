package com.glia.widgets.core.survey.domain;

import com.glia.widgets.core.survey.GliaSurveyRepository;
import com.glia.widgets.core.survey.OnSurveyListener;

public class GliaSurveyUseCase {

    private final GliaSurveyRepository surveyRepository;

    public GliaSurveyUseCase(GliaSurveyRepository surveyRepository) {
        this.surveyRepository = surveyRepository;
    }

    public void registerListener(OnSurveyListener listener) {
        surveyRepository.registerListener(listener);
    }

    public void unregisterListener(OnSurveyListener listener) {
        surveyRepository.unregisterListener(listener);
    }

}
