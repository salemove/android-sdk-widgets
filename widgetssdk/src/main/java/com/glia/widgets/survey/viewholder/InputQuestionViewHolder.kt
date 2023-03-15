package com.glia.widgets.survey.viewholder

import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.R
import com.glia.widgets.databinding.SurveyInputQuestionItemBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.survey.QuestionItem
import com.glia.widgets.survey.SurveyAdapter
import com.glia.widgets.view.configuration.OptionButtonConfiguration
import com.glia.widgets.view.configuration.survey.InputQuestionConfiguration
import com.glia.widgets.view.configuration.survey.SurveyStyle
import com.glia.widgets.view.unifiedui.exstensions.applyLayerTheme
import com.glia.widgets.view.unifiedui.exstensions.applyTextTheme
import com.glia.widgets.view.unifiedui.exstensions.deepMerge
import com.glia.widgets.view.unifiedui.exstensions.getColorCompat
import com.glia.widgets.view.unifiedui.exstensions.deepMerge
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme
import com.glia.widgets.view.unifiedui.theme.survey.OptionButtonTheme
import com.glia.widgets.view.unifiedui.theme.survey.SurveyInputQuestionTheme

class InputQuestionViewHolder(
    val binding: SurveyInputQuestionItemBinding,
    val style: SurveyStyle
) : SurveyViewHolder(binding.root, binding.tvTitle, binding.requiredError) {
    private val inputTheme: SurveyInputQuestionTheme? by lazy {
        Dependencies.getGliaThemeManager().theme?.surveyTheme?.inputQuestion
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
        val normalStrokeColor = Color.parseColor(optionButtonConfiguration.normalLayer.borderColor)
        val highlightedStrokeColor =
            Color.parseColor(optionButtonConfiguration.highlightedLayer.borderColor)
        val backgroundColor =
            ColorTheme(Color.parseColor(optionButtonConfiguration.normalLayer.backgroundColor))

        val baseTheme = OptionButtonTheme(
            normalText = TextTheme(),
            normalLayer = LayerTheme(
                fill = backgroundColor,
                stroke = normalStrokeColor,
                borderWidth = strokeWidth
            ),
            selectedText = TextTheme(),
            selectedLayer = LayerTheme(),
            highlightedText = TextTheme(),
            highlightedLayer = LayerTheme(
                fill = backgroundColor,
                stroke = highlightedStrokeColor,
                borderWidth = strokeWidth
            ),
            fontSize = null,
            fontStyle = null
        )

        return inputTheme?.option?.let {
            baseTheme deepMerge it
        } ?: baseTheme
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
        comment.setTextColor(optionButtonStyle.normalText.textColor)
        if (optionButtonStyle.normalText.isBold) comment.typeface = Typeface.DEFAULT_BOLD
        comment.setHintTextColor(itemView.getColorCompat(R.color.glia_base_shade_color))
        val textSize = optionButtonStyle.normalText.textSize
        comment.textSize = textSize
        comment.onFocusChangeListener = View.OnFocusChangeListener { _, _ ->
            setAnswer(comment.text.toString())
        }
        comment.doAfterTextChanged { setAnswer(it.toString()) }

        comment.applyTextTheme(inputTheme?.text)
        comment.applyLayerTheme(optionButtonTheme.normalLayer)
    }
}

