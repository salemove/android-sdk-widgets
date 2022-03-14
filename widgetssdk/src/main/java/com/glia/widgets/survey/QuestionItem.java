package com.glia.widgets.survey;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.glia.androidsdk.engagement.Survey;

public class QuestionItem {
    @NonNull
    private final Survey.Question question;
    @Nullable
    private Survey.Answer answer;

    public QuestionItem(@NonNull Survey.Question question, @Nullable Survey.Answer answer) {
        this.question = question;
        this.answer = answer;
    }

    public void setAnswer(@Nullable Survey.Answer answer) {
        this.answer = answer;
    }

    @NonNull
    public Survey.Question getQuestion() {
        return question;
    }

    @Nullable
    public Survey.Answer getAnswer() {
        return answer;
    }
}
