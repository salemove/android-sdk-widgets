package com.glia.widgets.survey.viewholder

import android.graphics.Typeface
import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.databinding.SurveyBooleanQuestionItemBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.survey.QuestionItem
import com.glia.widgets.survey.SurveyAdapter
import com.glia.widgets.view.button.GliaSurveyOptionButton
import com.glia.widgets.view.configuration.survey.SurveyStyle
import com.glia.widgets.view.unifiedui.exstensions.applyOptionButtonTheme
import com.glia.widgets.view.unifiedui.exstensions.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.survey.SurveyBooleanQuestionTheme

class BooleanQuestionViewHolder(
    private val binding: SurveyBooleanQuestionItemBinding,
    style: SurveyStyle
    ) : SurveyViewHolder(binding.root, binding.tvTitle, binding.requiredError) {
    private val booleanTheme: SurveyBooleanQuestionTheme? by lazy {
        Dependencies.getGliaThemeManager().theme?.surveyTheme?.booleanQuestion
    }
    private val yesButton: GliaSurveyOptionButton get() = binding.yesButton
    private val noButton: GliaSurveyOptionButton get() = binding.noButton

    init {
        val questionStyle = style.booleanQuestion
        val titleConfiguration = questionStyle.title
        title.setTextColor(titleConfiguration.textColor)
        val textSize = titleConfiguration.textSize
        title.textSize = textSize
        if (titleConfiguration.isBold) title.typeface = Typeface.DEFAULT_BOLD
        booleanTheme?.title?.also(title::applyTextTheme)

        val buttonConfiguration = questionStyle.optionButton
        yesButton.setStyle(buttonConfiguration)
        yesButton.setOnClickListener { setAnswer(true) }
        noButton.setStyle(buttonConfiguration)
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
        applyBooleanTheme()
    }

    private fun applyBooleanTheme() {
        booleanTheme?.optionButton?.also {
            yesButton.applyOptionButtonTheme(it)
            noButton.applyOptionButtonTheme(it)
        }
    }
}