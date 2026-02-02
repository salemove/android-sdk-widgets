package com.glia.widgets.base

/**
 * Marker interface for MVI UI state.
 *
 * Implement this interface for all screen state data classes.
 * Example:
 * ```
 * data class SurveyUiState(
 *     val title: String? = null,
 *     val questions: List<QuestionItem> = emptyList(),
 *     val isSubmitting: Boolean = false
 * ) : UiState
 * ```
 */
internal interface UiState

/**
 * Marker interface for MVI user intents/actions.
 *
 * Implement this interface for sealed interfaces representing user actions.
 * Example:
 * ```
 * sealed interface SurveyIntent : UiIntent {
 *     data class Initialize(val survey: Survey) : SurveyIntent
 *     data object SubmitSurvey : SurveyIntent
 * }
 * ```
 */
internal interface UiIntent

/**
 * Marker interface for MVI one-time effects.
 *
 * Implement this interface for sealed interfaces representing side effects.
 * Example:
 * ```
 * sealed interface SurveyEffect : UiEffect {
 *     data object Dismiss : SurveyEffect
 *     data class ShowError(val message: String) : SurveyEffect
 * }
 * ```
 */
internal interface UiEffect
