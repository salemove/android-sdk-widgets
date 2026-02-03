package com.glia.widgets.survey

import com.glia.androidsdk.GliaException
import com.glia.androidsdk.engagement.Survey
import com.glia.telemetry_lib.ButtonNames
import com.glia.telemetry_lib.EventAttribute
import com.glia.telemetry_lib.GliaLogger
import com.glia.telemetry_lib.LogEvents
import com.glia.widgets.base.BaseViewModel
import com.glia.widgets.internal.survey.domain.GliaSurveyAnswerUseCase

/**
 * ViewModel for Survey screen implementing MVI pattern.
 *
 * Handles:
 * - Survey initialization and state management
 * - Question-to-QuestionItem mapping
 * - Answer collection and validation
 * - Submit/cancel orchestration
 * - Telemetry logging
 */
internal class SurveyViewModel(
    private val surveyAnswerUseCase: GliaSurveyAnswerUseCase
) : BaseViewModel<SurveyUiState, SurveyIntent, SurveyEffect>(SurveyUiState()) {

    private var survey: Survey? = null

    override suspend fun handleIntent(intent: SurveyIntent) {
        when (intent) {
            is SurveyIntent.Initialize -> handleInitialize(intent.survey)
            is SurveyIntent.AnswerQuestion -> handleAnswer(intent.answer)
            SurveyIntent.SubmitSurvey -> handleSubmit()
            SurveyIntent.CancelSurvey -> handleCancel()
        }
    }

    private fun handleInitialize(survey: Survey) {
        if (isAlreadyInitialized(survey)) return

        this.survey = survey
        val questions: List<QuestionItem> = survey.questions?.map { makeQuestionItem(it) } ?: emptyList()
        updateState {
            copy(title = survey.title, questions = questions)
        }
    }

    private fun isAlreadyInitialized(newSurvey: Survey): Boolean {
        val currentSurvey = survey ?: return false
        return currentSurvey.id == newSurvey.id &&
            currentSurvey.engagementId == newSurvey.engagementId &&
            currentState.questions.isNotEmpty()
    }

    private fun makeQuestionItem(question: Survey.Question): QuestionItem {
        var answer: Survey.Answer? = null
        if (question.type == Survey.Question.QuestionType.SINGLE_CHOICE) {
            question.options
                ?.firstOrNull { it.isDefault }
                ?.let { option ->
                    answer = Survey.Answer.makeAnswer(question.id, option.id)
                }
        }
        return QuestionItem(question, answer)
    }

    private fun handleAnswer(answer: Survey.Answer) {
        val questions: MutableList<QuestionItem> = currentState.questions.toMutableList()
        val index: Int = questions.indexOfFirst { it.question.id == answer.questionId }
        if (index >= 0) {
            val item: QuestionItem = questions[index]
            // Create a new QuestionItem with updated answer and clear error
            val updatedItem: QuestionItem = QuestionItem(item.question, answer).apply {
                isShowError = false
            }
            questions[index] = updatedItem
            updateState { copy(questions = questions) }

            if (item.question.type != Survey.Question.QuestionType.TEXT) {
                sendEffect(SurveyEffect.HideSoftKeyboard)
            }
        }
    }

    private fun handleSubmit() {
        GliaLogger.i(
            LogEvents.SURVEY_SCREEN_BUTTON_CLICKED,
            null,
            mapOf(EventAttribute.ButtonName to ButtonNames.SUBMIT)
        )

        val questions: List<QuestionItem> = currentState.questions
        val survey: Survey = this.survey ?: return

        updateState { copy(isSubmitting = true) }

        surveyAnswerUseCase.submit(questions, survey) { exception ->
            updateState { copy(isSubmitting = false) }

            if (exception == null) {
                sendEffect(SurveyEffect.Dismiss)
                return@submit
            }

            if (exception is GliaException && exception.cause == GliaException.Cause.NETWORK_TIMEOUT) {
                sendEffect(SurveyEffect.ShowNetworkError)
            }

            // Update questions with error states (set by surveyAnswerUseCase.submit)
            val updatedQuestions: List<QuestionItem> = questions.map { item ->
                QuestionItem(item.question, item.answer).apply {
                    isShowError = item.isShowError
                }
            }
            updateState { copy(questions = updatedQuestions) }

            val firstErrorIndex: Int = updatedQuestions.indexOfFirst { it.isShowError }
            if (firstErrorIndex >= 0) {
                sendEffect(SurveyEffect.ScrollToQuestion(firstErrorIndex))
            }
        }
    }

    private fun handleCancel() {
        GliaLogger.i(
            LogEvents.SURVEY_SCREEN_BUTTON_CLICKED,
            null,
            mapOf(EventAttribute.ButtonName to ButtonNames.CANCEL)
        )
        sendEffect(SurveyEffect.Dismiss)
    }
}