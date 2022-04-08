package com.glia.widgets.core.survey.domain;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.glia.androidsdk.engagement.Survey;
import com.glia.widgets.core.survey.GliaSurveyRepository;
import com.glia.widgets.survey.QuestionItem;
import com.glia.widgets.survey.SurveyValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class GliaSurveyAnswerUseCase {

    private final GliaSurveyRepository repository;

    public GliaSurveyAnswerUseCase(GliaSurveyRepository repository) {
        this.repository = repository;
    }

    public void submit(@Nullable List<QuestionItem> questions,
                       @NonNull Survey survey,
                       @NonNull Consumer<RuntimeException> callback) {
        try {
            validate(questions);
        } catch (SurveyValidationException exception) {
            callback.accept(exception);
            return;
        }
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
        repository.submitSurveyAnswers(answers, surveyId, engagementId, ignore -> {
            callback.accept(null); // ignore the Glia exception
        });
    }

    private void validate(@Nullable List<QuestionItem> questions) throws SurveyValidationException {
        if (questions == null) {
            return;
        }
        Integer firstErrorIndex = null;
        for (int i = 0; i < questions.size(); i++) {
            QuestionItem item = questions.get(i);
            try {
                validate(item);
                item.setShowError(false);
            } catch (SurveyValidationException ignore) {
                item.setShowError(true);
                if (firstErrorIndex == null) {
                    firstErrorIndex = i;
                }
            }
        }
        if (firstErrorIndex != null) {
            throw new SurveyValidationException(firstErrorIndex);
        }
    }

    public void validate(QuestionItem item) throws SurveyValidationException {
        if (item.getQuestion().isRequired()) {
            if (item.getQuestion().getType() == Survey.Question.QuestionType.TEXT) {
                if (item.getAnswer() == null || TextUtils.isEmpty(item.getAnswer().getResponse())) {
                    throw new SurveyValidationException();
                }
            } else {
                if (item.getAnswer() == null) {
                    throw new SurveyValidationException();
                }
            }
        }
    }
}
