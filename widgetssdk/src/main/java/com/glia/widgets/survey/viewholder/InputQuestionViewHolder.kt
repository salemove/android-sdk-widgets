package com.glia.widgets.survey.viewholder

import android.view.View
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.R
import com.glia.widgets.databinding.SurveyInputQuestionItemBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.setLocaleHint
import com.glia.widgets.survey.QuestionItem
import com.glia.widgets.survey.SurveyAdapter
import com.glia.widgets.view.unifiedui.applyHintTheme
import com.glia.widgets.view.unifiedui.applyLayerTheme
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.survey.SurveyInputQuestionTheme

internal class InputQuestionViewHolder(
    val binding: SurveyInputQuestionItemBinding
) : SurveyViewHolder(binding.root, binding.tvTitle, binding.requiredError) {
    private val inputTheme: SurveyInputQuestionTheme? by lazy {
        Dependencies.gliaThemeManager.theme?.surveyTheme?.inputQuestion
    }
    private val comment: EditText get() = binding.etComment

    init {
        // The title's appearance and the field's text, hint and stateful border all come from
        // `survey_input_question_item.xml` and `bg_survey_edit_text`, so only the JSON theme is
        // applied here. `applyLayerTheme` is a no-op when the JSON theme declares no layer, which
        // leaves the drawable's own normal / focused / error states in charge.
        inputTheme?.title?.also(title::applyTextTheme)
        setupInputBox()
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

        // Drives `android:state_activated` in `survey_input_field_stroke_color_states`.
        comment.isActivated = error

        if (error) {
            comment.applyLayerTheme(inputTheme?.inputField?.highlightedLayer)
            requiredError.applyTextTheme(inputTheme?.inputField?.error)
        }
    }

    private fun setupInputBox() {
        comment.setLocaleHint(R.string.general_comment)
        comment.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            setAnswer(comment.text.toString())

            if (hasFocus) {
                comment.applyTextTheme(inputTheme?.inputField?.selectedText)
                comment.applyLayerTheme(inputTheme?.inputField?.selectedLayer)
            } else {
                comment.applyTextTheme(inputTheme?.inputField?.normalText)
                comment.applyLayerTheme(inputTheme?.inputField?.normalLayer)
            }
        }
        comment.doAfterTextChanged { setAnswer(it.toString()) }

        comment.applyTextTheme(inputTheme?.inputField?.normalText)
        comment.applyLayerTheme(inputTheme?.inputField?.normalLayer)
        inputTheme?.inputField?.placeholder?.also(comment::applyHintTheme)
    }
}
