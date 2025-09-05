package com.glia.widgets.survey.viewholder

import android.graphics.Typeface
import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.databinding.SurveyScaleQuestionItemBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.survey.QuestionItem
import com.glia.widgets.survey.SurveyAdapter
import com.glia.widgets.view.button.GliaSurveyOptionButton
import com.glia.widgets.view.configuration.survey.SurveyStyle
import com.glia.widgets.view.unifiedui.applyOptionButtonTheme
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.survey.SurveyScaleQuestionTheme

internal class ScaleQuestionViewHolder(
    private val binding: SurveyScaleQuestionItemBinding,
    private var style: SurveyStyle
) : SurveyViewHolder(binding.root, binding.tvTitle, binding.requiredError) {
    private val questionTheme: SurveyScaleQuestionTheme? by lazy {
        Dependencies.gliaThemeManager.theme?.surveyTheme?.scaleQuestion
    }
    private val buttons: List<GliaSurveyOptionButton>
        get() = listOf(
            binding.scale1Button,
            binding.scale2Button,
            binding.scale3Button,
            binding.scale4Button,
            binding.scale5Button
        )

    init {
        val titleConfiguration = style.scaleQuestion.title
        title.setTextColor(titleConfiguration.textColor)
        val textSize = titleConfiguration.textSize
        title.textSize = textSize
        if (titleConfiguration.isBold) title.typeface = Typeface.DEFAULT_BOLD
        questionTheme?.title?.also(title::applyTextTheme)

        buttons.forEach {
            it.setStyle(style.scaleQuestion.optionButton)
            it.applyOptionButtonTheme(questionTheme?.surveyOption)
        }
    }

    override fun onBind(
        questionItem: QuestionItem,
        listener: SurveyAdapter.SurveyAdapterListener?
    ) {
        super.onBind(questionItem, listener)
        applyAnswer(questionItem.answer)
        buttons.forEachIndexed { index, button ->
            button.setOnClickListener { setAnswer(index + 1) }
        }
    }

    override fun applyAnswer(answer: Survey.Answer?) {
        if (answer != null) {
            val value = answer.getResponse<Int>()
            setSelected(value)
        } else {
            unselectAll()
        }
    }

    private fun setSelected(value: Int) {
        for (i in buttons.indices) {
            val button: GliaSurveyOptionButton = buttons[i]
            val isSelected = i + 1 == value
            button.isSelected = isSelected
            button.applyOptionButtonTheme(questionTheme?.surveyOption)
        }
    }

    private fun unselectAll() {
        setSelected(0)
    }

    override fun showRequiredError(error: Boolean) {
        super.showRequiredError(error)
        buttons.forEach {
            it.isError = error
            it.applyOptionButtonTheme(questionTheme?.surveyOption)
        }
        if (error) requiredError.applyTextTheme(questionTheme?.surveyOption?.error)
    }
}
