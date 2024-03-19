package com.glia.widgets.survey;

import androidx.annotation.Nullable;

import java.util.List;

/**
 * @hide
 */
public class SurveyState {
    @Nullable
    public final String title;

    @Nullable
    public final List<QuestionItem> questions;

    private SurveyState(String title, List<QuestionItem> questions) {
        this.title = title;
        this.questions = questions;
    }

    static class Builder {
        @Nullable
        public String title;

        @Nullable
        public List<QuestionItem> questions;

        public Builder setTitle(@Nullable String title) {
            this.title = title;
            return this;
        }

        public Builder setQuestions(@Nullable List<QuestionItem> questions) {
            this.questions = questions;
            return this;
        }

        public Builder copyFrom(@Nullable SurveyState state) {
            if (state == null) {
                return this;
            }
            this.title = state.title;
            this.questions = state.questions;
            return this;
        }

        public SurveyState createSurveyState() {
            return new SurveyState(title, questions);
        }
    }
}
