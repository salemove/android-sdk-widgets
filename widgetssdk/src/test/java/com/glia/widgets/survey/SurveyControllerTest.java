package com.glia.widgets.survey;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static java.util.Collections.singletonList;

import com.glia.androidsdk.engagement.Survey;
import com.glia.widgets.internal.survey.domain.GliaSurveyAnswerUseCase;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class SurveyControllerTest {

    private SurveyController surveyController;
    private GliaSurveyAnswerUseCase gliaSurveyAnswerUseCase;

    @Before
    public void setUp() {
        gliaSurveyAnswerUseCase = mock(GliaSurveyAnswerUseCase.class);
        surveyController = new SurveyController(gliaSurveyAnswerUseCase);
    }

    @Test
    public void isAlreadyInit_returnsTrue_whenSameSurveyAndStateInit() {
        Survey survey = mock(Survey.class);
        when(survey.getId()).thenReturn("surveyId");
        when(survey.getEngagementId()).thenReturn("engagementId");
        surveyController.survey = survey;

        surveyController.state = new SurveyState.Builder()
            .setQuestions(singletonList(mock(QuestionItem.class)))
            .createSurveyState();

        assertTrue(surveyController.isAlreadyInit(survey));
    }

    @Test
    public void isAlreadyInit_returnsFalse_whenSameSurveyAndStateNotInit() {
        Survey survey = mock(Survey.class);
        when(survey.getId()).thenReturn("surveyId");
        when(survey.getEngagementId()).thenReturn("engagementId");
        surveyController.survey = survey;

        assertFalse(surveyController.isAlreadyInit(survey));
    }

    @Test
    public void isAlreadyInit_returnsFalse_whenSameSurveyAndStateQuestionsIsEmpty() {
        Survey survey = mock(Survey.class);
        when(survey.getId()).thenReturn("surveyId");
        when(survey.getEngagementId()).thenReturn("engagementId");
        surveyController.survey = survey;

        surveyController.state = new SurveyState.Builder()
            .setQuestions(new ArrayList<>())
            .createSurveyState();

        assertFalse(surveyController.isAlreadyInit(survey));
    }

    @Test
    public void isAlreadyInit_returnsFalse_whenSameSurveyButDifferentEngagement() {
        Survey survey = mock(Survey.class);
        when(survey.getId()).thenReturn("surveyId");
        when(survey.getEngagementId()).thenReturn("engagementId");
        surveyController.survey = survey;

        surveyController.state = new SurveyState.Builder()
            .setQuestions(singletonList(mock(QuestionItem.class)))
            .createSurveyState();

        Survey anotherSurvey = mock(Survey.class);
        when(anotherSurvey.getId()).thenReturn("surveyId");
        when(anotherSurvey.getEngagementId()).thenReturn("anotherEngagementId");
        assertFalse(surveyController.isAlreadyInit(anotherSurvey));
    }

    @Test
    public void isAlreadyInit_returnsFalse_whenDifferentSurvey() {
        Survey survey = mock(Survey.class);
        when(survey.getId()).thenReturn("surveyId");
        when(survey.getEngagementId()).thenReturn("engagementId");
        surveyController.survey = survey;

        surveyController.state = new SurveyState.Builder()
            .setQuestions(singletonList(mock(QuestionItem.class)))
            .createSurveyState();

        Survey anotherSurvey = mock(Survey.class);
        when(anotherSurvey.getId()).thenReturn("anotherSurveyId");
        when(anotherSurvey.getEngagementId()).thenReturn("engagementId");
        assertFalse(surveyController.isAlreadyInit(anotherSurvey));
    }

    @Test
    public void isAlreadyInit_returnsFalse_whenInitWithNull() {
        Survey survey = mock(Survey.class);
        when(survey.getId()).thenReturn("surveyId");
        when(survey.getEngagementId()).thenReturn("engagementId");
        surveyController.survey = survey;

        surveyController.state = new SurveyState.Builder()
            .setQuestions(singletonList(mock(QuestionItem.class)))
            .createSurveyState();

        assertFalse(surveyController.isAlreadyInit(null));
    }

    @Test
    public void isAlreadyInit_returnsFalse_whenControllerSurveyIsNull() {
        surveyController.survey = null;

        surveyController.state = new SurveyState.Builder()
            .setQuestions(singletonList(mock(QuestionItem.class)))
            .createSurveyState();

        Survey survey = mock(Survey.class);
        when(survey.getId()).thenReturn("surveyId");
        when(survey.getEngagementId()).thenReturn("engagementId");
        assertFalse(surveyController.isAlreadyInit(survey));
    }

    @Test
    public void isAlreadyInit_returnsFalse_whenInitWithNullAndControllerSurveyIsNull() {
        surveyController.survey = null;

        surveyController.state = new SurveyState.Builder()
            .setQuestions(singletonList(mock(QuestionItem.class)))
            .createSurveyState();

        assertFalse(surveyController.isAlreadyInit(null));
    }
}
