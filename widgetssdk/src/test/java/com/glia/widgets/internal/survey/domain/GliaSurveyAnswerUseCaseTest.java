package com.glia.widgets.internal.survey.domain;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.glia.androidsdk.engagement.Survey;
import com.glia.widgets.internal.survey.GliaSurveyRepository;
import com.glia.widgets.survey.QuestionItem;
import com.glia.widgets.survey.SurveyValidationException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class GliaSurveyAnswerUseCaseTest {

    private GliaSurveyAnswerUseCase gliaSurveyAnswerUseCase;

    @Before
    public void setUp() {
        GliaSurveyRepository repository = mock(GliaSurveyRepository.class);
        gliaSurveyAnswerUseCase = new GliaSurveyAnswerUseCase(repository);
    }

    // Tests for a single question validation

    @Test
    public void validate_succeeds_whenQuestionOptionalTypeTextAnswerNull() {
        Survey.Question question = mock(Survey.Question.class);
        when(question.isRequired()).thenReturn(false);
        when(question.getType()).thenReturn(Survey.Question.QuestionType.TEXT);

        Survey.Answer answer = null;
        QuestionItem item = new QuestionItem(question, answer);
        try {
            gliaSurveyAnswerUseCase.validate(item);
        } catch (Exception e) {
            Assert.fail("Should not have thrown any exception");
        }
        // Do nothing, basically succeeds test if validate() passed.
    }

    @Test
    public void validate_succeeds_whenQuestionOptionalTypeTextAnswerEmpty() {
        Survey.Question question = mock(Survey.Question.class);
        when(question.isRequired()).thenReturn(false);
        when(question.getType()).thenReturn(Survey.Question.QuestionType.TEXT);

        String response = "";
        Survey.Answer answer = Survey.Answer.makeAnswer("question_id", response);
        QuestionItem item = new QuestionItem(question, answer);
        try {
            gliaSurveyAnswerUseCase.validate(item);
        } catch (Exception e) {
            Assert.fail("Should not have thrown any exception");
        }
        // Do nothing, basically succeeds test if validate() passed.
    }

    @Test
    public void validate_succeeds_whenQuestionOptionalTypeSingleChoiceAnswerNull() {
        Survey.Question question = mock(Survey.Question.class);
        when(question.isRequired()).thenReturn(false);
        when(question.getType()).thenReturn(Survey.Question.QuestionType.SINGLE_CHOICE);

        Survey.Answer answer = null;
        QuestionItem item = new QuestionItem(question, answer);
        try {
            gliaSurveyAnswerUseCase.validate(item);
        } catch (Exception e) {
            Assert.fail("Should not have thrown any exception");
        }
        // Do nothing, basically succeeds test if validate() passed.
    }

    @Test
    public void validate_succeeds_whenQuestionOptionalTypeSingleChoiceAnswerEmpty() {
        Survey.Question question = mock(Survey.Question.class);
        when(question.isRequired()).thenReturn(false);
        when(question.getType()).thenReturn(Survey.Question.QuestionType.SINGLE_CHOICE);

        String response = "";
        Survey.Answer answer = Survey.Answer.makeAnswer("question_id", response);
        QuestionItem item = new QuestionItem(question, answer);
        try {
            gliaSurveyAnswerUseCase.validate(item);
        } catch (Exception e) {
            Assert.fail("Should not have thrown any exception");
        }
        // Do nothing, basically succeeds test if validate() passed.
    }

    @Test
    public void validate_succeeds_whenQuestionOptionalTypeBooleanAnswerNull() {
        Survey.Question question = mock(Survey.Question.class);
        when(question.isRequired()).thenReturn(false);
        when(question.getType()).thenReturn(Survey.Question.QuestionType.BOOLEAN);

        Survey.Answer answer = null;
        QuestionItem item = new QuestionItem(question, answer);
        try {
            gliaSurveyAnswerUseCase.validate(item);
        } catch (Exception e) {
            Assert.fail("Should not have thrown any exception");
        }
        // Do nothing, basically succeeds test if validate() passed.
    }

    @Test
    public void validate_succeeds_whenQuestionOptionalTypeBooleanAnswerEmpty() {
        Survey.Question question = mock(Survey.Question.class);
        when(question.isRequired()).thenReturn(false);
        when(question.getType()).thenReturn(Survey.Question.QuestionType.BOOLEAN);

        String response = "";
        Survey.Answer answer = Survey.Answer.makeAnswer("question_id", response);
        QuestionItem item = new QuestionItem(question, answer);
        try {
            gliaSurveyAnswerUseCase.validate(item);
        } catch (Exception e) {
            Assert.fail("Should not have thrown any exception");
        }
        // Do nothing, basically succeeds test if validate() passed.
    }

    @Test
    public void validate_succeeds_whenQuestionOptionalTypeScaleAnswerNull() {
        Survey.Question question = mock(Survey.Question.class);
        when(question.isRequired()).thenReturn(false);
        when(question.getType()).thenReturn(Survey.Question.QuestionType.SCALE);

        Survey.Answer answer = null;
        QuestionItem item = new QuestionItem(question, answer);
        try {
            gliaSurveyAnswerUseCase.validate(item);
        } catch (Exception e) {
            Assert.fail("Should not have thrown any exception");
        }
        // Do nothing, basically succeeds test if validate() passed.
    }

    @Test
    public void validate_succeeds_whenQuestionOptionalTypeScaleAnswerEmpty() {
        Survey.Question question = mock(Survey.Question.class);
        when(question.isRequired()).thenReturn(false);
        when(question.getType()).thenReturn(Survey.Question.QuestionType.SCALE);

        String response = "";
        Survey.Answer answer = Survey.Answer.makeAnswer("question_id", response);
        QuestionItem item = new QuestionItem(question, answer);
        try {
            gliaSurveyAnswerUseCase.validate(item);
        } catch (Exception e) {
            Assert.fail("Should not have thrown any exception");
        }
        // Do nothing, basically succeeds test if validate() passed.
    }

    @Test
    public void validate_Succeeds_whenQuestionRequiredTypeSingleChoiceTextAnswerEmpty() {
        Survey.Question question = mock(Survey.Question.class);
        when(question.isRequired()).thenReturn(true);
        when(question.getType()).thenReturn(Survey.Question.QuestionType.SINGLE_CHOICE);

        String response = "";
        Survey.Answer answer = Survey.Answer.makeAnswer("question_id", response);
        QuestionItem item = new QuestionItem(question, answer);
        try {
            gliaSurveyAnswerUseCase.validate(item);
        } catch (Exception e) {
            Assert.fail("Should not have thrown any exception");
        }
        // Do nothing, basically succeeds test if validate() passed.
    }

    @Test
    public void validate_throwsException_whenQuestionRequiredTypeSingleChoiceAnswerNull() {
        Survey.Question question = mock(Survey.Question.class);
        when(question.isRequired()).thenReturn(true);
        when(question.getType()).thenReturn(Survey.Question.QuestionType.SINGLE_CHOICE);

        Survey.Answer answer = null;
        QuestionItem item = new QuestionItem(question, answer);
        try {
            gliaSurveyAnswerUseCase.validate(item);
            Assert.fail("Should have thrown SurveyValidationException exception");
        } catch (SurveyValidationException e) {
            if (!e.getClass().equals(SurveyValidationException.class)) {
                Assert.fail();
            }
            // Do nothing, basically succeeds test if same exception was thrown.
        }
    }

    @Test
    public void validate_Succeeds_whenQuestionRequiredTypeBooleanTextAnswerEmpty() {
        Survey.Question question = mock(Survey.Question.class);
        when(question.isRequired()).thenReturn(true);
        when(question.getType()).thenReturn(Survey.Question.QuestionType.BOOLEAN);

        String response = "";
        Survey.Answer answer = Survey.Answer.makeAnswer("question_id", response);
        QuestionItem item = new QuestionItem(question, answer);
        try {
            gliaSurveyAnswerUseCase.validate(item);
        } catch (Exception e) {
            Assert.fail("Should not have thrown any exception");
        }
        // Do nothing, basically succeeds test if validate() passed.
    }

    @Test
    public void validate_throwsException_whenQuestionRequiredTypeBooleanAnswerNull() {
        Survey.Question question = mock(Survey.Question.class);
        when(question.isRequired()).thenReturn(true);
        when(question.getType()).thenReturn(Survey.Question.QuestionType.BOOLEAN);

        Survey.Answer answer = null;
        QuestionItem item = new QuestionItem(question, answer);
        try {
            gliaSurveyAnswerUseCase.validate(item);
            Assert.fail("Should have thrown SurveyValidationException exception");
        } catch (SurveyValidationException e) {
            if (!e.getClass().equals(SurveyValidationException.class)) {
                Assert.fail();
            }
            // Do nothing, basically succeeds test if same exception was thrown.
        }
    }

    @Test
    public void validate_Succeeds_whenQuestionRequiredTypeScaleTextAnswerEmpty() {
        Survey.Question question = mock(Survey.Question.class);
        when(question.isRequired()).thenReturn(true);
        when(question.getType()).thenReturn(Survey.Question.QuestionType.SCALE);

        String response = "";
        Survey.Answer answer = Survey.Answer.makeAnswer("question_id", response);
        QuestionItem item = new QuestionItem(question, answer);
        try {
            gliaSurveyAnswerUseCase.validate(item);
        } catch (Exception e) {
            Assert.fail("Should not have thrown any exception");
        }
        // Do nothing, basically succeeds test if validate() passed.
    }

    @Test
    public void validate_throwsException_whenQuestionRequiredTypeScaleAnswerNull() {
        Survey.Question question = mock(Survey.Question.class);
        when(question.isRequired()).thenReturn(true);
        when(question.getType()).thenReturn(Survey.Question.QuestionType.SCALE);

        Survey.Answer answer = null;
        QuestionItem item = new QuestionItem(question, answer);
        try {
            gliaSurveyAnswerUseCase.validate(item);
            Assert.fail("Should have thrown SurveyValidationException exception");
        } catch (SurveyValidationException e) {
            if (!e.getClass().equals(SurveyValidationException.class)) {
                Assert.fail();
            }
            // Do nothing, basically succeeds test if same exception was thrown.
        }
    }

    @Test
    public void validate_throwsException_whenQuestionRequiredTypeTextAnswerNull() {
        Survey.Question question = mock(Survey.Question.class);
        when(question.isRequired()).thenReturn(true);
        when(question.getType()).thenReturn(Survey.Question.QuestionType.TEXT);

        Survey.Answer answer = null;
        QuestionItem item = new QuestionItem(question, answer);
        try {
            gliaSurveyAnswerUseCase.validate(item);
            Assert.fail("Should have thrown SurveyValidationException exception");
        } catch (SurveyValidationException e) {
            if (!e.getClass().equals(SurveyValidationException.class)) {
                Assert.fail();
            }
            // Do nothing, basically succeeds test if same exception was thrown.
        }
    }

    @Test
    public void validate_throwsException_whenQuestionRequiredTypeTextAnswerEmpty() {
        Survey.Question question = mock(Survey.Question.class);
        when(question.isRequired()).thenReturn(true);
        when(question.getType()).thenReturn(Survey.Question.QuestionType.TEXT);

        String response = "";
        Survey.Answer answer = Survey.Answer.makeAnswer("question_id", response);
        QuestionItem item = new QuestionItem(question, answer);
        try {
            gliaSurveyAnswerUseCase.validate(item);
            Assert.fail("Should have thrown SurveyValidationException exception");
        } catch (SurveyValidationException e) {
            if (!e.getClass().equals(SurveyValidationException.class)) {
                Assert.fail();
            }
            // Do nothing, basically succeeds test if same exception was thrown.
        }
    }

    @Test
    public void validate_succeeds_whenQuestionRequiredTypeTextAnswerNotNullNotEmpty() {
        Survey.Question question = mock(Survey.Question.class);
        when(question.isRequired()).thenReturn(true);
        when(question.getType()).thenReturn(Survey.Question.QuestionType.TEXT);

        String response = "Some text";
        Survey.Answer answer = Survey.Answer.makeAnswer("question_id", response);
        QuestionItem item = new QuestionItem(question, answer);
        try {
            gliaSurveyAnswerUseCase.validate(item);
        } catch (Exception e) {
            Assert.fail("Should not have thrown any exception");
        }
        // Do nothing, basically succeeds test if validate() passed.
    }

    // Tests for a list of questions validation

    @Test
    public void validate_succeeds_whenQuestionsNull() {
        List<QuestionItem> questions = null;
        try {
            gliaSurveyAnswerUseCase.validate(questions);
        } catch (Exception e) {
            Assert.fail("Should not have thrown any exception");
        }
        // Do nothing, basically succeeds test if validate() passed.
    }

    @Test
    public void validate_succeeds_whenQuestionsListEmpty() {
        List<QuestionItem> questions = new ArrayList<>();
        try {
            gliaSurveyAnswerUseCase.validate(questions);
        } catch (Exception e) {
            Assert.fail("Should not have thrown any exception");
        }
        // Do nothing, basically succeeds test if validate() passed.
    }

    @Test
    public void validate_throwsException_whenQuestionsContainsValidAndInvalidAnswers() {
        Survey.Question question = mock(Survey.Question.class);
        when(question.isRequired()).thenReturn(true);
        when(question.getType()).thenReturn(Survey.Question.QuestionType.TEXT);

        List<QuestionItem> questions = new ArrayList<>();

        String invalidResponse = "";
        Survey.Answer invalidAnswer = Survey.Answer.makeAnswer("question_id", invalidResponse);
        QuestionItem invalidItem = new QuestionItem(question, invalidAnswer);
        questions.add(invalidItem);

        String validResponse = "Some text";
        Survey.Answer validAnswer = Survey.Answer.makeAnswer("question_id", validResponse);
        QuestionItem validItem = new QuestionItem(question, validAnswer);
        questions.add(validItem);

        try {
            gliaSurveyAnswerUseCase.validate(questions);
        } catch (SurveyValidationException e) {
            if (!e.getClass().equals(SurveyValidationException.class)) {
                Assert.fail();
            }
            // Do nothing, basically succeeds test if same exception was thrown.
        }
    }

    @Test
    public void validate_throwsException_whenQuestionsContainsOnlyInvalidAnswers() {
        Survey.Question question = mock(Survey.Question.class);
        when(question.isRequired()).thenReturn(true);
        when(question.getType()).thenReturn(Survey.Question.QuestionType.TEXT);

        List<QuestionItem> questions = new ArrayList<>();

        String invalidResponse = "";
        Survey.Answer invalidAnswer = Survey.Answer.makeAnswer("question_id", invalidResponse);
        QuestionItem invalidItem = new QuestionItem(question, invalidAnswer);
        questions.add(invalidItem);

        QuestionItem validItem = new QuestionItem(question, null);
        questions.add(validItem);

        try {
            gliaSurveyAnswerUseCase.validate(questions);
        } catch (SurveyValidationException e) {
            if (!e.getClass().equals(SurveyValidationException.class)) {
                Assert.fail();
            }
            // Do nothing, basically succeeds test if same exception was thrown.
        }
    }

    @Test
    public void validate_succeeds_whenQuestionsContainsOnlyValidAnswers() {
        Survey.Question textQuestion = mock(Survey.Question.class);
        when(textQuestion.isRequired()).thenReturn(true);
        when(textQuestion.getType()).thenReturn(Survey.Question.QuestionType.TEXT);

        List<QuestionItem> questions = new ArrayList<>();

        String validTextResponse = "Some text";
        Survey.Answer validTextAnswer = Survey.Answer.makeAnswer("question_id", validTextResponse);
        QuestionItem validTextItem = new QuestionItem(textQuestion, validTextAnswer);
        questions.add(validTextItem);

        Survey.Question booleanQuestion = mock(Survey.Question.class);
        when(booleanQuestion.isRequired()).thenReturn(true);
        when(booleanQuestion.getType()).thenReturn(Survey.Question.QuestionType.BOOLEAN);
        boolean validBooleanResponse = true;
        Survey.Answer validBooleanAnswer = Survey.Answer.makeAnswer("question_id", validBooleanResponse);
        QuestionItem validBooleanItem = new QuestionItem(booleanQuestion, validBooleanAnswer);
        questions.add(validBooleanItem);

        try {
            gliaSurveyAnswerUseCase.validate(questions);
        } catch (SurveyValidationException e) {
            Assert.fail("Should not have thrown any exception");
        }
        // Do nothing, basically succeeds test if validate() passed.
    }

    // Tests for a trim() function

    @Test
    public void trim_leavesQuestionsNull_whenQuestionsNull() {
        Survey.Question textQuestion = mock(Survey.Question.class);
        when(textQuestion.isRequired()).thenReturn(true);
        when(textQuestion.getType()).thenReturn(Survey.Question.QuestionType.TEXT);

        List<QuestionItem> questions = null;

        gliaSurveyAnswerUseCase.trim(questions);

        assertNull(questions);
    }

    @Test
    public void trim_replacesAnswerWithSpacesOnlyWithNull_whenQuestionsContainsAnswerWithSpacesOnly() {
        Survey.Question textQuestion = mock(Survey.Question.class);
        when(textQuestion.isRequired()).thenReturn(true);
        when(textQuestion.getType()).thenReturn(Survey.Question.QuestionType.TEXT);

        List<QuestionItem> questions = new ArrayList<>();

        String response = "    ";
        Survey.Answer answer = Survey.Answer.makeAnswer("question_id", response);
        QuestionItem textItem = new QuestionItem(textQuestion, answer);
        questions.add(textItem);

        gliaSurveyAnswerUseCase.trim(questions);

        assertNull(questions.get(0).getAnswer());
    }

    @Test
    public void trim_replacesAnswerWithSpacesOnlyWithNull_whenQuestionsContainsAnswerNullAndAnswerWithSpacesOnly() {
        Survey.Question textQuestion = mock(Survey.Question.class);
        when(textQuestion.isRequired()).thenReturn(true);
        when(textQuestion.getType()).thenReturn(Survey.Question.QuestionType.TEXT);

        List<QuestionItem> questions = new ArrayList<>();

        Survey.Answer answerNull = null;
        QuestionItem textItemNull = new QuestionItem(textQuestion, answerNull);
        questions.add(textItemNull);

        String response = "    ";
        Survey.Answer answer = Survey.Answer.makeAnswer("question_id", response);
        QuestionItem textItem = new QuestionItem(textQuestion, answer);
        questions.add(textItem);

        gliaSurveyAnswerUseCase.trim(questions);

        assertNull(questions.get(0).getAnswer());
        assertNull(questions.get(1).getAnswer());
    }

    @Test
    public void trim_doesNotChangeAnswer_whenQuestionsContainsBooleanQuestion() {
        Survey.Question question = mock(Survey.Question.class);
        when(question.isRequired()).thenReturn(true);
        when(question.getType()).thenReturn(Survey.Question.QuestionType.BOOLEAN);

        List<QuestionItem> questions = new ArrayList<>();

        boolean response = true;
        Survey.Answer answer = Survey.Answer.makeAnswer("question_id", response);
        QuestionItem questionItem = new QuestionItem(question, answer);
        questions.add(questionItem);

        List<QuestionItem> expectedList = new ArrayList<>(questions);
        gliaSurveyAnswerUseCase.trim(questions);

        assertArrayEquals(questions.toArray(), expectedList.toArray());
    }

    @Test
    public void trim_doesNotChangeAnswer_whenQuestionsContainsScaleQuestion() {
        Survey.Question question = mock(Survey.Question.class);
        when(question.isRequired()).thenReturn(true);
        when(question.getType()).thenReturn(Survey.Question.QuestionType.SCALE);

        List<QuestionItem> questions = new ArrayList<>();

        boolean response = true;
        Survey.Answer answer = Survey.Answer.makeAnswer("question_id", response);
        QuestionItem questionItem = new QuestionItem(question, answer);
        questions.add(questionItem);

        List<QuestionItem> expectedList = new ArrayList<>(questions);
        gliaSurveyAnswerUseCase.trim(questions);

        assertArrayEquals(questions.toArray(), expectedList.toArray());
    }

    @Test
    public void trim_doesNotChangeAnswer_whenQuestionsContainsSingleChoiceQuestion() {
        Survey.Question question = mock(Survey.Question.class);
        when(question.isRequired()).thenReturn(true);
        when(question.getType()).thenReturn(Survey.Question.QuestionType.SINGLE_CHOICE);

        List<QuestionItem> questions = new ArrayList<>();

        boolean response = true;
        Survey.Answer answer = Survey.Answer.makeAnswer("question_id", response);
        QuestionItem questionItem = new QuestionItem(question, answer);
        questions.add(questionItem);

        List<QuestionItem> expectedList = new ArrayList<>(questions);
        gliaSurveyAnswerUseCase.trim(questions);

        assertArrayEquals(questions.toArray(), expectedList.toArray());
    }

    @Test
    public void trim_doesNotChangeAnswer_whenQuestionsContainsValidTextAnswer() {
        Survey.Question question = mock(Survey.Question.class);
        when(question.isRequired()).thenReturn(true);
        when(question.getType()).thenReturn(Survey.Question.QuestionType.TEXT);

        List<QuestionItem> questions = new ArrayList<>();

        String response = "Some text";
        Survey.Answer answer = Survey.Answer.makeAnswer("question_id", response);
        QuestionItem questionItem = new QuestionItem(question, answer);
        questions.add(questionItem);

        gliaSurveyAnswerUseCase.trim(questions);

        Object expectedResponse = Objects.requireNonNull(questions.get(0).getAnswer()).getResponse();
        assertEquals(expectedResponse, response);
    }

    @Test
    public void trim_ChangesTextAnswer_whenQuestionsContainsTextAnswerWithTrailingSpaces() {
        Survey.Question question = mock(Survey.Question.class);
        when(question.isRequired()).thenReturn(true);
        when(question.getType()).thenReturn(Survey.Question.QuestionType.TEXT);

        List<QuestionItem> questions = new ArrayList<>();

        String response = "Some text     ";
        Survey.Answer answer = Survey.Answer.makeAnswer("question_id", response);
        QuestionItem questionItem = new QuestionItem(question, answer);
        questions.add(questionItem);

        gliaSurveyAnswerUseCase.trim(questions);

        Object expectedResponse = Objects.requireNonNull(questions.get(0).getAnswer()).getResponse();
        assertEquals(expectedResponse, "Some text");
    }

    @Test
    public void trim_replacesAnswerWithSpacesOnlyWithNull_whenQuestionsContainsTwoAnswersAnswerWithSpacesOnly() {
        Survey.Question textQuestion = mock(Survey.Question.class);
        when(textQuestion.isRequired()).thenReturn(true);
        when(textQuestion.getType()).thenReturn(Survey.Question.QuestionType.TEXT);

        List<QuestionItem> questions = new ArrayList<>();

        String response = "    ";
        Survey.Answer answer = Survey.Answer.makeAnswer("question_id", response);
        QuestionItem textItem = new QuestionItem(textQuestion, answer);
        questions.add(textItem);

        questions.add(textItem);

        gliaSurveyAnswerUseCase.trim(questions);

        assertNull(questions.get(0).getAnswer());
        assertNull(questions.get(1).getAnswer());
    }

    // Tests for a submit() function

    @Test
    public void submit_succeeds_whenQuestionsContainsOnlyValidAnswers() {
        Survey survey = mock(Survey.class);
        when(survey.getId()).thenReturn("survey_id");
        when(survey.getEngagementId()).thenReturn("engagement_id");
        Consumer<RuntimeException> callback = mock(Consumer.class);

        Survey.Question textQuestion = mock(Survey.Question.class);
        when(textQuestion.isRequired()).thenReturn(true);
        when(textQuestion.getType()).thenReturn(Survey.Question.QuestionType.TEXT);

        List<QuestionItem> questions = new ArrayList<>();

        String validTextResponse = "Some text";
        Survey.Answer validTextAnswer = Survey.Answer.makeAnswer("question_id", validTextResponse);
        QuestionItem validTextItem = new QuestionItem(textQuestion, validTextAnswer);
        questions.add(validTextItem);

        Survey.Question booleanQuestion = mock(Survey.Question.class);
        when(booleanQuestion.isRequired()).thenReturn(true);
        when(booleanQuestion.getType()).thenReturn(Survey.Question.QuestionType.BOOLEAN);
        boolean validBooleanResponse = true;
        Survey.Answer validBooleanAnswer = Survey.Answer.makeAnswer("question_id", validBooleanResponse);
        QuestionItem validBooleanItem = new QuestionItem(booleanQuestion, validBooleanAnswer);
        questions.add(validBooleanItem);

        try {
            gliaSurveyAnswerUseCase.submit(questions, survey, callback);
        } catch (Exception e) {
            if (!e.getClass().equals(SurveyValidationException.class)) {
                Assert.fail();
            }
            // Do nothing, basically succeeds test if same exception was thrown.
        }
    }

    @Test
    public void submit_throwsException_whenQuestionsContainsTextAnswerWithSpacesOnly() {
        Survey survey = mock(Survey.class);
        Consumer<RuntimeException> callback = mock(Consumer.class);

        Survey.Question textQuestion = mock(Survey.Question.class);
        when(textQuestion.isRequired()).thenReturn(true);
        when(textQuestion.getType()).thenReturn(Survey.Question.QuestionType.TEXT);

        List<QuestionItem> questions = new ArrayList<>();

        String invalidTextResponse = "    ";
        Survey.Answer invalidTextAnswer = Survey.Answer.makeAnswer("question_id", invalidTextResponse);
        QuestionItem invalidTextItem = new QuestionItem(textQuestion, invalidTextAnswer);
        questions.add(invalidTextItem);

        Survey.Question booleanQuestion = mock(Survey.Question.class);
        when(booleanQuestion.isRequired()).thenReturn(true);
        when(booleanQuestion.getType()).thenReturn(Survey.Question.QuestionType.BOOLEAN);
        boolean validBooleanResponse = true;
        Survey.Answer validBooleanAnswer = Survey.Answer.makeAnswer("question_id", validBooleanResponse);
        QuestionItem validBooleanItem = new QuestionItem(booleanQuestion, validBooleanAnswer);
        questions.add(validBooleanItem);

        gliaSurveyAnswerUseCase.submit(questions, survey, callback);

        verify(callback).accept(ArgumentMatchers.any(SurveyValidationException.class));
    }
}
