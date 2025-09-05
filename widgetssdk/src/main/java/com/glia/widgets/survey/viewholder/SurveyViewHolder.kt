package com.glia.widgets.survey.viewholder

import android.text.Html
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.R
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.setLocaleContentDescription
import com.glia.widgets.helper.setLocaleText
import com.glia.widgets.survey.QuestionItem
import com.glia.widgets.survey.SurveyAdapter
import com.glia.widgets.survey.SurveyController

internal abstract class SurveyViewHolder(
    itemView: View,
    val title: TextView,
    val requiredError: TextView
) : RecyclerView.ViewHolder(itemView),
    SurveyController.AnswerCallback {
    var questionItem: QuestionItem? = null
    var listener: SurveyAdapter.SurveyAdapterListener? = null
    private val localeProvider = Dependencies.localeProvider

    init {
        requiredError.setLocaleText(R.string.survey_action_validation_error)
    }

    open fun onBind(questionItem: QuestionItem, listener: SurveyAdapter.SurveyAdapterListener?) {
        this.questionItem = questionItem
        this.listener = listener
        this.questionItem?.answerCallback = this
        setItemTitle(questionItem.question)
        showRequiredError(questionItem.isShowError)
    }

    open fun applyAnswer(answer: Survey.Answer?) {}
    private fun setItemTitle(question: Survey.Question) {
        val questionText = question.text
        if (question.isRequired) {
            val context = title.context
            val color = ContextCompat.getColor(context, R.color.glia_negative_color)
            val colorString = String.format("%X", color).substring(2)
            val source = context.getString(
                R.string.glia_survey_require_label,
                questionText,
                colorString
            )
            title.text = Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY)
            title.setLocaleContentDescription(R.string.survey_question_required_accessibility_label)
        } else {
            title.text = questionText
            title.contentDescription = null
        }
    }

    open fun showRequiredError(error: Boolean) {
        requiredError.isVisible = error
        itemView.takeIf { error }
            ?.apply {
                announceForAccessibility(localeProvider.getString(R.string.survey_action_validation_error))
            }
    }

    override fun answerCallback(showError: Boolean) {
        showRequiredError(showError)
    }

    fun setAnswer(response: Int) {
        questionItem?.question?.id
            ?.let { Survey.Answer.makeAnswer(it, response) }
            ?.also(::onAnswer)
    }

    fun setAnswer(response: Boolean) {
        questionItem?.question?.id
            ?.let { Survey.Answer.makeAnswer(it, response) }
            ?.also(::onAnswer)
    }

    fun setAnswer(response: String) {
        questionItem?.question?.id
            ?.let { Survey.Answer.makeAnswer(it, response) }
            ?.also(::onAnswer)
    }

    private fun onAnswer(answer: Survey.Answer) {
        applyAnswer(answer)
        listener?.onAnswer(answer)
    }
}
