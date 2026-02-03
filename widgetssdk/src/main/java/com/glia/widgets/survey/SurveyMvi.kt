package com.glia.widgets.survey

import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.base.UiEffect
import com.glia.widgets.base.UiIntent
import com.glia.widgets.base.UiState

/**
 * UI state for Survey screen.
 */
internal data class SurveyUiState(
    val title: String? = null,
    val questions: List<QuestionItem> = emptyList(),
    val isSubmitting: Boolean = false
) : UiState

/**
 * User intents for Survey screen.
 */
internal sealed interface SurveyIntent : UiIntent {
    data class Initialize(val survey: Survey) : SurveyIntent
    data class AnswerQuestion(val answer: Survey.Answer) : SurveyIntent
    data object SubmitSurvey : SurveyIntent
    data object CancelSurvey : SurveyIntent
}

/**
 * One-time effects for Survey screen.
 */
internal sealed interface SurveyEffect : UiEffect {
    data object Dismiss : SurveyEffect
    data object HideSoftKeyboard : SurveyEffect
    data class ScrollToQuestion(val index: Int) : SurveyEffect
    data object ShowNetworkError : SurveyEffect
}