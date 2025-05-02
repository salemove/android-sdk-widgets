package com.glia.widgets.survey;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.glia.androidsdk.GliaException;
import com.glia.androidsdk.engagement.Survey;
import com.glia.widgets.internal.survey.domain.GliaSurveyAnswerUseCase;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @hide
 */
public class SurveyController implements SurveyContract.Controller {

    /**
     * @hide
     */
    public interface AnswerCallback {
        void answerCallback(boolean showError);
    }

    private SurveyContract.View view;

    @VisibleForTesting
    Survey survey;

    @VisibleForTesting
    SurveyState state = new SurveyState.Builder().createSurveyState();

    private final GliaSurveyAnswerUseCase gliaSurveyAnswerUseCase;

    public SurveyController(GliaSurveyAnswerUseCase gliaSurveyAnswerUseCase) {
        this.gliaSurveyAnswerUseCase = gliaSurveyAnswerUseCase;
    }

    @Override
    public void init(@Nullable Survey survey) {
        if (isAlreadyInit(survey)) {
            setState(state);
            return;
        }

        this.survey = survey;
        if (survey == null) {
            if (view != null) {
                view.finish();
                resetController();
            }
            return;
        }
        setTitle(survey.getTitle());
        setQuestions(survey);
    }

    @VisibleForTesting
    boolean isAlreadyInit(@Nullable Survey survey) {
        if (this.survey == null || survey == null) {
            return false;
        }
        return isEqualsSurveys(this.survey, survey) && isStateContainQuestions(state);
    }

    private boolean isEqualsSurveys(@NonNull Survey survey, @NonNull Survey otherSurvey) {
        return survey.getId().equals(otherSurvey.getId())
                && survey.getEngagementId().equals(otherSurvey.getEngagementId());
    }

    private boolean isStateContainQuestions(@NonNull SurveyState state) {
        return state.questions != null && !state.questions.isEmpty();
    }

    private void setTitle(String title) {
        setState(new SurveyState.Builder()
                .copyFrom(state)
                .setTitle(title)
                .createSurveyState()
        );
    }

    private void setQuestions(@NonNull Survey survey) {
        List<QuestionItem> questionItems = survey.getQuestions().stream()
                .map(this::makeQuestionItem)
                .collect(Collectors.toList());
        setState(new SurveyState.Builder()
                .copyFrom(state)
                .setQuestions(questionItems)
                .createSurveyState()
        );
    }

    private QuestionItem makeQuestionItem(Survey.Question question) {
        Survey.Answer answer = null;
        String questionId = question.getId();
        if (question.getType() == Survey.Question.QuestionType.SINGLE_CHOICE) {
            List<Survey.Question.Option> options = question.getOptions();
            if (options != null) {
                Survey.Question.Option option = options.stream()
                        .filter(Survey.Question.Option::isDefault)
                        .findFirst()
                        .orElse(null);
                if (option != null) {
                    answer = Survey.Answer.makeAnswer(questionId, option.getId());
                }
            }
        }
        return new QuestionItem(question, answer);
    }

    @Override
    public void setView(SurveyContract.View view) {
        this.view = view;
    }

    @Override
    public void onAnswer(@NonNull Survey.Answer answer) {
        if (state.questions == null) {
            return;
        }
        state.questions.stream()
                .filter(item -> item.getQuestion().getId().equals(answer.getQuestionId()))
                .findFirst()
                .ifPresent(item -> {
                    setAnswer(item, answer);
                    hideSoftKeyboardIfNeeds(item);
                });
    }

    private void hideSoftKeyboardIfNeeds(QuestionItem item) {
        if (item.getQuestion().getType() != Survey.Question.QuestionType.TEXT) {
            view.hideSoftKeyboard();
        }
    }

    private void setAnswer(QuestionItem item, Survey.Answer answer) {
        item.setAnswer(answer);
        if (item.isShowError()) {
            validate(item);
        }
    }

    private void validate(QuestionItem item) {
        boolean showError;
        try {
            gliaSurveyAnswerUseCase.validate(item);
            showError = false;
        } catch (SurveyValidationException ignore) {
            showError = true;
        }
        item.setShowError(showError);
        if (item.getAnswerCallback() != null) {
            item.getAnswerCallback().answerCallback(showError);
        }
    }

    @Override
    public void onCancelClicked() {
        if (view != null) {
            view.finish();
            resetController();
        }
    }

    @Override
    public void onSubmitClicked() {
        List<QuestionItem> questionItems = state.questions;
        if (questionItems == null) {
            return;
        }
        gliaSurveyAnswerUseCase.submit(questionItems, survey, exception -> {
            if (exception == null) {
                if (view != null) {
                    view.finish();
                    resetController();
                }
                return;
            } else if (exception instanceof GliaException gliaException) {
                if (gliaException.cause == GliaException.Cause.NETWORK_TIMEOUT) {
                    view.onNetworkTimeout();
                }
                // Ignore other Glia exceptions
            }
            questionItems.forEach(item -> {
                if (item.getAnswerCallback() != null) {
                    item.getAnswerCallback().answerCallback(item.isShowError());
                }
            });
            questionItems.stream()
                    .filter(QuestionItem::isShowError)
                    .findFirst()
                    .ifPresent(item -> view.scrollTo(questionItems.indexOf(item)));
        });
    }

    private synchronized void setState(SurveyState state) {
        this.state = state;
        if (view != null) {
            view.onStateUpdated(state);
        }
    }

    @Override
    public void onDestroy() {
        this.view = null;
    }

    private void resetController() {
        state = new SurveyState.Builder().createSurveyState();
        survey = null;
    }
}
