package com.glia.widgets.survey.viewholder

import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.R
import com.glia.widgets.databinding.SurveyBooleanQuestionItemBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.setLocaleText
import com.glia.widgets.survey.QuestionItem
import com.glia.widgets.survey.SurveyAdapter
import com.glia.widgets.view.button.GliaSurveyOptionButton
import com.glia.widgets.view.unifiedui.applyOptionButtonTheme
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.survey.SurveyBooleanQuestionTheme

internal class BooleanQuestionViewHolder(
    private val binding: SurveyBooleanQuestionItemBinding
) : SurveyViewHolder(binding.root, binding.tvTitle, binding.requiredError) {
    private val booleanTheme: SurveyBooleanQuestionTheme? by lazy {
        Dependencies.gliaThemeManager.theme?.surveyTheme?.booleanQuestion
    }
    private val yesButton: GliaSurveyOptionButton get() = binding.yesButton
    private val noButton: GliaSurveyOptionButton get() = binding.noButton

    init {
        // The title's colour, size and weight come from `survey_boolean_question_item.xml`; the
        // buttons' from `?attr/buttonSurveyOptionButtonStyle`. Only the JSON theme is left to apply.
        booleanTheme?.title?.also(title::applyTextTheme)

        yesButton.setLocaleText(R.string.general_yes)
        yesButton.setOnClickListener { setAnswer(true) }

        noButton.setLocaleText(R.string.general_no)
        noButton.setOnClickListener { setAnswer(false) }
    }

    override fun onBind(questionItem: QuestionItem, listener: SurveyAdapter.SurveyAdapterListener?) {
        super.onBind(questionItem, listener)
        applyAnswer(questionItem.answer)
    }

    override fun applyAnswer(answer: Survey.Answer?) {
        if (answer != null) {
            val value = answer.getResponse<Boolean>()
            setSelected(value)
        } else {
            unselectAll()
        }
    }

    private fun setSelected(value: Boolean) {
        yesButton.isSelected = value
        noButton.isSelected = !value
        applyBooleanTheme()
    }

    private fun unselectAll() {
        yesButton.isSelected = false
        noButton.isSelected = false
        applyBooleanTheme()
    }

    override fun showRequiredError(error: Boolean) {
        super.showRequiredError(error)
        yesButton.isError = error
        noButton.isError = error
        if (error) applyBooleanThemeWithError() else applyBooleanTheme()
    }

    private fun applyBooleanThemeWithError() {
        applyBooleanTheme()
        booleanTheme?.surveyOption?.also {
            requiredError.applyTextTheme(it.error)
        }
    }

    private fun applyBooleanTheme() {
        booleanTheme?.surveyOption?.also {
            yesButton.applyOptionButtonTheme(it)
            noButton.applyOptionButtonTheme(it)
        }
    }
}
