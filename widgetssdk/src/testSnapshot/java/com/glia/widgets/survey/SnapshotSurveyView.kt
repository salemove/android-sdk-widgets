package com.glia.widgets.survey

import com.glia.androidsdk.engagement.Survey
import com.glia.androidsdk.engagement.Survey.Question.QuestionType
import com.glia.widgets.R
import com.glia.widgets.di.Dependencies
import com.glia.widgets.snapshotutils.SnapshotContent
import com.glia.widgets.snapshotutils.SnapshotProviders
import com.glia.widgets.snapshotutils.SnapshotTestLifecycle
import com.glia.widgets.snapshotutils.SnapshotTheme
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Builds a [SurveyView] carrying one question of every type.
 *
 * Lives in `com.glia.widgets.survey` rather than `snapshotutils` because [SurveyState.Builder] is
 * package-private - the production surface has no other way to hand the view a state.
 */
internal interface SnapshotSurveyView : SnapshotTestLifecycle, SnapshotContent, SnapshotProviders, SnapshotTheme {

    fun question(
        type: QuestionType,
        text: String,
        required: Boolean = false,
        options: List<String> = emptyList()
    ): Survey.Question {
        // Built before the question is stubbed: Mockito rejects a `whenever` opened inside another.
        val optionMocks = options.map { label ->
            mock<Survey.Question.Option>().also {
                whenever(it.id).thenReturn(label)
                whenever(it.label).thenReturn(label)
            }
        }

        return mock<Survey.Question>().also { question ->
            whenever(question.id).thenReturn(text)
            whenever(question.text).thenReturn(text)
            whenever(question.type).thenReturn(type)
            whenever(question.isRequired).thenReturn(required)
            whenever(question.options).thenReturn(optionMocks)
        }
    }

    /**
     * One question of every type, so a single golden covers all four view holders.
     *
     * @param answered pre-selects an answer on the scale, boolean and single-choice questions, which
     * is the only way to see the selected colours.
     * @param showError puts every question into its validation-error state.
     */
    fun allQuestionTypes(answered: Boolean = false, showError: Boolean = false): List<QuestionItem> {
        val scale = question(QuestionType.SCALE, "How would you rate us?", required = true)
        val boolean = question(QuestionType.BOOLEAN, "Was your issue resolved?", required = true)
        val single = question(
            QuestionType.SINGLE_CHOICE,
            "How did you hear about us?",
            required = true,
            options = listOf("Search", "A friend", "Somewhere else")
        )
        val text = question(QuestionType.TEXT, "Anything else you would like to add?", required = true)

        return listOf(
            QuestionItem(scale, Survey.Answer.makeAnswer(scale.id, 4).takeIf { answered }),
            QuestionItem(boolean, Survey.Answer.makeAnswer(boolean.id, true).takeIf { answered }),
            QuestionItem(single, Survey.Answer.makeAnswer(single.id, "A friend").takeIf { answered }),
            QuestionItem(text, Survey.Answer.makeAnswer(text.id, "The operator was great.").takeIf { answered })
        ).onEach { it.setShowError(showError) }
    }

    /**
     * `test_unified_config.json` has no `surveyScreen` section, so it cannot show the JSON theme
     * winning over the composed one. This config exists for that: every survey layer it declares uses
     * a colour that appears nowhere in the SDK defaults or in `Test.Glia.Survey.Customized`.
     */
    fun unifiedSurveyTheme(): UnifiedTheme = unifiedTheme(R.raw.survey_unified_config)

    fun setupView(
        questions: List<QuestionItem> = allQuestionTypes(),
        title: String = "Please rate your experience",
        unifiedTheme: UnifiedTheme? = null
    ): SurveyView {
        unifiedTheme?.also { Dependencies.gliaThemeManager.theme = it }
        setOnEndListener { Dependencies.gliaThemeManager.theme = null }

        val state = SurveyState.Builder()
            .setTitle(title)
            .setQuestions(questions)
            .createSurveyState()

        return SurveyView(context).also { it.onStateUpdated(state) }
    }
}
