package com.glia.widgets.survey;

import java.util.List;
import java.util.stream.Collectors;

public class SurveyState {
    public final List<QuestionItem> questions;

    public SurveyState() {
        this.questions = null;
    }

    public SurveyState(List<QuestionItem> questions) {
        this.questions = questions.stream().map(QuestionItem::copy).collect(Collectors.toList());
    }
}
