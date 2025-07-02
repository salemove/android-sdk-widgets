package com.glia.widgets.survey;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.glia.androidsdk.engagement.Survey;

import java.util.Objects;

/**
 * @hide
 */
public class QuestionItem {
    @NonNull
    private final Survey.Question question;
    @Nullable
    private Survey.Answer answer;
    private boolean showError;
    @Nullable
    private SurveyController.AnswerCallback answerCallback;

    public QuestionItem(@NonNull Survey.Question question, @Nullable Survey.Answer answer) {
        this.question = question;
        this.answer = answer;
        this.showError = false;
    }

    public void setAnswer(@Nullable Survey.Answer answer) {
        this.answer = answer;
    }

    public void setShowError(boolean showError) {
        this.showError = showError;
    }

    @NonNull
    public Survey.Question getQuestion() {
        return question;
    }

    @Nullable
    public Survey.Answer getAnswer() {
        return answer;
    }

    public boolean isShowError() {
        return showError;
    }

    @Nullable
    public SurveyController.AnswerCallback getAnswerCallback() {
        return answerCallback;
    }

    public void setAnswerCallback(@Nullable SurveyController.AnswerCallback answerCallback) {
        this.answerCallback = answerCallback;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuestionItem that = (QuestionItem) o;
        return showError == that.showError && question.equals(that.question)
            && Objects.equals(answer, that.answer);
    }
}
