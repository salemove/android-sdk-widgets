package com.glia.widgets.survey

import com.android.ide.common.rendering.api.SessionParams
import com.glia.widgets.SnapshotTest
import org.junit.Test

/**
 * Every survey text follows the theme font, including the single-choice option labels, which are
 * built in code rather than inflated from a layout.
 */
internal class SurveyViewCustomFontSnapshotTest : SnapshotTest(
    renderingMode = SessionParams.RenderingMode.NORMAL,
    theme = "Test_Glia_CustomFont"
), SnapshotSurveyView {

    @Test
    fun answered() {
        snapshot(setupView(questions = allQuestionTypes(answered = true)))
    }
}
