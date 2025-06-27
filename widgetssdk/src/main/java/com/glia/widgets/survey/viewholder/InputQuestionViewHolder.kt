package com.glia.widgets.survey.viewholder

import android.graphics.Typeface
import android.view.View
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.R
import com.glia.widgets.databinding.SurveyInputQuestionItemBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.getColorCompat
import com.glia.widgets.helper.setLocaleHint
import com.glia.widgets.survey.QuestionItem
import com.glia.widgets.survey.SurveyAdapter
import com.glia.widgets.view.configuration.OptionButtonConfiguration
import com.glia.widgets.view.configuration.survey.InputQuestionConfiguration
import com.glia.widgets.view.configuration.survey.SurveyStyle
import com.glia.widgets.view.unifiedui.applyLayerTheme
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.nullSafeMerge
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme
import com.glia.widgets.view.unifiedui.theme.survey.OptionButtonTheme
import com.glia.widgets.view.unifiedui.theme.survey.SurveyInputQuestionTheme
import androidx.core.graphics.toColorInt

internal class InputQuestionViewHolder(
    val binding: SurveyInputQuestionItemBinding,
    val style: SurveyStyle
) : SurveyViewHolder(binding.root, binding.tvTitle, binding.requiredError) {
    private val inputTheme: SurveyInputQuestionTheme? by lazy {
        Dependencies.gliaThemeManager.theme?.surveyTheme?.inputQuestion
    }
    private val comment: EditText get() = binding.etComment

    private val optionButtonTheme: OptionButtonTheme by lazy {
        createOptionButtonTheme(style.inputQuestion.optionButton)
    }

    init {
        val inputQuestionConfig = style.inputQuestion
        setupTitle(inputQuestionConfig)
        setupInputBoxText(inputQuestionConfig.optionButton)
    }

    override fun onBind(
        questionItem: QuestionItem,
        listener: SurveyAdapter.SurveyAdapterListener?
    ) {
        super.onBind(questionItem, listener)
        applyAnswer(questionItem.answer)
    }

    override fun applyAnswer(answer: Survey.Answer?) {
        if (answer != null) {
            val oldValue = comment.text.toString()
            val newValue = answer.getResponse<String>()
            if (oldValue != newValue) {
                comment.setText(newValue)
            }
        } else {
            comment.text = null
        }
    }

    override fun showRequiredError(error: Boolean) {
        super.showRequiredError(error)

        if (error) {
            comment.applyLayerTheme(optionButtonTheme.highlightedLayer)
        } else {
            comment.applyLayerTheme(optionButtonTheme.normalLayer)
        }
    }

    private fun createOptionButtonTheme(optionButtonConfiguration: OptionButtonConfiguration): OptionButtonTheme {
        val strokeWidth = optionButtonConfiguration.normalLayer.borderWidth.toFloat()
        val normalStrokeColor = optionButtonConfiguration.normalLayer.borderColor.toColorInt()
        val selectedStrokeColor = optionButtonConfiguration.selectedLayer.borderColor.toColorInt()
        val highlightedStrokeColor = optionButtonConfiguration.highlightedLayer.borderColor.toColorInt()
        val backgroundColor = ColorTheme(optionButtonConfiguration.normalLayer.backgroundColor.toColorInt())

        return OptionButtonTheme(
            normalText = TextTheme(),
            normalLayer = LayerTheme(fill = backgroundColor, stroke = normalStrokeColor, borderWidth = strokeWidth),
            selectedText = TextTheme(),
            selectedLayer = LayerTheme(fill = backgroundColor, stroke = selectedStrokeColor, borderWidth = strokeWidth),
            highlightedText = TextTheme(),
            highlightedLayer = LayerTheme(fill = backgroundColor, stroke = highlightedStrokeColor, borderWidth = strokeWidth),
            fontSize = null,
            fontStyle = null
        ) nullSafeMerge inputTheme?.option

    }

    private fun setupTitle(inputQuestionConfig: InputQuestionConfiguration) {
        val titleConfiguration = inputQuestionConfig.title
        title.setTextColor(titleConfiguration.textColor)
        val textSize = titleConfiguration.textSize
        title.textSize = textSize
        if (titleConfiguration.isBold) title.typeface = Typeface.DEFAULT_BOLD

        inputTheme?.title?.also(title::applyTextTheme)
    }

    private fun setupInputBoxText(optionButtonStyle: OptionButtonConfiguration) {
        comment.setLocaleHint(R.string.general_comment)
        comment.setTextColor(optionButtonStyle.normalText.textColor)
        if (optionButtonStyle.normalText.isBold) comment.typeface = Typeface.DEFAULT_BOLD
        comment.setHintTextColor(itemView.getColorCompat(R.color.glia_shade_color))
        val textSize = optionButtonStyle.normalText.textSize
        comment.textSize = textSize
        comment.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            setAnswer(comment.text.toString())

            if (hasFocus) {
                comment.applyTextTheme(inputTheme?.text)
                comment.applyLayerTheme(optionButtonTheme.selectedLayer)
            } else {
                comment.applyTextTheme(inputTheme?.text)
                comment.applyLayerTheme(optionButtonTheme.normalLayer)
            }
        }
        comment.doAfterTextChanged { setAnswer(it.toString()) }

        comment.applyTextTheme(inputTheme?.text)
        comment.applyLayerTheme(optionButtonTheme.normalLayer)
    }
}
