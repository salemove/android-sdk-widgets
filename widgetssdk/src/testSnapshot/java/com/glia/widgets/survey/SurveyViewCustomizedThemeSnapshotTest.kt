package com.glia.widgets.survey

import com.android.ide.common.rendering.api.SessionParams
import com.glia.widgets.SnapshotTest
import org.junit.Test

/**
 * The point of Phase 8: with a customized brand palette the survey widgets follow it.
 *
 * Rendered against `Test.Glia.Survey.Customized`, which overrides three `glia*` attributes exactly
 * the way an integrator's `GliaTheme` would. Before the migration all of this was Glia-blue on
 * Glia-purple no matter what the theme said, because the question widgets read raw colour resources
 * through `Dependencies.resourceProvider`.
 */
internal class SurveyViewCustomizedThemeSnapshotTest : SnapshotTest(
    renderingMode = SessionParams.RenderingMode.NORMAL,
    theme = "Test_Glia_Survey_Customized"
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
    fun unifiedThemeStillWins() {
        snapshot(setupView(questions = allQuestionTypes(answered = true), unifiedTheme = unifiedSurveyTheme()))
    }
}
