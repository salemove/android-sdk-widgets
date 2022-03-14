package com.glia.widgets.core.survey.domain;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.glia.androidsdk.GliaException;
import com.glia.androidsdk.engagement.Survey;
import com.glia.widgets.core.survey.GliaSurveyRepository;
import com.glia.widgets.survey.QuestionItem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class GliaSurveyAnswerUseCase {

    private final GliaSurveyRepository repository;

    public GliaSurveyAnswerUseCase(GliaSurveyRepository repository) {
        this.repository = repository;
    }

    public void execute(@Nullable List<QuestionItem> questions,
                        @NonNull Survey survey,
                        @NonNull Consumer<GliaException> callback) {
        List<Survey.Answer> answers;
        if (questions != null) {
            answers = questions.stream()
                    .map(QuestionItem::getAnswer)
                    .collect(Collectors.toList());
        } else {
            answers = new ArrayList<>();
        }
        String surveyId = survey.getId();
        String engagementId = survey.getEngagementId();
        repository.submitSurveyAnswers(answers, surveyId, engagementId, callback);
    }
}
