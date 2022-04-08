package com.glia.widgets.survey;

import java.util.List;

public class SurveyState {
    public final List<QuestionItem> questions;

    public SurveyState() {
        this.questions = null;
    }

    public SurveyState(List<QuestionItem> questions) {
        this.questions = questions;
    }
}
