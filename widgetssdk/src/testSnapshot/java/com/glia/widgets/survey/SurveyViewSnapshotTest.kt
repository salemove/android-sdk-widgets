package com.glia.widgets.survey

import com.android.ide.common.rendering.api.SessionParams
import com.glia.widgets.SnapshotTest
import org.junit.Test

/**
 * First survey goldens in the project. They lock the attribute-driven theming that replaced
 * `SurveyStyle` and the four `*QuestionConfiguration` classes: everything visible here used to be
 * painted from hard-coded colour resources that no theme attribute could reach.
 */
internal class SurveyViewSnapshotTest : SnapshotTest(
    renderingMode = SessionParams.RenderingMode.NORMAL
), SnapshotSurveyView {

    @Test
    fun unanswered() {
        snapshot(setupView())
    }

    @Test
    fun answered() {
        snapshot(setupView(questions = allQuestionTypes(answered = true)))
    }

    @Test
    fun requiredError() {
        snapshot(setupView(questions = allQuestionTypes(showError = true)))
    }

    @Test
    fun withUnifiedTheme() {
        snapshot(setupView(questions = allQuestionTypes(answered = true), unifiedTheme = unifiedSurveyTheme()))
    }

    @Test
    fun withUnifiedThemeAndRequiredError() {
        snapshot(setupView(questions = allQuestionTypes(showError = true), unifiedTheme = unifiedSurveyTheme()))
    }

    @Test
    fun withGlobalColors() {
        snapshot(setupView(unifiedTheme = unifiedThemeWithGlobalColors()))
    }
}
