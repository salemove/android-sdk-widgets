package com.glia.widgets.survey;

public class SurveyValidationException extends RuntimeException {
    private final int firstErrorPosition;

    public SurveyValidationException() {
        this.firstErrorPosition = 0;
    }

    public SurveyValidationException(int firstErrorPosition) {
        this.firstErrorPosition = firstErrorPosition;
    }

    public int getFirstErrorPosition() {
        return firstErrorPosition;
    }
}
