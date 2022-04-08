package com.glia.widgets.survey;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.glia.androidsdk.engagement.Survey;

import java.util.Objects;

public class QuestionItem {
    @NonNull
    private final Survey.Question question;
    @Nullable
    private Survey.Answer answer;
    private boolean showError;

    public QuestionItem(@NonNull Survey.Question question, @Nullable Survey.Answer answer) {
        this.question = question;
        this.answer = answer;
        this.showError = false;
    }

    private QuestionItem(@NonNull Survey.Question question,
                         @Nullable Survey.Answer answer, boolean showError) {
        this.question = question;
        this.answer = answer;
        this.showError = showError;
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

    public QuestionItem copy() {
        return new QuestionItem(question, answer, showError);
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
