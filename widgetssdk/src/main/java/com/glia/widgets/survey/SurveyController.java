package com.glia.widgets.survey;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.glia.androidsdk.engagement.Survey;
import com.glia.widgets.core.dialog.DialogController;
import com.glia.widgets.core.survey.domain.GliaSurveyAnswerUseCase;
import com.glia.widgets.helper.Logger;

import java.util.List;
import java.util.stream.Collectors;

public class SurveyController implements SurveyContract.Controller {
    private static final String TAG = SurveyController.class.getSimpleName();

    public interface AnswerCallback {
        void answerCallback(boolean showError);
    }

    private SurveyContract.View view;
    private Survey survey;
    private List<QuestionItem> questionItems;
    private SurveyState state = new SurveyState();

    private final GliaSurveyAnswerUseCase gliaSurveyAnswerUseCase;
    private final DialogController dialogController;

    public SurveyController(GliaSurveyAnswerUseCase gliaSurveyAnswerUseCase,
                            DialogController dialogController) {
        this.gliaSurveyAnswerUseCase = gliaSurveyAnswerUseCase;
        this.dialogController = dialogController;
    }

    @Override
    public void init(@Nullable Survey survey) {
        this.survey = survey;
        if (survey == null) {
            if (view != null) {
                view.finish();
            }
            return;
        }
        setQuestions(survey);
    }

    private void setQuestions(@NonNull Survey survey) {
        questionItems = survey.getQuestions().stream()
                .map(this::makeQuestionItem)
                .collect(Collectors.toList());
        setState(new SurveyState(questionItems));
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
    public void setDialogCallback(DialogController.Callback callback) {
        dialogController.addCallback(callback);
    }

    @Override
    public void removeDialogCallback(DialogController.Callback callback) {
        dialogController.removeCallback(callback);
    }

    @Override
    public void onAnswer(@NonNull Survey.Answer answer) {
        questionItems.stream()
                .filter(item -> item.getQuestion().getId().equals(answer.getQuestionId()))
                .findFirst()
                .ifPresent(item -> {
                    item.setAnswer(answer);
                    if (item.isShowError()) {
                        try {
                            gliaSurveyAnswerUseCase.validate(item);
                            item.setShowError(false);
                            if (item.getAnswerCallback() != null) {
                                item.getAnswerCallback().answerCallback(false);
                            }
                        } catch (SurveyValidationException ignore) {
                            item.setShowError(true);
                            if (item.getAnswerCallback() != null) {
                                item.getAnswerCallback().answerCallback(true);
                            }
                        }
                    }
                    setState(new SurveyState(questionItems));
                });
    }

    @Override
    public void onCancelClicked() {
        if (view != null) {
            view.finish();
        }
    }

    @Override
    public void onSubmitClicked() {
        gliaSurveyAnswerUseCase.submit(questionItems, survey, exception -> {
            if (exception == null) {
                if (view != null) {
                    view.finish();
                }
                return;
            }
            if (exception instanceof SurveyValidationException) {
                questionItems.forEach(item -> {
                    if (item.getAnswerCallback() != null) {
                        item.getAnswerCallback().answerCallback(item.isShowError());
                    }
                });
                view.scrollTo(((SurveyValidationException) exception).getFirstErrorPosition());
            } else {
                dialogController.showSubmitSurveyAnswersErrorDialog();
            }
        });
    }

    private synchronized void setState(SurveyState state) {
        this.state = state;
        if (view != null) {
            view.onStateUpdated(state);
        }
    }

    @Override
    public void submitSurveyAnswersErrorDialogDismissed() {
        Logger.d(TAG, "submitSurveyAnswersErrorDialogDismissed");
        dialogController.dismissDialogs();
    }

    @Override
    public void onDestroy() {
        this.view = null;
    }
}
