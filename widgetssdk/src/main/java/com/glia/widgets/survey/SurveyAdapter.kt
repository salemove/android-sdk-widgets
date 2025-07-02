package com.glia.widgets.survey

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.glia.androidsdk.engagement.Survey
import com.glia.androidsdk.engagement.Survey.Question.QuestionType
import com.glia.widgets.databinding.SurveyBooleanQuestionItemBinding
import com.glia.widgets.databinding.SurveyInputQuestionItemBinding
import com.glia.widgets.databinding.SurveyScaleQuestionItemBinding
import com.glia.widgets.databinding.SurveySingleQuestionItemBinding
import com.glia.widgets.survey.viewholder.BooleanQuestionViewHolder
import com.glia.widgets.survey.viewholder.InputQuestionViewHolder
import com.glia.widgets.survey.viewholder.ScaleQuestionViewHolder
import com.glia.widgets.survey.viewholder.SingleQuestionViewHolder
import com.glia.widgets.survey.viewholder.SurveyViewHolder
import com.glia.widgets.view.configuration.survey.SurveyStyle

internal class SurveyAdapter(
    private val listener: SurveyAdapterListener,
    private val style: SurveyStyle
) : RecyclerView.Adapter<SurveyViewHolder>() {
    interface SurveyAdapterListener {
        fun onAnswer(answer: Survey.Answer)
    }

    private val questionItems: MutableList<QuestionItem> = ArrayList()

    fun submitList(items: List<QuestionItem>?) {
        questionItems.clear()
        if (items == null) {
            return
        }
        questionItems.addAll(items)
    }

    private fun getItem(position: Int): QuestionItem = questionItems[position]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SurveyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            SURVEY_SCALE -> {
                val binding = SurveyScaleQuestionItemBinding.inflate(inflater, parent, false)
                ScaleQuestionViewHolder(binding, style)
            }

            SURVEY_YES_NO -> {
                val binding = SurveyBooleanQuestionItemBinding.inflate(inflater, parent, false)
                BooleanQuestionViewHolder(binding, style)
            }

            SURVEY_SINGLE_CHOICE -> {
                val binding = SurveySingleQuestionItemBinding.inflate(inflater, parent, false)
                SingleQuestionViewHolder(binding, style)
            }

            else -> {
                val binding = SurveyInputQuestionItemBinding.inflate(inflater, parent, false)
                InputQuestionViewHolder(binding, style)
            }
        }
    }

    override fun onBindViewHolder(viewHolder: SurveyViewHolder, position: Int) =
        viewHolder.onBind(getItem(position), listener)

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position).question.type) {
            QuestionType.TEXT -> SURVEY_OPEN_TEXT
            QuestionType.BOOLEAN -> SURVEY_YES_NO
            QuestionType.SINGLE_CHOICE -> SURVEY_SINGLE_CHOICE
            else -> SURVEY_SCALE
        }
    }

    override fun getItemCount(): Int {
        return questionItems.size
    }

    companion object {
        private const val SURVEY_SCALE = 1
        private const val SURVEY_YES_NO = 2
        private const val SURVEY_SINGLE_CHOICE = 3
        private const val SURVEY_OPEN_TEXT = 4
    }
}
